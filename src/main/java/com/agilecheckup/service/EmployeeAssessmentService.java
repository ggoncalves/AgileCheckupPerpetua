package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.EmployeeAssessmentScore;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.QuestionScore;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepository;
import com.agilecheckup.service.dto.EmployeeValidationRequest;
import com.agilecheckup.service.dto.EmployeeValidationResponse;
import com.agilecheckup.service.exception.EmployeeAssessmentAlreadyExistsException;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.agilecheckup.service.validator.AssessmentStatusValidator;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmployeeAssessmentService extends AbstractCrudService<EmployeeAssessment, AbstractCrudRepository<EmployeeAssessment>> {

  private final AssessmentMatrixService assessmentMatrixService;

  private final TeamService teamService;

  private final EmployeeAssessmentRepository employeeAssessmentRepository;

  private final AnswerRepository answerRepository;

  @Inject
  public EmployeeAssessmentService(EmployeeAssessmentRepository employeeAssessmentRepository, AssessmentMatrixService assessmentMatrixService, TeamService teamService, AnswerRepository answerRepository) {
    this.employeeAssessmentRepository = employeeAssessmentRepository;
    this.assessmentMatrixService = assessmentMatrixService;
    this.teamService = teamService;
    this.answerRepository = answerRepository;
  }

  public Optional<EmployeeAssessment> create(@NonNull String assessmentMatrixId, String teamId, String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, Gender gender, GenderPronoun genderPronoun) {
    validateEmployeeAssessmentUniqueness(email, assessmentMatrixId);
    return super.create(createEmployeeAssessment(assessmentMatrixId, teamId, name, email, documentNumber, documentType, gender, genderPronoun));
  }

  public Optional<EmployeeAssessment> update(@NonNull String id, @NonNull String assessmentMatrixId, String teamId, String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, Gender gender, GenderPronoun genderPronoun) {
    Optional<EmployeeAssessment> optionalEmployeeAssessment = findById(id);
    if (optionalEmployeeAssessment.isPresent()) {
      EmployeeAssessment employeeAssessment = optionalEmployeeAssessment.get();
      Optional<AssessmentMatrix> assessmentMatrix = assessmentMatrixService.findById(assessmentMatrixId);
      Optional<Team> team = teamService.findById(teamId);
      
      employeeAssessment.setAssessmentMatrixId(assessmentMatrix.orElseThrow(() -> new InvalidIdReferenceException(assessmentMatrixId, getClass().getName(), "AssessmentMatrix")).getId());
      employeeAssessment.setTeamId(team.orElseThrow(() -> new InvalidIdReferenceException(teamId, getClass().getName(), "Team")).getId());
      employeeAssessment.setEmployee(createNaturalPerson(name, email, documentNumber, documentType, gender, genderPronoun, employeeAssessment.getEmployee().getId()));
      employeeAssessment.setEmployeeEmailNormalized(email.toLowerCase().trim());
      return super.update(employeeAssessment);
    } else {
      return Optional.empty();
    }
  }

  private EmployeeAssessment createEmployeeAssessment(@NonNull String assessmentMatrixId, String teamId, String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, Gender gender, GenderPronoun genderPronoun) {
    Optional<AssessmentMatrix> assessmentMatrix = assessmentMatrixService.findById(assessmentMatrixId);
    Optional<Team> team = teamService.findById(teamId);
    Team teamEntity = team.orElseThrow(() -> new InvalidIdReferenceException(teamId, getClass().getName(), "Team"));
    return EmployeeAssessment.builder()
        .assessmentMatrixId(assessmentMatrix.orElseThrow(() -> new InvalidIdReferenceException(assessmentMatrixId, getClass().getName(), "AssessmentMatrix")).getId())
        .teamId(teamEntity.getId())
        .tenantId(teamEntity.getTenantId())
        .employee(createNaturalPerson(name, email, documentNumber, documentType, gender, genderPronoun, null))
        .employeeEmailNormalized(email.toLowerCase().trim())
        .answeredQuestionCount(0)
        .assessmentStatus(AssessmentStatus.INVITED)
        .build();
  }

  public static NaturalPerson createNaturalPerson(String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, Gender gender, GenderPronoun genderPronoun, String personId) {
    return NaturalPerson.builder()
        .id(personId)
        .name(name)
        .email(email)
        .documentNumber(documentNumber)
        .personDocumentType(documentType)
        .gender(gender)
        .genderPronoun(genderPronoun)
        .build();
  }

  public void incrementAnsweredQuestionCount(String employeeAssessmentId) {
    EmployeeAssessment employeeAssessment = getRepository().findById(employeeAssessmentId);
    if (employeeAssessment != null) {
      employeeAssessment.setAnsweredQuestionCount(employeeAssessment.getAnsweredQuestionCount() + 1);
      AssessmentStatus currentStatus = employeeAssessment.getAssessmentStatus();
      boolean statusChanged = false;
      
      if (currentStatus == null) {
        currentStatus = AssessmentStatus.INVITED;
        employeeAssessment.setAssessmentStatus(currentStatus);
      }
      if (employeeAssessment.getAnsweredQuestionCount() == 1 && currentStatus == AssessmentStatus.INVITED) {
        employeeAssessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);
        statusChanged = true;
      }
      getRepository().save(employeeAssessment);
      
      // Update lastActivityDate for status transition
      if (statusChanged) {
        updateLastActivityDate(employeeAssessmentId);
      }
    }
  }

  public Optional<EmployeeAssessment> updateAssessmentStatus(@NonNull String employeeAssessmentId, @NonNull AssessmentStatus status) {
    Optional<EmployeeAssessment> optionalEmployeeAssessment = findById(employeeAssessmentId);
    if (optionalEmployeeAssessment.isPresent()) {
      EmployeeAssessment employeeAssessment = optionalEmployeeAssessment.get();
      AssessmentStatus currentStatus = employeeAssessment.getAssessmentStatus();
      if (currentStatus == null) {
        currentStatus = AssessmentStatus.INVITED;
        employeeAssessment.setAssessmentStatus(currentStatus);
      }
      AssessmentStatusValidator.validateTransition(currentStatus, status);
      employeeAssessment.setAssessmentStatus(status);
      Optional<EmployeeAssessment> result = super.update(employeeAssessment);
      
      // Update lastActivityDate for status transitions (except when transitioning TO COMPLETED)
      // When transitioning TO COMPLETED, the lastActivityDate should remain as the completion timestamp
      if (result.isPresent() && status != AssessmentStatus.COMPLETED) {
        updateLastActivityDate(employeeAssessmentId);
      }
      return result;
    }
    return Optional.empty();
  }

  // TODO Remove this tenantId and refactor this code.
  public EmployeeAssessment updateEmployeeAssessmentScore(String employeeAssessmentId, String tenantId) {
    EmployeeAssessment employeeAssessment = getRepository().findById(employeeAssessmentId);

    if (employeeAssessment != null) {
      List<Answer> answers = retrieveAnswers(employeeAssessmentId, tenantId);
      EmployeeAssessmentScore employeeAssessmentScore = calculateEmployeeAssessmentScore(answers);
      employeeAssessment.setEmployeeAssessmentScore(employeeAssessmentScore);
      getRepository().save(employeeAssessment);
    }
    return employeeAssessment;
  }

  private List<Answer> retrieveAnswers(String employeeAssessmentId, String tenantId) {
    return answerRepository.findByEmployeeAssessmentId(employeeAssessmentId, tenantId);
  }

  private EmployeeAssessmentScore calculateEmployeeAssessmentScore(List<Answer> answers) {
    Map<String, List<Answer>> answersByPillar = groupAnswersByPillarId(answers);
    Map<String, PillarScore> pillarScores = calculatePillarScores(answersByPillar);

    EmployeeAssessmentScore employeeAssessmentScore = new EmployeeAssessmentScore();
    employeeAssessmentScore.setPillarIdToPillarScoreMap(pillarScores);
    employeeAssessmentScore.setScore(calculateTotalScore(pillarScores));
    return employeeAssessmentScore;
  }

  private Map<String, List<Answer>> groupAnswersByPillarId(List<Answer> answers) {
    return answers.stream().collect(Collectors.groupingBy(Answer::getPillarId));
  }

  private Map<String, PillarScore> calculatePillarScores(Map<String, List<Answer>> answersByPillar) {
    Map<String, PillarScore> pillarScores = new HashMap<>();

    for (Map.Entry<String, List<Answer>> entry : answersByPillar.entrySet()) {
      PillarScore pillarScore = calculatePillarScore(entry.getKey(), entry.getValue());
      pillarScores.put(entry.getKey(), pillarScore);
    }

    return pillarScores;
  }

  private PillarScore calculatePillarScore(String pillarId, List<Answer> answers) {
    Map<String, List<Answer>> answersByCategory = groupAnswersByCategory(answers);
    Map<String, CategoryScore> categoryScores = calculateCategoryScores(answersByCategory);

    PillarScore pillarScore = new PillarScore();
    pillarScore.setPillarId(pillarId);
    pillarScore.setPillarName(answers.get(0).getQuestion().getPillarName());
    pillarScore.setScore(calculateScoreForAnswers(answers));
    pillarScore.setCategoryIdToCategoryScoreMap(categoryScores);
    return pillarScore;
  }

  private Map<String, List<Answer>> groupAnswersByCategory(List<Answer> answers) {
    return answers.stream().collect(Collectors.groupingBy(Answer::getCategoryId));
  }

  private Map<String, CategoryScore> calculateCategoryScores(Map<String, List<Answer>> answersByCategory) {
    Map<String, CategoryScore> categoryScores = new HashMap<>();

    for (Map.Entry<String, List<Answer>> entry : answersByCategory.entrySet()) {
      CategoryScore categoryScore = calculateCategoryScore(entry.getKey(), entry.getValue());
      categoryScores.put(entry.getKey(), categoryScore);
    }

    return categoryScores;
  }

  private CategoryScore calculateCategoryScore(String categoryId, List<Answer> answers) {
    CategoryScore categoryScore = new CategoryScore();
    categoryScore.setCategoryId(categoryId);
    categoryScore.setCategoryName(answers.get(0).getQuestion().getCategoryName());
    categoryScore.setScore(calculateScoreForAnswers(answers));
    categoryScore.setQuestionScores(calculateQuestionScores(answers));
    return categoryScore;
  }

  private List<QuestionScore> calculateQuestionScores(List<Answer> answers) {
    List<QuestionScore> questionScores = new ArrayList<>();

    for (Answer answer : answers) {
      questionScores.add(createQuestionScore(answer));
    }

    return questionScores;
  }

  private QuestionScore createQuestionScore(Answer answer) {
    QuestionScore questionScore = new QuestionScore();
    questionScore.setQuestionId(answer.getQuestionId());
    questionScore.setScore(answer.getScore());
    return questionScore;
  }

  private double calculateScoreForAnswers(List<Answer> answers) {
    return answers.stream().mapToDouble(Answer::getScore).sum();
  }

  private double calculateTotalScore(Map<String, PillarScore> pillarScores) {
    return pillarScores.values().stream().mapToDouble(PillarScore::getScore).sum();
  }

  @Override
  AbstractCrudRepository<EmployeeAssessment> getRepository() {
    return employeeAssessmentRepository;
  }
  
  /**
   * Find all employee assessments by tenant ID
   */
  public List<EmployeeAssessment> findAllByTenantId(String tenantId) {
    return employeeAssessmentRepository.findAllByTenantId(tenantId).stream()
        .collect(Collectors.toList());
  }
  
  /**
   * Find all employee assessments by assessment matrix ID and tenant ID
   */
  public List<EmployeeAssessment> findByAssessmentMatrix(String assessmentMatrixId, String tenantId) {
    return employeeAssessmentRepository.findAllByTenantId(tenantId).stream()
        .filter(ea -> assessmentMatrixId.equals(ea.getAssessmentMatrixId()))
        .collect(Collectors.toList());
  }
  
  /**
   * Find employee assessment by ID and tenant ID
   */
  public Optional<EmployeeAssessment> findById(String id, String tenantId) {
    EmployeeAssessment ea = employeeAssessmentRepository.findById(id);
    if (ea != null && tenantId.equals(ea.getTenantId())) {
      return Optional.of(ea);
    }
    return Optional.empty();
  }
  
  /**
   * Delete employee assessment by ID
   */
  public void deleteById(String id) {
    EmployeeAssessment ea = employeeAssessmentRepository.findById(id);
    if (ea != null) {
      employeeAssessmentRepository.delete(ea);
    }
  }
  
  /**
   * Save employee assessment (create or update)
   */
  public EmployeeAssessment save(EmployeeAssessment employeeAssessment) {
    // Ensure normalized email is set for GSI
    Optional.ofNullable(employeeAssessment.getEmployee())
        .map(employee -> employee.getEmail())
        .filter(StringUtils::isNotBlank)
        .ifPresent(email -> employeeAssessment.setEmployeeEmailNormalized(email.toLowerCase().trim()));
    
    // For new assessments (no ID), validate employee assessment uniqueness
    if (employeeAssessment.getId() == null) {
      String email = Optional.ofNullable(employeeAssessment.getEmployee())
          .map(employee -> employee.getEmail())
          .orElseThrow(() -> new IllegalArgumentException("Employee email is required"));
      validateEmployeeAssessmentUniqueness(email, employeeAssessment.getAssessmentMatrixId());
    }
    return employeeAssessmentRepository.save(employeeAssessment);
  }
  
  /**
   * Validates that employee assessment does not already exist within the assessment matrix.
   * Uses efficient GSI query instead of expensive table scan.
   */
  private void validateEmployeeAssessmentUniqueness(String employeeEmail, String assessmentMatrixId) {
    boolean exists = employeeAssessmentRepository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrixId, employeeEmail);
    
    if (exists) {
      throw new EmployeeAssessmentAlreadyExistsException(employeeEmail, assessmentMatrixId);
    }
  }
  
  /**
   * Scan all EmployeeAssessments in the database (for maintenance/debugging purposes only)
   * WARNING: This is an expensive operation - use only for debugging or data migration
   */
  public List<EmployeeAssessment> scanAllEmployeeAssessments() {
    return employeeAssessmentRepository.getDynamoDBMapper()
        .scan(EmployeeAssessment.class, new com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression());
  }

  /**
   * Validates an employee for assessment access
   * Updates status from INVITED to CONFIRMED if applicable
   */
  public EmployeeValidationResponse validateEmployee(EmployeeValidationRequest request) {
    Optional<EmployeeAssessment> matchingAssessment = findEmployeeAssessment(request);

    if (!matchingAssessment.isPresent()) {
      return createEmployeeNotFoundResponse();
    }

    return handleEmployeeAssessmentValidation(matchingAssessment.get());
  }

  private void confirmEmployeeAssessment(EmployeeAssessment assessment) {
    assessment.setAssessmentStatus(AssessmentStatus.CONFIRMED);
    employeeAssessmentRepository.save(assessment);
    // Update lastActivityDate for status transition
    updateLastActivityDate(assessment.getId());
  }

  private EmployeeValidationResponse createEmployeeNotFoundResponse() {
    return EmployeeValidationResponse.error(
        "We couldn't find your assessment invitation. Please check that you're using the same email address that HR used to invite you, or contact your HR department for assistance."
                                           );
  }

  private Optional<EmployeeAssessment> findEmployeeAssessment(EmployeeValidationRequest request) {
    List<EmployeeAssessment> assessments = findByAssessmentMatrix(
        request.getAssessmentMatrixId(), request.getTenantId());

    return assessments.stream()
        .filter(assessment -> isEmailMatch(assessment, request.getEmail()))
        .findFirst();
  }

  private EmployeeValidationResponse handleActiveStatus(EmployeeAssessment assessment, AssessmentStatus currentStatus) {
    return EmployeeValidationResponse.info(
        "Welcome back! You can continue your assessment where you left off.",
        assessment.getId(),
        assessment.getEmployee().getName(),
        currentStatus.toString()
                                          );
  }

  private EmployeeValidationResponse handleCompletedStatus(EmployeeAssessment assessment) {
    return EmployeeValidationResponse.info(
        "You have already completed this assessment. Thank you for your participation!",
        assessment.getId(),
        assessment.getEmployee().getName(),
        AssessmentStatus.COMPLETED.toString()
                                          );
  }

  private EmployeeValidationResponse handleEmployeeAssessmentValidation(EmployeeAssessment assessment) {
    AssessmentStatus currentStatus = assessment.getAssessmentStatus();

    switch (currentStatus) {
      case INVITED:
        return handleInvitedStatus(assessment);
      case CONFIRMED:
      case IN_PROGRESS:
        return handleActiveStatus(assessment, currentStatus);
      case COMPLETED:
        return handleCompletedStatus(assessment);
      default:
        return handleUnknownStatus(assessment, currentStatus);
    }
  }

  private EmployeeValidationResponse handleInvitedStatus(EmployeeAssessment assessment) {
    confirmEmployeeAssessment(assessment);

    return EmployeeValidationResponse.success(
        "Welcome! Your assessment access has been confirmed.",
        assessment.getId(),
        assessment.getEmployee().getName(),
        AssessmentStatus.CONFIRMED.toString()
                                             );
  }

  private EmployeeValidationResponse handleUnknownStatus(EmployeeAssessment assessment, AssessmentStatus currentStatus) {
    return EmployeeValidationResponse.info(
        "Your assessment is in status: " + currentStatus,
        assessment.getId(),
        assessment.getEmployee().getName(),
        currentStatus.toString()
                                          );
  }

  private boolean isEmailMatch(EmployeeAssessment assessment, String email) {
    return assessment.getEmployee().getEmail().equalsIgnoreCase(email);
  }

  /**
   * Updates the lastActivityDate for an employee assessment.
   * This method should only be called for assessments that are not COMPLETED.
   * 
   * @param employeeAssessmentId The employee assessment ID to update
   */
  public void updateLastActivityDate(@NonNull String employeeAssessmentId) {
    EmployeeAssessment employeeAssessment = getRepository().findById(employeeAssessmentId);
    if (employeeAssessment != null && employeeAssessment.getAssessmentStatus() != AssessmentStatus.COMPLETED) {
      employeeAssessment.setLastActivityDate(new java.util.Date());
      getRepository().save(employeeAssessment);
    }
  }
}
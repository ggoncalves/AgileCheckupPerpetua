package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessmentScore;
import com.agilecheckup.persistency.entity.EmployeeAssessmentV2;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.QuestionScore;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepositoryV2;
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

public class EmployeeAssessmentServiceV2 extends AbstractCrudServiceV2<EmployeeAssessmentV2, EmployeeAssessmentRepositoryV2> {

    private final AssessmentMatrixServiceV2 assessmentMatrixServiceV2;

    private final TeamService teamService;

    private final EmployeeAssessmentRepositoryV2 employeeAssessmentRepositoryV2;

    private final AnswerRepository answerRepository;

    @Inject
    public EmployeeAssessmentServiceV2(EmployeeAssessmentRepositoryV2 employeeAssessmentRepositoryV2, 
                                     AssessmentMatrixServiceV2 assessmentMatrixServiceV2, 
                                     TeamService teamService,
                                     AnswerRepository answerRepository) {
        this.employeeAssessmentRepositoryV2 = employeeAssessmentRepositoryV2;
        this.assessmentMatrixServiceV2 = assessmentMatrixServiceV2;
        this.teamService = teamService;
        this.answerRepository = answerRepository;
    }

    @Override
    public EmployeeAssessmentRepositoryV2 getRepository() {
        return employeeAssessmentRepositoryV2;
    }

    public Optional<EmployeeAssessmentV2> create(@NonNull String assessmentMatrixId, String teamId, String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, Gender gender, GenderPronoun genderPronoun) {
        validateEmployeeAssessmentUniqueness(email, assessmentMatrixId);
        return super.create(createEmployeeAssessment(assessmentMatrixId, teamId, name, email, documentNumber, documentType, gender, genderPronoun));
    }

    public Optional<EmployeeAssessmentV2> update(@NonNull String id, @NonNull String assessmentMatrixId, String teamId, String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, Gender gender, GenderPronoun genderPronoun) {
        Optional<EmployeeAssessmentV2> optionalEmployeeAssessment = findById(id);
        if (optionalEmployeeAssessment.isPresent()) {
            EmployeeAssessmentV2 employeeAssessment = optionalEmployeeAssessment.get();
            Optional<AssessmentMatrixV2> assessmentMatrix = assessmentMatrixServiceV2.findById(assessmentMatrixId);
            AssessmentMatrixV2 assessmentMatrixEntity = assessmentMatrix.orElseThrow(() -> new InvalidIdReferenceException(assessmentMatrixId, getClass().getName(), "AssessmentMatrix"));
            
            employeeAssessment.setAssessmentMatrixId(assessmentMatrixEntity.getId());
            
            if (StringUtils.isNotBlank(teamId)) {
                Optional<Team> team = teamService.findById(teamId);
                Team teamEntity = team.orElseThrow(() -> new InvalidIdReferenceException(teamId, getClass().getName(), "Team"));
                employeeAssessment.setTeamId(teamEntity.getId());
            } else {
                employeeAssessment.setTeamId(null);
            }
            employeeAssessment.setEmployee(createNaturalPerson(name, email, documentNumber, documentType, gender, genderPronoun, employeeAssessment.getEmployee().getId()));
            employeeAssessment.setEmployeeEmailNormalized(email.toLowerCase().trim());
            return super.update(employeeAssessment);
        } else {
            return Optional.empty();
        }
    }

    private EmployeeAssessmentV2 createEmployeeAssessment(@NonNull String assessmentMatrixId, String teamId, String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, Gender gender, GenderPronoun genderPronoun) {
        Optional<AssessmentMatrixV2> assessmentMatrix = assessmentMatrixServiceV2.findById(assessmentMatrixId);
        AssessmentMatrixV2 assessmentMatrixEntity = assessmentMatrix.orElseThrow(() -> new InvalidIdReferenceException(assessmentMatrixId, getClass().getName(), "AssessmentMatrix"));
        
        String finalTeamId = null;
        String tenantId = assessmentMatrixEntity.getTenantId();
        
        if (StringUtils.isNotBlank(teamId)) {
            Optional<Team> team = teamService.findById(teamId);
            Team teamEntity = team.orElseThrow(() -> new InvalidIdReferenceException(teamId, getClass().getName(), "Team"));
            finalTeamId = teamEntity.getId();
        }
        
        return EmployeeAssessmentV2.builder()
            .assessmentMatrixId(assessmentMatrixEntity.getId())
            .teamId(finalTeamId)
            .tenantId(tenantId)
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
        Optional<EmployeeAssessmentV2> optionalEmployeeAssessment = getRepository().findById(employeeAssessmentId);
        if (optionalEmployeeAssessment.isPresent()) {
            EmployeeAssessmentV2 employeeAssessment = optionalEmployeeAssessment.get();
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

    public Optional<EmployeeAssessmentV2> updateAssessmentStatus(@NonNull String employeeAssessmentId, @NonNull AssessmentStatus status) {
        Optional<EmployeeAssessmentV2> optionalEmployeeAssessment = findById(employeeAssessmentId);
        if (optionalEmployeeAssessment.isPresent()) {
            EmployeeAssessmentV2 employeeAssessment = optionalEmployeeAssessment.get();
            AssessmentStatus currentStatus = employeeAssessment.getAssessmentStatus();
            if (currentStatus == null) {
                currentStatus = AssessmentStatus.INVITED;
                employeeAssessment.setAssessmentStatus(currentStatus);
            }
            AssessmentStatusValidator.validateTransition(currentStatus, status);
            employeeAssessment.setAssessmentStatus(status);
            Optional<EmployeeAssessmentV2> result = super.update(employeeAssessment);
            
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
    public EmployeeAssessmentV2 updateEmployeeAssessmentScore(String employeeAssessmentId, String tenantId) {
        Optional<EmployeeAssessmentV2> optionalEmployeeAssessment = getRepository().findById(employeeAssessmentId);
        
        if (optionalEmployeeAssessment.isPresent()) {
            EmployeeAssessmentV2 employeeAssessment = optionalEmployeeAssessment.get();
            List<Answer> answers = retrieveAnswers(employeeAssessmentId, tenantId);
            EmployeeAssessmentScore employeeAssessmentScore = calculateEmployeeAssessmentScore(answers);
            employeeAssessment.setEmployeeAssessmentScore(employeeAssessmentScore);
            getRepository().save(employeeAssessment);
            return employeeAssessment;
        }
        return null;
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
    
    /**
     * Find all employee assessments by tenant ID
     */
    public List<EmployeeAssessmentV2> findAllByTenantId(String tenantId) {
        return employeeAssessmentRepositoryV2.findAllByTenantId(tenantId);
    }
    
    /**
     * Find all employee assessments by assessment matrix ID and tenant ID
     */
    public List<EmployeeAssessmentV2> findByAssessmentMatrix(String assessmentMatrixId, String tenantId) {
        return employeeAssessmentRepositoryV2.findByAssessmentMatrixId(assessmentMatrixId, tenantId);
    }
    
    /**
     * Find employee assessment by ID and tenant ID
     */
    public Optional<EmployeeAssessmentV2> findById(String id, String tenantId) {
        Optional<EmployeeAssessmentV2> optionalEa = employeeAssessmentRepositoryV2.findById(id);
        if (optionalEa.isPresent() && tenantId.equals(optionalEa.get().getTenantId())) {
            return optionalEa;
        }
        return Optional.empty();
    }
    
    /**
     * Delete employee assessment by ID
     */
    @Override
    public boolean deleteById(String id) {
        return employeeAssessmentRepositoryV2.deleteById(id);
    }
    
    /**
     * Save employee assessment (create or update)
     */
    public EmployeeAssessmentV2 save(EmployeeAssessmentV2 employeeAssessment) {
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
        return employeeAssessmentRepositoryV2.save(employeeAssessment).orElse(employeeAssessment);
    }
    
    /**
     * Validates that employee assessment does not already exist within the assessment matrix.
     * Uses efficient GSI query instead of expensive table scan.
     */
    private void validateEmployeeAssessmentUniqueness(String employeeEmail, String assessmentMatrixId) {
        boolean exists = employeeAssessmentRepositoryV2.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrixId, employeeEmail);
        
        if (exists) {
            throw new EmployeeAssessmentAlreadyExistsException(employeeEmail, assessmentMatrixId);
        }
    }
    
    /**
     * Scan all EmployeeAssessments in the database (for maintenance/debugging purposes only)
     * WARNING: This is an expensive operation - use only for debugging or data migration
     */
    public List<EmployeeAssessmentV2> scanAllEmployeeAssessments() {
        return employeeAssessmentRepositoryV2.findAll();
    }

    /**
     * Validates an employee for assessment access
     * Updates status from INVITED to CONFIRMED if applicable
     */
    public EmployeeValidationResponse validateEmployee(EmployeeValidationRequest request) {
        Optional<EmployeeAssessmentV2> matchingAssessment = findEmployeeAssessment(request);

        if (!matchingAssessment.isPresent()) {
            return createEmployeeNotFoundResponse();
        }

        return handleEmployeeAssessmentValidation(matchingAssessment.get());
    }

    private void confirmEmployeeAssessment(EmployeeAssessmentV2 assessment) {
        assessment.setAssessmentStatus(AssessmentStatus.CONFIRMED);
        employeeAssessmentRepositoryV2.save(assessment);
        // Update lastActivityDate for status transition
        updateLastActivityDate(assessment.getId());
    }

    private EmployeeValidationResponse createEmployeeNotFoundResponse() {
        return EmployeeValidationResponse.error(
            "We couldn't find your assessment invitation. Please check that you're using the same email address that HR used to invite you, or contact your HR department for assistance."
        );
    }

    private Optional<EmployeeAssessmentV2> findEmployeeAssessment(EmployeeValidationRequest request) {
        List<EmployeeAssessmentV2> assessments = findByAssessmentMatrix(
            request.getAssessmentMatrixId(), request.getTenantId());

        return assessments.stream()
            .filter(assessment -> isEmailMatch(assessment, request.getEmail()))
            .findFirst();
    }

    private EmployeeValidationResponse handleActiveStatus(EmployeeAssessmentV2 assessment, AssessmentStatus currentStatus) {
        return EmployeeValidationResponse.info(
            "Welcome back! You can continue your assessment where you left off.",
            assessment.getId(),
            assessment.getEmployee().getName(),
            currentStatus.toString()
        );
    }

    private EmployeeValidationResponse handleCompletedStatus(EmployeeAssessmentV2 assessment) {
        return EmployeeValidationResponse.info(
            "You have already completed this assessment. Thank you for your participation!",
            assessment.getId(),
            assessment.getEmployee().getName(),
            AssessmentStatus.COMPLETED.toString()
        );
    }

    private EmployeeValidationResponse handleEmployeeAssessmentValidation(EmployeeAssessmentV2 assessment) {
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

    private EmployeeValidationResponse handleInvitedStatus(EmployeeAssessmentV2 assessment) {
        confirmEmployeeAssessment(assessment);

        return EmployeeValidationResponse.success(
            "Welcome! Your assessment access has been confirmed.",
            assessment.getId(),
            assessment.getEmployee().getName(),
            AssessmentStatus.CONFIRMED.toString()
        );
    }

    private EmployeeValidationResponse handleUnknownStatus(EmployeeAssessmentV2 assessment, AssessmentStatus currentStatus) {
        return EmployeeValidationResponse.info(
            "Your assessment is in status: " + currentStatus,
            assessment.getId(),
            assessment.getEmployee().getName(),
            currentStatus.toString()
        );
    }

    private boolean isEmailMatch(EmployeeAssessmentV2 assessment, String email) {
        return assessment.getEmployee().getEmail().equalsIgnoreCase(email);
    }

    /**
     * Updates the lastActivityDate for an employee assessment.
     * This method should only be called for assessments that are not COMPLETED.
     * 
     * @param employeeAssessmentId The employee assessment ID to update
     */
    public void updateLastActivityDate(@NonNull String employeeAssessmentId) {
        Optional<EmployeeAssessmentV2> optionalEmployeeAssessment = getRepository().findById(employeeAssessmentId);
        if (optionalEmployeeAssessment.isPresent()) {
            EmployeeAssessmentV2 employeeAssessment = optionalEmployeeAssessment.get();
            if (employeeAssessment.getAssessmentStatus() != AssessmentStatus.COMPLETED) {
                employeeAssessment.setLastActivityDate(new java.util.Date());
                getRepository().save(employeeAssessment);
            }
        }
    }

    @Override
    protected void postCreate(EmployeeAssessmentV2 entity) {
        // Hook for post-create actions
    }

    @Override
    protected void postUpdate(EmployeeAssessmentV2 entity) {
        // Hook for post-update actions
    }

    @Override
    protected void postDelete(String id) {
        // Hook for post-delete actions
    }
}
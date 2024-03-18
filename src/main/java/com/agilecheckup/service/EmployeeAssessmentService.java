package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
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
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import lombok.NonNull;

import javax.inject.Inject;
import java.util.*;
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

  public Optional<EmployeeAssessment> create(@NonNull String assessmentMatrixId, String teamId, String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, @NonNull Gender gender, @NonNull GenderPronoun genderPronoun) {
    return super.create(createEmployeeAssessment(assessmentMatrixId, teamId, name, email, documentNumber, documentType, gender, genderPronoun, null));
  }

  private EmployeeAssessment createEmployeeAssessment(@NonNull String assessmentMatrixId, String teamId, String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, @NonNull Gender gender, @NonNull GenderPronoun genderPronoun, String personId) {
    Optional<AssessmentMatrix> assessmentMatrix = assessmentMatrixService.findById(assessmentMatrixId);
    Optional<Team> team = teamService.findById(teamId);
    EmployeeAssessment employeeAssessment = EmployeeAssessment.builder()
        .assessmentMatrixId(assessmentMatrix.orElseThrow(() -> new InvalidIdReferenceException(assessmentMatrixId, getClass().getName(), "AssessmentMatrix")).getId())
        .team(team.orElseThrow(() -> new InvalidIdReferenceException(teamId, getClass().getName(), "Team")))
        .employee(createNaturalPerson(name, email, documentNumber, documentType, gender, genderPronoun, personId))
        .answeredQuestionCount(0)
        .build();
    return setFixedIdIfConfigured(employeeAssessment);
  }

  public static NaturalPerson createNaturalPerson(String name, @NonNull String email, String documentNumber, PersonDocumentType documentType, @NonNull Gender gender, @NonNull GenderPronoun genderPronoun, String personId) {
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
      getRepository().save(employeeAssessment);
    }
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
}
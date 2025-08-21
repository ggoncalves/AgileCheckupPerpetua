package com.agilecheckup.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.agilecheckup.persistency.entity.AssessmentConfiguration;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionNavigationType;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.PotentialScore;
import com.agilecheckup.persistency.entity.score.QuestionScore;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepository;
import com.agilecheckup.security.JwtTokenProvider;
import com.agilecheckup.security.TenantAccessValidator;
import com.agilecheckup.service.dto.AssessmentDashboardData;
import com.agilecheckup.service.dto.EmployeeAssessmentSummary;
import com.agilecheckup.service.dto.TeamAssessmentSummary;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.google.common.annotations.VisibleForTesting;

import dagger.Lazy;

public class AssessmentMatrixService extends AbstractCrudService<AssessmentMatrix, AssessmentMatrixRepository> {

  private static final String DEFAULT_WHEN_NULL = "";
  private final AssessmentMatrixRepository assessmentMatrixRepository;
  private final PerformanceCycleService performanceCycleService;
  private final Lazy<QuestionService> questionService;
  private final Lazy<EmployeeAssessmentService> employeeAssessmentService;
  private final Lazy<TeamService> teamService;
  private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();

  @Inject
  public AssessmentMatrixService(AssessmentMatrixRepository assessmentMatrixRepository, PerformanceCycleService performanceCycleService, Lazy<QuestionService> questionService, Lazy<EmployeeAssessmentService> employeeAssessmentService, Lazy<TeamService> teamService) {
    this.assessmentMatrixRepository = assessmentMatrixRepository;
    this.performanceCycleService = performanceCycleService;
    this.questionService = questionService;
    this.employeeAssessmentService = employeeAssessmentService;
    this.teamService = teamService;
  }

  @Override
  AssessmentMatrixRepository getRepository() {
    return assessmentMatrixRepository;
  }

  @VisibleForTesting
  protected QuestionService getQuestionService() {
    return questionService.get();
  }

  @VisibleForTesting
  protected EmployeeAssessmentService getEmployeeAssessmentService() {
    return employeeAssessmentService.get();
  }

  @VisibleForTesting
  protected TeamService getTeamService() {
    return teamService.get();
  }

  public Optional<AssessmentMatrix> create(String name, String description, String tenantId, String performanceCycleId, Map<String, Pillar> pillarMap) {
    return create(name, description, tenantId, performanceCycleId, pillarMap, null);
  }

  public Optional<AssessmentMatrix> create(String name, String description, String tenantId, String performanceCycleId, Map<String, Pillar> pillarMap, AssessmentConfiguration configuration) {
    return super.create(createAssessmentMatrix(name, description, tenantId, performanceCycleId, pillarMap, configuration));
  }

  public Optional<AssessmentMatrix> update(String id, String name, String description, String tenantId, String performanceCycleId, Map<String, Pillar> pillarMap) {
    return update(id, name, description, tenantId, performanceCycleId, pillarMap, null);
  }

  public Optional<AssessmentMatrix> update(String id, String name, String description, String tenantId, String performanceCycleId, Map<String, Pillar> pillarMap, AssessmentConfiguration configuration) {
    Optional<AssessmentMatrix> optionalAssessmentMatrix = findById(id);
    if (optionalAssessmentMatrix.isPresent()) {
      AssessmentMatrix assessmentMatrix = optionalAssessmentMatrix.get();
      assessmentMatrix.setName(name);
      assessmentMatrix.setDescription(description);
      assessmentMatrix.setTenantId(tenantId);
      assessmentMatrix.setPerformanceCycleId(performanceCycleId);
      assessmentMatrix.setPillarMap(pillarMap);
      assessmentMatrix.setConfiguration(configuration);
      return super.update(assessmentMatrix);
    }
    return Optional.empty();
  }

  public AssessmentMatrix incrementQuestionCount(String matrixId) {
    Optional<AssessmentMatrix> optionalMatrix = findById(matrixId);
    if (optionalMatrix.isPresent()) {
      AssessmentMatrix matrix = optionalMatrix.get();
      int currentCount = matrix.getQuestionCount() != null ? matrix.getQuestionCount() : 0;
      matrix.setQuestionCount(currentCount + 1);
      return super.update(matrix).orElse(matrix);
    }
    throw new RuntimeException("Matrix not found: " + matrixId);
  }

  public AssessmentMatrix decrementQuestionCount(String matrixId) {
    Optional<AssessmentMatrix> optionalMatrix = findById(matrixId);
    if (optionalMatrix.isPresent()) {
      AssessmentMatrix matrix = optionalMatrix.get();
      int currentCount = matrix.getQuestionCount() != null ? matrix.getQuestionCount() : 0;
      matrix.setQuestionCount(Math.max(0, currentCount - 1));
      return super.update(matrix).orElse(matrix);
    }
    throw new RuntimeException("Matrix not found: " + matrixId);
  }

  public AssessmentConfiguration createDefaultConfiguration() {
    return AssessmentConfiguration.builder()
                                  .allowQuestionReview(true)
                                  .requireAllQuestions(true)
                                  .autoSave(true)
                                  .navigationMode(QuestionNavigationType.RANDOM)
                                  .build();
  }

  public String generateInvitationToken(String tenantId, String assessmentMatrixId, boolean lockMatrixAndRecalculateScore) {
    AssessmentMatrix assessmentMatrix = getAssessmentMatrix(assessmentMatrixId, tenantId);
    if (lockMatrixAndRecalculateScore) {
      lockMatrixAndRecalculateScore(assessmentMatrix);
    }
    return jwtTokenProvider.generateInvitationToken(tenantId, assessmentMatrixId);
  }

  public void lockMatrixAndRecalculateScore(AssessmentMatrix assessmentMatrix) {
    assessmentMatrix.setIsLocked(true);
    updateCurrentPotentialScore(assessmentMatrix);
  }

  public AssessmentConfiguration getEffectiveConfiguration(AssessmentMatrix assessmentMatrix) {
    AssessmentConfiguration configuration = assessmentMatrix.getConfiguration();
    if (configuration == null) {
      return createDefaultConfiguration();
    }
    return configuration;
  }

  public List<AssessmentMatrix> findAllByTenantId(String tenantId) {
    return getRepository().findAllByTenantId(tenantId);
  }

  private AssessmentMatrix getAssessmentMatrix(String matrixId, String tenantId) {
    AssessmentMatrix assessmentMatrix = getRepository().findById(matrixId)
                                                       .orElseThrow(() -> new InvalidIdReferenceException(matrixId, "AssessmentMatrix"));

    TenantAccessValidator.validateTenantAccess(tenantId, assessmentMatrix);

    return assessmentMatrix;
  }

  public AssessmentMatrix updateCurrentPotentialScore(String matrixId, String tenantId) {
    AssessmentMatrix assessmentMatrix = getAssessmentMatrix(matrixId, tenantId);
    return updateCurrentPotentialScore(assessmentMatrix);
  }

  private AssessmentMatrix updateCurrentPotentialScore(AssessmentMatrix matrix) {
    List<Question> questions = getQuestionService().findByAssessmentMatrixId(matrix.getId(), matrix.getTenantId());

    int estimatedPillars = Math.max(8, Math.min(50, questions.size() / 10));
    Map<String, PillarScore> pillarIdToPillarScoreMap = new HashMap<>(estimatedPillars);

    Map<String, List<Question>> questionsByPillar = questions.stream()
                                                             .collect(Collectors.groupingBy(Question::getPillarId));

    // Calculate the maximum possible total score from the retrieved questions
    double totalPoints = 0.0;

    // Process questions in batches by pillar for better performance
    for (Map.Entry<String, List<Question>> pillarEntry : questionsByPillar.entrySet()) {
      String pillarId = pillarEntry.getKey();
      List<Question> pillarQuestions = pillarEntry.getValue();

      // Create PillarScore once per pillar
      Question firstQuestion = pillarQuestions.get(0);

      int estimatedCategories = Math.max(4, Math.min(20, pillarQuestions.size() / 5));
      PillarScore pillarScore = PillarScore.builder()
                                           .pillarId(pillarId)
                                           .pillarName(firstQuestion.getPillarName())
                                           .categoryIdToCategoryScoreMap(new HashMap<>(estimatedCategories))
                                           .build();

      // Process all questions for this pillar
      for (Question question : pillarQuestions) {
        updatePillarScoreWithQuestionOptimized(pillarScore, question);
        totalPoints += computeQuestionMaxScore(question);
      }

      pillarIdToPillarScoreMap.put(pillarId, pillarScore);
    }

    // Update the PotentialScore of the AssessmentMatrix
    PotentialScore potentialScore = matrix.getPotentialScore();
    if (potentialScore == null) {
      potentialScore = PotentialScore.builder().build();
      matrix.setPotentialScore(potentialScore);
    }
    potentialScore.setPillarIdToPillarScoreMap(pillarIdToPillarScoreMap);
    potentialScore.setScore(totalPoints);

    // Save the updated AssessmentMatrix
    return super.update(matrix).orElse(matrix);
  }

  public Optional<AssessmentDashboardData> getAssessmentDashboard(String matrixId, String tenantId) {
    Optional<AssessmentMatrix> optionalMatrix = findById(matrixId);
    if (optionalMatrix.isEmpty()) {
      return Optional.empty();
    }

    AssessmentMatrix matrix = optionalMatrix.get();

    // Check if the matrix belongs to the requested tenant
    if (!matrix.getTenantId().equals(tenantId)) {
      return Optional.empty();
    }

    // Get all employee assessments for this matrix using existing GSI
    // This is cost-effective as it uses assessmentMatrixId-employeeEmail-index
    List<EmployeeAssessment> employeeAssessments = getEmployeeAssessmentService().findByAssessmentMatrix(matrixId, tenantId);

    // Create employee summaries from  entities
    List<EmployeeAssessmentSummary> employeeSummaries = createEmployeeSummaries(employeeAssessments);

    // Create team summaries from  entities
    List<TeamAssessmentSummary> teamSummaries = createTeamSummaries(employeeAssessments);

    // Calculate completion statistics
    int completedCount = calculateCompletedCount(employeeAssessments);

    AssessmentDashboardData result = AssessmentDashboardData.builder()
                                                            .assessmentMatrixId(matrixId)
                                                            .matrixName(matrix.getName())
                                                            .potentialScore(matrix.getPotentialScore())
                                                            .teamSummaries(teamSummaries)
                                                            .employeeSummaries(employeeSummaries)
                                                            .totalEmployees(employeeAssessments.size())
                                                            .completedAssessments(completedCount)
                                                            .build();

    return Optional.of(result);
  }

  private List<EmployeeAssessmentSummary> createEmployeeSummaries(List<EmployeeAssessment> employeeAssessments) {
    return employeeAssessments.stream().map(this::convertToEmployeeSummary).collect(Collectors.toList());
  }

  private List<TeamAssessmentSummary> createTeamSummaries(List<EmployeeAssessment> employeeAssessments) {
    // Group assessments by team
    Map<String, List<EmployeeAssessment>> assessmentsByTeam = employeeAssessments.stream()
                                                                                 .filter(assessment -> assessment.getTeamId() != null)
                                                                                 .collect(Collectors.groupingBy(EmployeeAssessment::getTeamId));

    return assessmentsByTeam.entrySet()
                            .stream()
                            .map(entry -> createTeamSummary(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
  }

  private EmployeeAssessmentSummary convertToEmployeeSummary(EmployeeAssessment assessment) {
    return EmployeeAssessmentSummary.builder()
                                    .employeeAssessmentId(assessment.getId())
                                    .employeeId(assessment.getEmployee() != null ? assessment.getEmployee()
                                                                                             .getId() : null)
                                    .employeeName(assessment.getEmployee() != null ? assessment.getEmployee()
                                                                                               .getName() : "Unknown")
                                    .employeeEmail(assessment.getEmployee() != null ? assessment.getEmployee()
                                                                                                .getEmail() : "Unknown")
                                    .teamId(assessment.getTeamId())
                                    .teamName(getTeamName(assessment.getTeamId()))
                                    .assessmentStatus(assessment.getAssessmentStatus())
                                    .currentScore(assessment.getEmployeeAssessmentScore() != null ? assessment.getEmployeeAssessmentScore()
                                                                                                              .getScore() : null)
                                    .answeredQuestionCount(assessment.getAnsweredQuestionCount())
                                    .lastActivityDate(convertToLocalDateTime(assessment.getLastActivityDate()))
                                    .build();
  }

  private TeamAssessmentSummary createTeamSummary(String teamId, List<EmployeeAssessment> teamAssessments) {
    int totalEmployees = teamAssessments.size();
    int completedAssessments = (int) teamAssessments.stream().filter(EmployeeAssessment::isCompleted).count();

    double completionPercentage = totalEmployees > 0 ? (double) completedAssessments / totalEmployees * 100.0 : 0.0;

    double averageScore = teamAssessments.stream()
                                         .filter(assessment -> assessment.getEmployeeAssessmentScore() != null && assessment.getEmployeeAssessmentScore()
                                                                                                                            .getScore() != null)
                                         .mapToDouble(assessment -> assessment.getEmployeeAssessmentScore().getScore())
                                         .average()
                                         .orElse(0.0);

    return TeamAssessmentSummary.builder()
                                .teamId(teamId)
                                .teamName(getTeamName(teamId))
                                .totalEmployees(totalEmployees)
                                .completedAssessments(completedAssessments)
                                .completionPercentage(completionPercentage)
                                .averageScore(averageScore > 0 ? averageScore : null)
                                .build();
  }

  private String getTeamName(String teamId) {
    if (teamId == null) return "No Team";
    try {
      Optional<Team> team = getTeamService().findById(teamId);
      return team.map(Team::getName).orElse("Unknown Team");
    }
    catch (Exception e) {
      return "Unknown Team";
    }
  }

  private LocalDateTime convertToLocalDateTime(Date date) {
    if (date == null) return null;
    return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
  }

  private AssessmentMatrix createAssessmentMatrix(String name, String description, String tenantId, String performanceCycleId, Map<String, Pillar> pillarMap, AssessmentConfiguration configuration) {
    validatePerformanceCycle(performanceCycleId, tenantId);

    return AssessmentMatrix.builder()
                           .name(name)
                           .description(description)
                           .tenantId(tenantId)
                           .performanceCycleId(performanceCycleId)
                           .pillarMap(pillarMap)
                           .questionCount(0)
                           .configuration(configuration)
                           .build();
  }

  private void validatePerformanceCycle(String performanceCycleId, String tenantId) {
    if (StringUtils.isNotBlank(performanceCycleId)) {
      Optional<PerformanceCycle> performanceCycle = performanceCycleService.findById(performanceCycleId);
      if (performanceCycle.isEmpty() || !performanceCycle.get().getTenantId().equals(tenantId)) {
        throw new InvalidIdReferenceException("PerformanceCycle not found or does not belong to tenant: " + performanceCycleId);
      }
    }
  }

  private void updatePillarScoreWithQuestion(PillarScore pillarScore, Question question) {
    // Assuming each CategoryScore can be identified by the category ID in the question
    String categoryId = question.getCategoryId();
    CategoryScore categoryScore = pillarScore.getCategoryIdToCategoryScoreMap()
                                             .computeIfAbsent(categoryId, id -> CategoryScore.builder()
                                                                                             .categoryId(id)
                                                                                             .categoryName(question.getCategoryName())
                                                                                             .questionScores(new ArrayList<>())
                                                                                             .build()
                                             );

    // Create and add QuestionScore to the questionScores list in CategoryScore
    QuestionScore questionScore = QuestionScore.builder()
                                               .questionId(question.getId())
                                               .score(computeQuestionMaxScore(question))
                                               .build();
    categoryScore.getQuestionScores().add(questionScore);

    // Update the maxCategoryScore in CategoryScore
    categoryScore.setScore(categoryScore.getQuestionScores().stream().mapToDouble(QuestionScore::getScore).sum());

    // Update the maxPillarScore in PillarScore
    pillarScore.setScore(pillarScore.getCategoryIdToCategoryScoreMap()
                                    .values()
                                    .stream()
                                    .mapToDouble(CategoryScore::getScore)
                                    .sum());
  }

  // Quick Win 1: Optimized version that avoids O(n²) recalculation
  private void updatePillarScoreWithQuestionOptimized(PillarScore pillarScore, Question question) {
    String categoryId = question.getCategoryId();
    CategoryScore categoryScore = pillarScore.getCategoryIdToCategoryScoreMap()
                                             .computeIfAbsent(categoryId, id -> CategoryScore.builder()
                                                                                             .categoryId(id)
                                                                                             .categoryName(question.getCategoryName())
                                                                                             .questionScores(new ArrayList<>())
                                                                                             .score(0.0)
                                                                                             .build()
                                             );

    // Create and add QuestionScore to the questionScores list in CategoryScore
    double questionMaxScore = computeQuestionMaxScore(question);
    QuestionScore questionScore = QuestionScore.builder()
                                               .questionId(question.getId())
                                               .score(questionMaxScore)
                                               .build();
    categoryScore.getQuestionScores().add(questionScore);

    // Quick Win 1: Incremental addition instead of full recalculation
    double currentCategoryScore = categoryScore.getScore() != null ? categoryScore.getScore() : 0.0;
    categoryScore.setScore(currentCategoryScore + questionMaxScore);

    // Quick Win 1: Incremental addition for pillar score
    double currentPillarScore = pillarScore.getScore() != null ? pillarScore.getScore() : 0.0;
    pillarScore.setScore(currentPillarScore + questionMaxScore);
  }

  @VisibleForTesting
  Double computeQuestionMaxScore(Question question) {
    if (QuestionType.CUSTOMIZED.equals(question.getQuestionType())) {
      if (question.getOptionGroup().isMultipleChoice()) {
        return question.getOptionGroup().getOptionMap().values().stream().mapToDouble(QuestionOption::getPoints).sum();
      }
      else {
        return question.getOptionGroup()
                       .getOptionMap()
                       .values()
                       .stream()
                       .mapToDouble(QuestionOption::getPoints)
                       .max()
                       .orElse(0);
      }
    }
    else {
      return question.getPoints();
    }
  }

  // Note: Summary building methods removed due to V1 DTO deletion
  // These methods (buildEmployeeSummaries, buildTeamSummaries, etc.) can be restored
  // once  DTOs are created for EmployeeAssessmentSummary and TeamAssessmentSummary

  /**
   * Counts completed assessments efficiently.
   */
  private int calculateCompletedCount(List<EmployeeAssessment> assessments) {
    return (int) assessments.stream().filter(EmployeeAssessment::isCompleted).count();
  }

  @Override
  protected void postCreate(AssessmentMatrix entity) {
    // Hook for post-create actions
  }

  @Override
  protected void postUpdate(AssessmentMatrix entity) {
    // Hook for post-update actions
  }

  @Override
  protected void postDelete(String id) {
    // Hook for post-delete actions
  }
}

package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentConfiguration;
import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.CategoryV2;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.PerformanceCycleV2;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.persistency.entity.QuestionNavigationType;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.PotentialScore;
import com.agilecheckup.persistency.entity.score.QuestionScore;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepositoryV2;
import com.agilecheckup.service.dto.AssessmentDashboardData;
import com.agilecheckup.service.dto.EmployeeAssessmentSummary;
import com.agilecheckup.service.dto.TeamAssessmentSummary;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.agilecheckup.service.exception.ValidationException;
import com.google.common.annotations.VisibleForTesting;
import dagger.Lazy;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AssessmentMatrixServiceV2 extends AbstractCrudServiceV2<AssessmentMatrixV2, AssessmentMatrixRepositoryV2> {

    private final AssessmentMatrixRepositoryV2 assessmentMatrixRepositoryV2;

    private final PerformanceCycleService performanceCycleService;

    private final Lazy<QuestionService> questionService;

    private final Lazy<EmployeeAssessmentService> employeeAssessmentService;

    private final Lazy<TeamServiceLegacy> teamService;

    private static final String DEFAULT_WHEN_NULL = "";

    @Inject
    public AssessmentMatrixServiceV2(AssessmentMatrixRepositoryV2 assessmentMatrixRepositoryV2,
                                     PerformanceCycleService performanceCycleService,
                                     Lazy<QuestionService> questionService,
                                     Lazy<EmployeeAssessmentService> employeeAssessmentService,
                                     Lazy<TeamServiceLegacy> teamService) {
        this.assessmentMatrixRepositoryV2 = assessmentMatrixRepositoryV2;
        this.performanceCycleService = performanceCycleService;
        this.questionService = questionService;
        this.employeeAssessmentService = employeeAssessmentService;
        this.teamService = teamService;
    }

    @Override
    AssessmentMatrixRepositoryV2 getRepository() {
        return assessmentMatrixRepositoryV2;
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
    protected TeamServiceLegacy getTeamService() {
        return teamService.get();
    }

    public Optional<AssessmentMatrixV2> create(String name, String description, String tenantId, String performanceCycleId,
                                               Map<String, PillarV2> pillarMap) {
        return create(name, description, tenantId, performanceCycleId, pillarMap, null);
    }

    public Optional<AssessmentMatrixV2> create(String name, String description, String tenantId, String performanceCycleId,
                                               Map<String, PillarV2> pillarMap, AssessmentConfiguration configuration) {
        return super.create(createAssessmentMatrix(name, description, tenantId, performanceCycleId, pillarMap, configuration));
    }

    public Optional<AssessmentMatrixV2> update(String id, String name, String description, String tenantId, String performanceCycleId,
                                               Map<String, PillarV2> pillarMap) {
        return update(id, name, description, tenantId, performanceCycleId, pillarMap, null);
    }

    public Optional<AssessmentMatrixV2> update(String id, String name, String description, String tenantId, String performanceCycleId,
                                               Map<String, PillarV2> pillarMap, AssessmentConfiguration configuration) {
        Optional<AssessmentMatrixV2> optionalAssessmentMatrix = findById(id);
        if (optionalAssessmentMatrix.isPresent()) {
            AssessmentMatrixV2 assessmentMatrix = optionalAssessmentMatrix.get();
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

    public AssessmentMatrixV2 incrementQuestionCount(String matrixId) {
        Optional<AssessmentMatrixV2> optionalMatrix = findById(matrixId);
        if (optionalMatrix.isPresent()) {
            AssessmentMatrixV2 matrix = optionalMatrix.get();
            Integer currentCount = matrix.getQuestionCount() != null ? matrix.getQuestionCount() : 0;
            matrix.setQuestionCount(currentCount + 1);
            return super.update(matrix).orElse(matrix);
        }
        throw new RuntimeException("Matrix not found: " + matrixId);
    }

    public AssessmentMatrixV2 decrementQuestionCount(String matrixId) {
        Optional<AssessmentMatrixV2> optionalMatrix = findById(matrixId);
        if (optionalMatrix.isPresent()) {
            AssessmentMatrixV2 matrix = optionalMatrix.get();
            Integer currentCount = matrix.getQuestionCount() != null ? matrix.getQuestionCount() : 0;
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

    public AssessmentConfiguration getEffectiveConfiguration(AssessmentMatrixV2 assessmentMatrix) {
        AssessmentConfiguration configuration = assessmentMatrix.getConfiguration();
        if (configuration == null) {
            return createDefaultConfiguration();
        }
        return configuration;
    }

    public List<AssessmentMatrixV2> findAllByTenantId(String tenantId) {
        return getRepository().findAllByTenantId(tenantId);
    }

    public AssessmentMatrixV2 updateCurrentPotentialScore(String matrixId, String tenantId) {
        List<Question> questions = getQuestionService().findByAssessmentMatrixId(matrixId, tenantId);

        // Map pillarId -> PillarScore
        Map<String, PillarScore> pillarIdToPillarScoreMap = new HashMap<>();

        // 2. Calculate the maximum possible total score from the retrieved questions
        double totalPoints = questions.stream()
            .mapToDouble(question -> {
                // Retrieve or create the PillarScore
                PillarScore pillarScore = pillarIdToPillarScoreMap.computeIfAbsent(question.getPillarId(), id ->
                    PillarScore.builder()
                        .pillarId(id)
                        .pillarName(question.getPillarName())
                        .categoryIdToCategoryScoreMap(new HashMap<>())
                        .build()
                );

                // Run the logic to update PillarScore and if necessary CategoryScore based on the question
                updatePillarScoreWithQuestion(pillarScore, question);

                return computeQuestionMaxScore(question);
            })
            .sum();

        // 3.1 Retrieve the AssessmentMatrix
        Optional<AssessmentMatrixV2> optionalMatrix = findById(matrixId);
        if (optionalMatrix.isEmpty()) {
            throw new RuntimeException("Matrix not found: " + matrixId);
        }
        
        AssessmentMatrixV2 assessmentMatrix = optionalMatrix.get();

        // 3.2 Update the PotentialScore of the AssessmentMatrix
        PotentialScore potentialScore = assessmentMatrix.getPotentialScore();
        if (potentialScore == null) {
            potentialScore = PotentialScore.builder().build();
            assessmentMatrix.setPotentialScore(potentialScore);
        }
        potentialScore.setPillarIdToPillarScoreMap(pillarIdToPillarScoreMap);
        potentialScore.setScore(totalPoints);

        // 4. Save the updated AssessmentMatrix
        return super.update(assessmentMatrix).orElse(assessmentMatrix);
    }

    public Optional<AssessmentDashboardData> getAssessmentDashboard(String matrixId, String tenantId) {
        Optional<AssessmentMatrixV2> optionalMatrix = findById(matrixId);
        if (optionalMatrix.isEmpty()) {
            return Optional.empty();
        }

        AssessmentMatrixV2 matrix = optionalMatrix.get();
        
        // Check if the matrix belongs to the requested tenant
        if (!matrix.getTenantId().equals(tenantId)) {
            return Optional.empty();
        }
        
        // Get all employee assessments for this matrix using existing GSI
        // This is cost-effective as it uses assessmentMatrixId-employeeEmail-index
        List<EmployeeAssessment> employeeAssessments = getEmployeeAssessmentService()
            .findByAssessmentMatrix(matrixId, tenantId);

        // Build team summaries using in-memory aggregation (low cost)
        List<TeamAssessmentSummary> teamSummaries = buildTeamSummaries(employeeAssessments, tenantId);

        // Build employee summaries with pagination consideration
        List<EmployeeAssessmentSummary> employeeSummaries = buildEmployeeSummaries(employeeAssessments);

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

    private AssessmentMatrixV2 createAssessmentMatrix(String name, String description, String tenantId, String performanceCycleId,
                                                      Map<String, PillarV2> pillarMap, AssessmentConfiguration configuration) {
        validatePerformanceCycle(performanceCycleId, tenantId);

        return AssessmentMatrixV2.builder()
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
            Optional<PerformanceCycleV2> performanceCycle = performanceCycleService.findById(performanceCycleId);
            if (performanceCycle.isEmpty() || !performanceCycle.get().getTenantId().equals(tenantId)) {
                throw new InvalidIdReferenceException("PerformanceCycle not found or does not belong to tenant: " + performanceCycleId);
            }
        }
    }

    private PotentialScore calculatePotentialScore(AssessmentMatrixV2 matrix) {
        List<Question> questions = getQuestionService().findByAssessmentMatrixId(matrix.getId(), matrix.getTenantId());

        // Map pillarId -> PillarScore
        Map<String, PillarScore> pillarIdToPillarScoreMap = new HashMap<>();

        // Calculate the maximum possible total score from the retrieved questions
        double totalPoints = questions.stream()
            .mapToDouble(question -> {
                // Retrieve or create the PillarScore
                PillarScore pillarScore = pillarIdToPillarScoreMap.computeIfAbsent(question.getPillarId(), id ->
                    PillarScore.builder()
                        .pillarId(id)
                        .pillarName(question.getPillarName())
                        .categoryIdToCategoryScoreMap(new HashMap<>())
                        .build()
                );

                // Run the logic to update PillarScore and if necessary CategoryScore based on the question
                updatePillarScoreWithQuestion(pillarScore, question);

                return computeQuestionMaxScore(question);
            })
            .sum();

        return PotentialScore.builder()
            .pillarIdToPillarScoreMap(pillarIdToPillarScoreMap)
            .score(totalPoints)
            .build();
    }

    private void updatePillarScoreWithQuestion(PillarScore pillarScore, Question question) {
        // Assuming each CategoryScore can be identified by the category ID in the question
        String categoryId = question.getCategoryId();
        CategoryScore categoryScore = pillarScore.getCategoryIdToCategoryScoreMap().computeIfAbsent(categoryId, id ->
            CategoryScore.builder()
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
        categoryScore.setScore(categoryScore.getQuestionScores().stream()
            .mapToDouble(QuestionScore::getScore)
            .sum());

        // Update the maxPillarScore in PillarScore
        pillarScore.setScore(pillarScore.getCategoryIdToCategoryScoreMap().values().stream()
            .mapToDouble(CategoryScore::getScore)
            .sum());
    }

    @VisibleForTesting
    Double computeQuestionMaxScore(Question question) {
        if (QuestionType.CUSTOMIZED.equals(question.getQuestionType())) {
            if (question.getOptionGroup().isMultipleChoice()) {
                return question.getOptionGroup().getOptionMap().values().stream()
                    .mapToDouble(QuestionOption::getPoints)
                    .sum();
            } else {
                return question.getOptionGroup().getOptionMap().values().stream()
                    .mapToDouble(QuestionOption::getPoints)
                    .max().orElse(0);
            }
        } else {
            return question.getPoints();
        }
    }

    /**
     * Builds employee summaries with efficient data conversion.
     */
    private List<EmployeeAssessmentSummary> buildEmployeeSummaries(List<EmployeeAssessment> assessments) {
        return assessments.stream()
            .map(this::buildEmployeeSummary)
            .collect(Collectors.toList());
    }

    /**
     * Builds individual employee assessment summary.
     */
    private EmployeeAssessmentSummary buildEmployeeSummary(EmployeeAssessment assessment) {
        Double currentScore = null;
        if (assessment.getEmployeeAssessmentScore() != null) {
            currentScore = assessment.getEmployeeAssessmentScore().getScore();
        }

        // Convert lastActivityDate from Date to LocalDateTime
        java.time.LocalDateTime lastActivityDate = null;
        if (assessment.getLastActivityDate() != null) {
            lastActivityDate = assessment.getLastActivityDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
        }

        return EmployeeAssessmentSummary.builder()
            .employeeAssessmentId(assessment.getId())
            .employeeName(assessment.getEmployee().getName())
            .employeeEmail(assessment.getEmployee().getEmail())
            .teamId(assessment.getTeamId())
            .assessmentStatus(assessment.getAssessmentStatus())
            .currentScore(currentScore)
            .answeredQuestions(assessment.getAnsweredQuestionCount())
            .lastActivityDate(lastActivityDate)
            .build();
    }

    /**
     * Builds team summaries by grouping employee assessments and fetching team details.
     * Uses batch processing for team lookups to minimize DynamoDB calls.
     */
    private List<TeamAssessmentSummary> buildTeamSummaries(List<EmployeeAssessment> assessments, String tenantId) {
        // Group assessments by team ID (in-memory operation - no additional DynamoDB cost)
        Map<String, List<EmployeeAssessment>> assessmentsByTeam = assessments.stream()
            .filter(ea -> StringUtils.isNotBlank(ea.getTeamId()))
            .collect(Collectors.groupingBy(EmployeeAssessment::getTeamId));

        return assessmentsByTeam.entrySet().stream()
            .map(entry -> buildTeamSummary(entry.getKey(), entry.getValue()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    /**
     * Builds a team summary for a specific team with aggregated metrics.
     */
    private Optional<TeamAssessmentSummary> buildTeamSummary(String teamId, List<EmployeeAssessment> assessments) {
        try {
            Optional<Team> teamOpt = getTeamService().findById(teamId);
            if (!teamOpt.isPresent()) {
                return Optional.empty();
            }

            Team team = teamOpt.get();

            int totalEmployees = assessments.size();
            int completedAssessments = calculateCompletedCount(assessments);
            double completionPercentage = totalEmployees > 0 ? (completedAssessments * 100.0) / totalEmployees : 0.0;

            // Calculate average score for completed assessments
            Double averageScore = calculateAverageScore(assessments);

            return Optional.of(TeamAssessmentSummary.builder()
                .teamId(teamId)
                .teamName(team.getName())
                .totalEmployees(totalEmployees)
                .completedAssessments(completedAssessments)
                .completionPercentage(completionPercentage)
                .averageScore(averageScore)
                .build());
        }
        catch (Exception e) {
            // Log error and skip this team rather than failing entire dashboard
            return Optional.empty();
        }
    }

    /**
     * Calculates average score for completed assessments.
     */
    private Double calculateAverageScore(List<EmployeeAssessment> assessments) {
        List<Double> completedScores = assessments.stream()
            .filter(ea -> ea.getAssessmentStatus() == AssessmentStatus.COMPLETED)
            .filter(ea -> ea.getEmployeeAssessmentScore() != null && ea.getEmployeeAssessmentScore().getScore() != null)
            .map(ea -> ea.getEmployeeAssessmentScore().getScore())
            .collect(Collectors.toList());

        if (completedScores.isEmpty()) {
            return null;
        }

        return completedScores.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }

    /**
     * Counts completed assessments efficiently.
     */
    private int calculateCompletedCount(List<EmployeeAssessment> assessments) {
        return (int) assessments.stream()
            .filter(ea -> ea.getAssessmentStatus() == AssessmentStatus.COMPLETED)
            .count();
    }

    @Override
    protected void postCreate(AssessmentMatrixV2 entity) {
        // Hook for post-create actions
    }

    @Override
    protected void postUpdate(AssessmentMatrixV2 entity) {
        // Hook for post-update actions
    }

    @Override
    protected void postDelete(String id) {
        // Hook for post-delete actions
    }
}
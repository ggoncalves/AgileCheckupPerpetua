package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentConfigurationV2;
import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessmentV2;
import com.agilecheckup.persistency.entity.PerformanceCycleV2;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.persistency.entity.QuestionNavigationType;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.TeamV2;
import com.agilecheckup.persistency.entity.question.QuestionV2;
import com.agilecheckup.persistency.entity.question.QuestionOptionV2;
import com.agilecheckup.persistency.entity.score.CategoryScoreV2;
import com.agilecheckup.persistency.entity.score.PillarScoreV2;
import com.agilecheckup.persistency.entity.score.PotentialScoreV2;
import com.agilecheckup.persistency.entity.score.QuestionScoreV2;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepositoryV2;
import com.agilecheckup.service.dto.AssessmentDashboardData;
import com.agilecheckup.service.dto.EmployeeAssessmentSummaryV2;
import com.agilecheckup.service.dto.TeamAssessmentSummaryV2;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.google.common.annotations.VisibleForTesting;
import dagger.Lazy;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AssessmentMatrixServiceV2 extends AbstractCrudServiceV2<AssessmentMatrixV2, AssessmentMatrixRepositoryV2> {

    private final AssessmentMatrixRepositoryV2 assessmentMatrixRepositoryV2;

    private final PerformanceCycleServiceV2 performanceCycleServiceV2;

    private final Lazy<QuestionServiceV2> questionService;

    private final Lazy<EmployeeAssessmentServiceV2> employeeAssessmentServiceV2;

    private final Lazy<TeamServiceV2> teamServiceV2;

    private static final String DEFAULT_WHEN_NULL = "";

    @Inject
    public AssessmentMatrixServiceV2(AssessmentMatrixRepositoryV2 assessmentMatrixRepositoryV2,
                                     PerformanceCycleServiceV2 performanceCycleServiceV2,
                                     Lazy<QuestionServiceV2> questionService,
                                     Lazy<EmployeeAssessmentServiceV2> employeeAssessmentServiceV2,
                                     Lazy<TeamServiceV2> teamServiceV2) {
        this.assessmentMatrixRepositoryV2 = assessmentMatrixRepositoryV2;
        this.performanceCycleServiceV2 = performanceCycleServiceV2;
        this.questionService = questionService;
        this.employeeAssessmentServiceV2 = employeeAssessmentServiceV2;
        this.teamServiceV2 = teamServiceV2;
    }

    @Override
    AssessmentMatrixRepositoryV2 getRepository() {
        return assessmentMatrixRepositoryV2;
    }

    @VisibleForTesting
    protected QuestionServiceV2 getQuestionServiceV2() {
        return questionService.get();
    }

    @VisibleForTesting
    protected EmployeeAssessmentServiceV2 getEmployeeAssessmentServiceV2() {
        return employeeAssessmentServiceV2.get();
    }

    @VisibleForTesting
    protected TeamServiceV2 getTeamServiceV2() {
        return teamServiceV2.get();
    }

    public Optional<AssessmentMatrixV2> create(String name, String description, String tenantId, String performanceCycleId,
                                               Map<String, PillarV2> pillarMap) {
        return create(name, description, tenantId, performanceCycleId, pillarMap, null);
    }

    public Optional<AssessmentMatrixV2> create(String name, String description, String tenantId, String performanceCycleId,
                                               Map<String, PillarV2> pillarMap, AssessmentConfigurationV2 configuration) {
        return super.create(createAssessmentMatrix(name, description, tenantId, performanceCycleId, pillarMap, configuration));
    }

    public Optional<AssessmentMatrixV2> update(String id, String name, String description, String tenantId, String performanceCycleId,
                                               Map<String, PillarV2> pillarMap) {
        return update(id, name, description, tenantId, performanceCycleId, pillarMap, null);
    }

    public Optional<AssessmentMatrixV2> update(String id, String name, String description, String tenantId, String performanceCycleId,
                                               Map<String, PillarV2> pillarMap, AssessmentConfigurationV2 configuration) {
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

    public AssessmentConfigurationV2 createDefaultConfiguration() {
        return AssessmentConfigurationV2.builder()
                .allowQuestionReview(true)
                .requireAllQuestions(true)
                .autoSave(true)
                .navigationMode(QuestionNavigationType.RANDOM)
                .build();
    }

    public AssessmentConfigurationV2 getEffectiveConfiguration(AssessmentMatrixV2 assessmentMatrix) {
        AssessmentConfigurationV2 configuration = assessmentMatrix.getConfiguration();
        if (configuration == null) {
            return createDefaultConfiguration();
        }
        return configuration;
    }

    public List<AssessmentMatrixV2> findAllByTenantId(String tenantId) {
        return getRepository().findAllByTenantId(tenantId);
    }

    public AssessmentMatrixV2 updateCurrentPotentialScoreV2(String matrixId, String tenantId) {
        List<QuestionV2> questions = getQuestionServiceV2().findByAssessmentMatrixId(matrixId, tenantId);

        // Map pillarId -> PillarScoreV2
        Map<String, PillarScoreV2> pillarIdToPillarScoreMap = new HashMap<>();

        // 2. Calculate the maximum possible total score from the retrieved questions
        double totalPoints = questions.stream()
            .mapToDouble(question -> {
                // Retrieve or create the PillarScoreV2
                PillarScoreV2 pillarScore = pillarIdToPillarScoreMap.computeIfAbsent(question.getPillarId(), id ->
                    PillarScoreV2.builder()
                        .pillarId(id)
                        .pillarName(question.getPillarName())
                        .categoryIdToCategoryScoreMap(new HashMap<>())
                        .build()
                );

                // Run the logic to update PillarScoreV2 and if necessary CategoryScoreV2 based on the question
                updatePillarScoreV2WithQuestion(pillarScore, question);

                return computeQuestionMaxScore(question);
            })
            .sum();

        // 3.1 Retrieve the AssessmentMatrix
        Optional<AssessmentMatrixV2> optionalMatrix = findById(matrixId);
        if (optionalMatrix.isEmpty()) {
            throw new RuntimeException("Matrix not found: " + matrixId);
        }
        
        AssessmentMatrixV2 assessmentMatrix = optionalMatrix.get();

        // 3.2 Update the PotentialScoreV2 of the AssessmentMatrix
        PotentialScoreV2 potentialScore = assessmentMatrix.getPotentialScore();
        if (potentialScore == null) {
            potentialScore = PotentialScoreV2.builder().build();
            assessmentMatrix.setPotentialScore(potentialScore);
        }
        potentialScore.setPillarIdToPillarScoreMap(pillarIdToPillarScoreMap);
        potentialScore.setScore(totalPoints);

        // 4. Save the updated AssessmentMatrix
        return super.update(assessmentMatrix).orElse(assessmentMatrix);
    }

    public AssessmentMatrixV2 updateCurrentPotentialScore(String matrixId, String tenantId) {
        return updateCurrentPotentialScoreV2(matrixId, tenantId);
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
        List<EmployeeAssessmentV2> employeeAssessments = getEmployeeAssessmentServiceV2()
            .findByAssessmentMatrix(matrixId, tenantId);

        // Create employee summaries from V2 entities
        List<EmployeeAssessmentSummaryV2> employeeSummaries = createEmployeeSummaries(employeeAssessments);
        
        // Create team summaries from V2 entities
        List<TeamAssessmentSummaryV2> teamSummaries = createTeamSummaries(employeeAssessments);

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

    private List<EmployeeAssessmentSummaryV2> createEmployeeSummaries(List<EmployeeAssessmentV2> employeeAssessments) {
        return employeeAssessments.stream()
            .map(this::convertToEmployeeSummary)
            .collect(Collectors.toList());
    }
    
    private List<TeamAssessmentSummaryV2> createTeamSummaries(List<EmployeeAssessmentV2> employeeAssessments) {
        // Group assessments by team
        Map<String, List<EmployeeAssessmentV2>> assessmentsByTeam = employeeAssessments.stream()
            .filter(assessment -> assessment.getTeamId() != null)
            .collect(Collectors.groupingBy(EmployeeAssessmentV2::getTeamId));
            
        return assessmentsByTeam.entrySet().stream()
            .map(entry -> createTeamSummary(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
    
    private EmployeeAssessmentSummaryV2 convertToEmployeeSummary(EmployeeAssessmentV2 assessment) {
        return EmployeeAssessmentSummaryV2.builder()
            .employeeAssessmentId(assessment.getId())
            .employeeId(assessment.getEmployee() != null ? assessment.getEmployee().getId() : null)
            .employeeName(assessment.getEmployee() != null ? assessment.getEmployee().getName() : "Unknown")
            .employeeEmail(assessment.getEmployee() != null ? assessment.getEmployee().getEmail() : "Unknown")
            .teamId(assessment.getTeamId())
            .teamName(getTeamName(assessment.getTeamId()))
            .assessmentStatus(assessment.getAssessmentStatus())
            .currentScore(assessment.getEmployeeAssessmentScore() != null ? assessment.getEmployeeAssessmentScore().getScore() : null)
            .answeredQuestionCount(assessment.getAnsweredQuestionCount())
            .lastActivityDate(convertToLocalDateTime(assessment.getLastActivityDate()))
            .build();
    }
    
    private TeamAssessmentSummaryV2 createTeamSummary(String teamId, List<EmployeeAssessmentV2> teamAssessments) {
        int totalEmployees = teamAssessments.size();
        int completedAssessments = (int) teamAssessments.stream()
            .filter(assessment -> AssessmentStatus.COMPLETED.equals(assessment.getAssessmentStatus()))
            .count();
        double completionPercentage = totalEmployees > 0 ? (double) completedAssessments / totalEmployees * 100.0 : 0.0;
        
        Double averageScore = teamAssessments.stream()
            .filter(assessment -> assessment.getEmployeeAssessmentScore() != null && assessment.getEmployeeAssessmentScore().getScore() != null)
            .mapToDouble(assessment -> assessment.getEmployeeAssessmentScore().getScore())
            .average()
            .orElse(0.0);
            
        return TeamAssessmentSummaryV2.builder()
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
            Optional<TeamV2> team = getTeamServiceV2().findById(teamId);
            return team.map(TeamV2::getName).orElse("Unknown Team");
        } catch (Exception e) {
            return "Unknown Team";
        }
    }
    
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
    }

    private AssessmentMatrixV2 createAssessmentMatrix(String name, String description, String tenantId, String performanceCycleId,
                                                      Map<String, PillarV2> pillarMap, AssessmentConfigurationV2 configuration) {
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
            Optional<PerformanceCycleV2> performanceCycle = performanceCycleServiceV2.findById(performanceCycleId);
            if (performanceCycle.isEmpty() || !performanceCycle.get().getTenantId().equals(tenantId)) {
                throw new InvalidIdReferenceException("PerformanceCycle not found or does not belong to tenant: " + performanceCycleId);
            }
        }
    }

    private PotentialScoreV2 calculatePotentialScoreV2(AssessmentMatrixV2 matrix) {
        List<QuestionV2> questions = getQuestionServiceV2().findByAssessmentMatrixId(matrix.getId(), matrix.getTenantId());

        // Map pillarId -> PillarScoreV2
        Map<String, PillarScoreV2> pillarIdToPillarScoreMap = new HashMap<>();

        // Calculate the maximum possible total score from the retrieved questions
        double totalPoints = questions.stream()
            .mapToDouble(question -> {
                // Retrieve or create the PillarScoreV2
                PillarScoreV2 pillarScore = pillarIdToPillarScoreMap.computeIfAbsent(question.getPillarId(), id ->
                    PillarScoreV2.builder()
                        .pillarId(id)
                        .pillarName(question.getPillarName())
                        .categoryIdToCategoryScoreMap(new HashMap<>())
                        .build()
                );

                // Run the logic to update PillarScoreV2 and if necessary CategoryScoreV2 based on the question
                updatePillarScoreV2WithQuestion(pillarScore, question);

                return computeQuestionMaxScore(question);
            })
            .sum();

        return PotentialScoreV2.builder()
            .pillarIdToPillarScoreMap(pillarIdToPillarScoreMap)
            .score(totalPoints)
            .build();
    }

    private void updatePillarScoreV2WithQuestion(PillarScoreV2 pillarScore, QuestionV2 question) {
        // Assuming each CategoryScoreV2 can be identified by the category ID in the question
        String categoryId = question.getCategoryId();
        CategoryScoreV2 categoryScore = pillarScore.getCategoryIdToCategoryScoreMap().computeIfAbsent(categoryId, id ->
            CategoryScoreV2.builder()
                .categoryId(id)
                .categoryName(question.getCategoryName())
                .questionScores(new ArrayList<>())
                .build()
        );

        // Create and add QuestionScoreV2 to the questionScores list in CategoryScoreV2
        QuestionScoreV2 questionScore = QuestionScoreV2.builder()
            .questionId(question.getId())
            .score(computeQuestionMaxScore(question))
            .build();
        categoryScore.getQuestionScores().add(questionScore);

        // Update the maxCategoryScoreV2 in CategoryScoreV2
        categoryScore.setScore(categoryScore.getQuestionScores().stream()
            .mapToDouble(QuestionScoreV2::getScore)
            .sum());

        // Update the maxPillarScoreV2 in PillarScoreV2
        pillarScore.setScore(pillarScore.getCategoryIdToCategoryScoreMap().values().stream()
            .mapToDouble(CategoryScoreV2::getScore)
            .sum());
    }

    @VisibleForTesting
    Double computeQuestionMaxScore(QuestionV2 question) {
        if (QuestionType.CUSTOMIZED.equals(question.getQuestionType())) {
            if (question.getOptionGroup().isMultipleChoice()) {
                return question.getOptionGroup().getOptionMap().values().stream()
                    .mapToDouble(QuestionOptionV2::getPoints)
                    .sum();
            } else {
                return question.getOptionGroup().getOptionMap().values().stream()
                    .mapToDouble(QuestionOptionV2::getPoints)
                    .max().orElse(0);
            }
        } else {
            return question.getPoints();
        }
    }

    // Note: Summary building methods removed due to V1 DTO deletion
    // These methods (buildEmployeeSummaries, buildTeamSummaries, etc.) can be restored
    // once V2 DTOs are created for EmployeeAssessmentSummary and TeamAssessmentSummary

    /**
     * Counts completed assessments efficiently.
     */
    private int calculateCompletedCount(List<EmployeeAssessmentV2> assessments) {
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
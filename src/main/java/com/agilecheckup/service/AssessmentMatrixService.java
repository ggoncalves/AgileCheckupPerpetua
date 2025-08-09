package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentConfiguration;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
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
import com.agilecheckup.service.dto.AssessmentDashboardData;
import com.agilecheckup.service.dto.EmployeeAssessmentSummary;
import com.agilecheckup.service.dto.TeamAssessmentSummary;
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

public class AssessmentMatrixService extends AbstractCrudService<AssessmentMatrix, AssessmentMatrixRepository> {

    private final AssessmentMatrixRepository assessmentMatrixRepositoryV2;

    private final PerformanceCycleService performanceCycleServiceV2;

    private final Lazy<QuestionService> questionService;

    private final Lazy<EmployeeAssessmentService> employeeAssessmentServiceV2;

    private final Lazy<TeamService> teamServiceV2;

    private static final String DEFAULT_WHEN_NULL = "";

    @Inject
    public AssessmentMatrixService(AssessmentMatrixRepository assessmentMatrixRepositoryV2,
                                   PerformanceCycleService performanceCycleServiceV2,
                                   Lazy<QuestionService> questionService,
                                   Lazy<EmployeeAssessmentService> employeeAssessmentServiceV2,
                                   Lazy<TeamService> teamServiceV2) {
        this.assessmentMatrixRepositoryV2 = assessmentMatrixRepositoryV2;
        this.performanceCycleServiceV2 = performanceCycleServiceV2;
        this.questionService = questionService;
        this.employeeAssessmentServiceV2 = employeeAssessmentServiceV2;
        this.teamServiceV2 = teamServiceV2;
    }

    @Override
    AssessmentMatrixRepository getRepository() {
        return assessmentMatrixRepositoryV2;
    }

    @VisibleForTesting
    protected QuestionService getQuestionServiceV2() {
        return questionService.get();
    }

    @VisibleForTesting
    protected EmployeeAssessmentService getEmployeeAssessmentServiceV2() {
        return employeeAssessmentServiceV2.get();
    }

    @VisibleForTesting
    protected TeamService getTeamServiceV2() {
        return teamServiceV2.get();
    }

    public Optional<AssessmentMatrix> create(String name, String description, String tenantId, String performanceCycleId,
                                             Map<String, Pillar> pillarMap) {
        return create(name, description, tenantId, performanceCycleId, pillarMap, null);
    }

    public Optional<AssessmentMatrix> create(String name, String description, String tenantId, String performanceCycleId,
                                             Map<String, Pillar> pillarMap, AssessmentConfiguration configuration) {
        return super.create(createAssessmentMatrix(name, description, tenantId, performanceCycleId, pillarMap, configuration));
    }

    public Optional<AssessmentMatrix> update(String id, String name, String description, String tenantId, String performanceCycleId,
                                             Map<String, Pillar> pillarMap) {
        return update(id, name, description, tenantId, performanceCycleId, pillarMap, null);
    }

    public Optional<AssessmentMatrix> update(String id, String name, String description, String tenantId, String performanceCycleId,
                                             Map<String, Pillar> pillarMap, AssessmentConfiguration configuration) {
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
            Integer currentCount = matrix.getQuestionCount() != null ? matrix.getQuestionCount() : 0;
            matrix.setQuestionCount(currentCount + 1);
            return super.update(matrix).orElse(matrix);
        }
        throw new RuntimeException("Matrix not found: " + matrixId);
    }

    public AssessmentMatrix decrementQuestionCount(String matrixId) {
        Optional<AssessmentMatrix> optionalMatrix = findById(matrixId);
        if (optionalMatrix.isPresent()) {
            AssessmentMatrix matrix = optionalMatrix.get();
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

    public AssessmentMatrix updateCurrentPotentialScoreV2(String matrixId, String tenantId) {
        List<Question> questions = getQuestionServiceV2().findByAssessmentMatrixId(matrixId, tenantId);

        // Map pillarId -> PillarScoreV2
        Map<String, PillarScore> pillarIdToPillarScoreMap = new HashMap<>();

        // 2. Calculate the maximum possible total score from the retrieved questions
        double totalPoints = questions.stream()
            .mapToDouble(question -> {
                // Retrieve or create the PillarScoreV2
                PillarScore pillarScore = pillarIdToPillarScoreMap.computeIfAbsent(question.getPillarId(), id ->
                    PillarScore.builder()
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
        Optional<AssessmentMatrix> optionalMatrix = findById(matrixId);
        if (optionalMatrix.isEmpty()) {
            throw new RuntimeException("Matrix not found: " + matrixId);
        }
        
        AssessmentMatrix assessmentMatrix = optionalMatrix.get();

        // 3.2 Update the PotentialScoreV2 of the AssessmentMatrix
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

    public AssessmentMatrix updateCurrentPotentialScore(String matrixId, String tenantId) {
        return updateCurrentPotentialScoreV2(matrixId, tenantId);
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
        List<EmployeeAssessment> employeeAssessments = getEmployeeAssessmentServiceV2()
            .findByAssessmentMatrix(matrixId, tenantId);

        // Create employee summaries from V2 entities
        List<EmployeeAssessmentSummary> employeeSummaries = createEmployeeSummaries(employeeAssessments);
        
        // Create team summaries from V2 entities
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
        return employeeAssessments.stream()
            .map(this::convertToEmployeeSummary)
            .collect(Collectors.toList());
    }
    
    private List<TeamAssessmentSummary> createTeamSummaries(List<EmployeeAssessment> employeeAssessments) {
        // Group assessments by team
        Map<String, List<EmployeeAssessment>> assessmentsByTeam = employeeAssessments.stream()
            .filter(assessment -> assessment.getTeamId() != null)
            .collect(Collectors.groupingBy(EmployeeAssessment::getTeamId));
            
        return assessmentsByTeam.entrySet().stream()
            .map(entry -> createTeamSummary(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
    
    private EmployeeAssessmentSummary convertToEmployeeSummary(EmployeeAssessment assessment) {
        return EmployeeAssessmentSummary.builder()
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
    
    private TeamAssessmentSummary createTeamSummary(String teamId, List<EmployeeAssessment> teamAssessments) {
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
            Optional<Team> team = getTeamServiceV2().findById(teamId);
            return team.map(Team::getName).orElse("Unknown Team");
        } catch (Exception e) {
            return "Unknown Team";
        }
    }
    
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
    }

    private AssessmentMatrix createAssessmentMatrix(String name, String description, String tenantId, String performanceCycleId,
                                                    Map<String, Pillar> pillarMap, AssessmentConfiguration configuration) {
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
            Optional<PerformanceCycle> performanceCycle = performanceCycleServiceV2.findById(performanceCycleId);
            if (performanceCycle.isEmpty() || !performanceCycle.get().getTenantId().equals(tenantId)) {
                throw new InvalidIdReferenceException("PerformanceCycle not found or does not belong to tenant: " + performanceCycleId);
            }
        }
    }

    private PotentialScore calculatePotentialScoreV2(AssessmentMatrix matrix) {
        List<Question> questions = getQuestionServiceV2().findByAssessmentMatrixId(matrix.getId(), matrix.getTenantId());

        // Map pillarId -> PillarScoreV2
        Map<String, PillarScore> pillarIdToPillarScoreMap = new HashMap<>();

        // Calculate the maximum possible total score from the retrieved questions
        double totalPoints = questions.stream()
            .mapToDouble(question -> {
                // Retrieve or create the PillarScoreV2
                PillarScore pillarScore = pillarIdToPillarScoreMap.computeIfAbsent(question.getPillarId(), id ->
                    PillarScore.builder()
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

        return PotentialScore.builder()
            .pillarIdToPillarScoreMap(pillarIdToPillarScoreMap)
            .score(totalPoints)
            .build();
    }

    private void updatePillarScoreV2WithQuestion(PillarScore pillarScore, Question question) {
        // Assuming each CategoryScoreV2 can be identified by the category ID in the question
        String categoryId = question.getCategoryId();
        CategoryScore categoryScore = pillarScore.getCategoryIdToCategoryScoreMap().computeIfAbsent(categoryId, id ->
            CategoryScore.builder()
                .categoryId(id)
                .categoryName(question.getCategoryName())
                .questionScores(new ArrayList<>())
                .build()
        );

        // Create and add QuestionScoreV2 to the questionScores list in CategoryScoreV2
        QuestionScore questionScore = QuestionScore.builder()
            .questionId(question.getId())
            .score(computeQuestionMaxScore(question))
            .build();
        categoryScore.getQuestionScores().add(questionScore);

        // Update the maxCategoryScoreV2 in CategoryScoreV2
        categoryScore.setScore(categoryScore.getQuestionScores().stream()
            .mapToDouble(QuestionScore::getScore)
            .sum());

        // Update the maxPillarScoreV2 in PillarScoreV2
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

    // Note: Summary building methods removed due to V1 DTO deletion
    // These methods (buildEmployeeSummaries, buildTeamSummaries, etc.) can be restored
    // once V2 DTOs are created for EmployeeAssessmentSummary and TeamAssessmentSummary

    /**
     * Counts completed assessments efficiently.
     */
    private int calculateCompletedCount(List<EmployeeAssessment> assessments) {
        return (int) assessments.stream()
            .filter(ea -> ea.getAssessmentStatus() == AssessmentStatus.COMPLETED)
            .count();
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
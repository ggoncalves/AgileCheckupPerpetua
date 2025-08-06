package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AnalyticsScope;
import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.CompanyV2;
import com.agilecheckup.persistency.entity.DashboardAnalyticsV2;
import com.agilecheckup.persistency.entity.EmployeeAssessmentV2;
import com.agilecheckup.persistency.entity.EmployeeAssessmentScoreV2;
import com.agilecheckup.persistency.entity.PerformanceCycleV2;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.persistency.entity.TeamV2;
import com.agilecheckup.persistency.entity.question.AnswerV2;
import com.agilecheckup.persistency.entity.score.CategoryScoreV2;
import com.agilecheckup.persistency.entity.score.PillarScoreV2;
import com.agilecheckup.persistency.entity.score.PotentialScoreV2;
import com.agilecheckup.persistency.repository.AnswerRepositoryV2;
import com.agilecheckup.persistency.repository.DashboardAnalyticsRepositoryV2;
import com.agilecheckup.persistency.repository.TeamRepositoryV2;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * V2 Service for calculating and managing dashboard analytics.
 */
@Slf4j
@Singleton
public class DashboardAnalyticsServiceV2 {

    private static final int MIN_WORD_FREQUENCY = 2;
    private static final int MAX_WORD_CLOUD_WORDS = 50;
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "as", "is", "was", "are", "were", "been",
            "be", "have", "has", "had", "do", "does", "did", "will", "would",
            "could", "should", "may", "might", "must", "can", "this", "that",
            "these", "those", "i", "you", "he", "she", "it", "we", "they"
    );

    private final DashboardAnalyticsRepositoryV2 dashboardAnalyticsRepository;
    private final AssessmentMatrixServiceV2 assessmentMatrixService;
    private final EmployeeAssessmentServiceV2 employeeAssessmentService;
    private final CompanyServiceV2 companyServiceV2;
    private final PerformanceCycleServiceV2 performanceCycleServiceV2;
    private final TeamRepositoryV2 teamRepository;
    private final AnswerRepositoryV2 answerRepository;
    private final ObjectMapper objectMapper;

    @Inject
    public DashboardAnalyticsServiceV2(
            DashboardAnalyticsRepositoryV2 dashboardAnalyticsRepository,
            AssessmentMatrixServiceV2 assessmentMatrixService,
            EmployeeAssessmentServiceV2 employeeAssessmentService,
            CompanyServiceV2 companyServiceV2,
            PerformanceCycleServiceV2 performanceCycleServiceV2,
            TeamRepositoryV2 teamRepository,
            AnswerRepositoryV2 answerRepository) {
        this.dashboardAnalyticsRepository = dashboardAnalyticsRepository;
        this.assessmentMatrixService = assessmentMatrixService;
        this.employeeAssessmentService = employeeAssessmentService;
        this.companyServiceV2 = companyServiceV2;
        this.performanceCycleServiceV2 = performanceCycleServiceV2;
        this.teamRepository = teamRepository;
        this.answerRepository = answerRepository;
        this.objectMapper = new ObjectMapper();
    }


    /**
     * Get analytics overview for an assessment matrix
     */
    public Optional<DashboardAnalyticsV2> getOverview(String assessmentMatrixId) {
        Optional<AssessmentMatrixV2> matrixOpt = assessmentMatrixService.findById(assessmentMatrixId);
        if (matrixOpt.isEmpty()) {
            log.warn("AssessmentMatrix not found for id={}", assessmentMatrixId);
            return Optional.empty();
        }
        
        AssessmentMatrixV2 matrix = matrixOpt.get();
        String performanceCycleId = matrix.getPerformanceCycleId();

        Optional<PerformanceCycleV2> cycleOpt = performanceCycleServiceV2.findById(performanceCycleId);
        String companyId = cycleOpt.map(PerformanceCycleV2::getCompanyId).orElse(null);

        try {
            Optional<DashboardAnalyticsV2> result = dashboardAnalyticsRepository.findAssessmentMatrixOverview(
                    companyId, performanceCycleId, assessmentMatrixId);

            return result;
        }
        catch (Exception e) {
            log.error("Error querying analytics for assessmentMatrixId={}, companyId={}, performanceCycleId={}",
                    assessmentMatrixId, companyId, performanceCycleId, e);
            throw e;
        }
    }

    /**
     * Get team-specific analytics
     */
    public Optional<DashboardAnalyticsV2> getTeamAnalytics(String assessmentMatrixId, String teamId) {
        Optional<AssessmentMatrixV2> matrixOpt = assessmentMatrixService.findById(assessmentMatrixId);
        if (matrixOpt.isEmpty()) {
            log.warn("AssessmentMatrix not found for id={}", assessmentMatrixId);
            return Optional.empty();
        }
        
        AssessmentMatrixV2 matrix = matrixOpt.get();
        String performanceCycleId = matrix.getPerformanceCycleId();

        Optional<PerformanceCycleV2> cycleOpt = performanceCycleServiceV2.findById(performanceCycleId);
        String companyId = cycleOpt.map(PerformanceCycleV2::getCompanyId).orElse(null);

        try {
            Optional<DashboardAnalyticsV2> result = dashboardAnalyticsRepository.findTeamAnalytics(
                    companyId, performanceCycleId, assessmentMatrixId, teamId);

            return result;
        }
        catch (Exception e) {
            log.error("Error querying team analytics for assessmentMatrixId={}, teamId={}, companyId={}, performanceCycleId={}",
                    assessmentMatrixId, teamId, companyId, performanceCycleId, e);
            throw e;
        }
    }

    /**
     * Get all analytics for an assessment matrix
     */
    public List<DashboardAnalyticsV2> getAllAnalytics(String assessmentMatrixId) {
        Optional<AssessmentMatrixV2> matrixOpt = assessmentMatrixService.findById(assessmentMatrixId);
        if (matrixOpt.isEmpty()) {
            return Collections.emptyList();
        }
        
        AssessmentMatrixV2 matrix = matrixOpt.get();
        String performanceCycleId = matrix.getPerformanceCycleId();

        Optional<PerformanceCycleV2> cycleOpt = performanceCycleServiceV2.findById(performanceCycleId);
        String companyId = cycleOpt.map(PerformanceCycleV2::getCompanyId).orElse(null);

        if (companyId == null) {
            return Collections.emptyList();
        }
        
        return dashboardAnalyticsRepository.findByCompanyAndPerformanceCycle(companyId, performanceCycleId)
                .stream()
                .filter(da -> da.getAssessmentMatrixId().equals(assessmentMatrixId))
                .collect(Collectors.toList());
    }

    /**
     * Update analytics for an entire assessment matrix
     */
    public void updateAssessmentMatrixAnalytics(String assessmentMatrixId) {
        Optional<AssessmentMatrixV2> matrixOpt = assessmentMatrixService.findById(assessmentMatrixId);
        if (matrixOpt.isEmpty()) {
            log.error("Assessment matrix not found: {}", assessmentMatrixId);
            return;
        }
        
        AssessmentMatrixV2 matrix = matrixOpt.get();
        String performanceCycleId = matrix.getPerformanceCycleId();

        Optional<PerformanceCycleV2> cycleOpt = performanceCycleServiceV2.findById(performanceCycleId);
        String companyId = cycleOpt.map(PerformanceCycleV2::getCompanyId).orElse(null);
        String performanceCycleName = cycleOpt.map(PerformanceCycleV2::getName).orElse("Unknown Cycle");

        Optional<CompanyV2> companyOpt = companyId != null ?
                companyServiceV2.findById(companyId) : Optional.empty();
        String companyName = companyOpt.map(CompanyV2::getName).orElse("Unknown Company");

        String assessmentMatrixName = matrix.getName();

        String tenantId = matrix.getTenantId();

        List<EmployeeAssessmentV2> allAssessments = employeeAssessmentService
                .findByAssessmentMatrix(assessmentMatrixId, tenantId);
        
        if (allAssessments.isEmpty()) {
            log.warn("No assessments found for matrix: {} with tenantId: {}. Cannot compute analytics.", assessmentMatrixId, tenantId);
            return;
        }
        
        Map<String, List<EmployeeAssessmentV2>> assessmentsByTeam = allAssessments.stream()
                .filter(ea -> ea.getTeamId() != null)
                .collect(Collectors.groupingBy(EmployeeAssessmentV2::getTeamId));
        
        List<DashboardAnalyticsV2> analyticsToSave = new ArrayList<>();
        
        for (Map.Entry<String, List<EmployeeAssessmentV2>> entry : assessmentsByTeam.entrySet()) {
            String teamId = entry.getKey();
            List<EmployeeAssessmentV2> teamAssessments = entry.getValue();
            
            DashboardAnalyticsV2 teamAnalytics = calculateAnalytics(
                    companyId, performanceCycleId, assessmentMatrixId,
                    AnalyticsScope.TEAM, teamId, teamAssessments, matrix,
                    companyName, performanceCycleName, assessmentMatrixName);
            
            analyticsToSave.add(teamAnalytics);
        }
        
        DashboardAnalyticsV2 overviewAnalytics = calculateOverviewAnalytics(
                companyId, performanceCycleId, assessmentMatrixId,
                allAssessments, matrix,
                companyName, performanceCycleName, assessmentMatrixName);
        analyticsToSave.add(overviewAnalytics);
        
        analyticsToSave.forEach(dashboardAnalyticsRepository::save);
    }

    private DashboardAnalyticsV2 calculateOverviewAnalytics(
            String companyId, String performanceCycleId, String assessmentMatrixId,
            List<EmployeeAssessmentV2> allAssessments, AssessmentMatrixV2 matrix,
            String companyName, String performanceCycleName, String assessmentMatrixName) {

        return calculateAnalytics(
                companyId, performanceCycleId, assessmentMatrixId,
                AnalyticsScope.ASSESSMENT_MATRIX, null, allAssessments, matrix,
                companyName, performanceCycleName, assessmentMatrixName);
    }

    private DashboardAnalyticsV2 calculateAnalytics(
            String companyId, String performanceCycleId, String assessmentMatrixId,
            AnalyticsScope scope, String teamId, List<EmployeeAssessmentV2> assessments, AssessmentMatrixV2 matrix,
            String companyName, String performanceCycleName, String assessmentMatrixName) {

        String teamName = null;
        if (scope == AnalyticsScope.TEAM && teamId != null) {
            Optional<TeamV2> teamOpt = teamRepository.findById(teamId);
            teamName = teamOpt.map(TeamV2::getName).orElse("Unknown Team");
        }

        int employeeCount = assessments.size();
        long completedCount = assessments.stream()
                .filter(ea -> ea.getAssessmentStatus() == AssessmentStatus.COMPLETED)
                .count();
        double completionPercentage = calculatePercentage(completedCount, employeeCount);

        List<EmployeeAssessmentV2> completedAssessments = assessments.stream()
                .filter(ea -> ea.getAssessmentStatus() == AssessmentStatus.COMPLETED)
                .collect(Collectors.toList());

        List<EmployeeAssessmentV2> scoredAssessments = completedAssessments.stream()
                .filter(ea -> ea.getEmployeeAssessmentScore() != null)
                .collect(Collectors.toList());

        double totalScore = 0.0;
        Map<String, Object> analyticsData = new HashMap<>();

        if (!completedAssessments.isEmpty()) {
            double sumOfScores = completedAssessments.stream()
                    .mapToDouble(ea -> ea.getEmployeeAssessmentScore() != null ?
                            ea.getEmployeeAssessmentScore().getScore() : 0.0)
                    .sum();
            totalScore = sumOfScores / completedAssessments.size();

            if (!scoredAssessments.isEmpty()) {
                analyticsData.put("pillars", calculatePillarAnalytics(scoredAssessments, matrix));
            }

            analyticsData.put("wordCloud", generateWordCloud(assessments));
        }

        String analyticsDataJson = convertToJson(analyticsData);

        return DashboardAnalyticsV2.builder()
                .companyPerformanceCycleId(companyId + "#" + performanceCycleId)
                .assessmentMatrixScopeId(buildAssessmentMatrixScopeId(assessmentMatrixId, scope, teamId))
                .companyId(companyId)
                .performanceCycleId(performanceCycleId)
                .assessmentMatrixId(assessmentMatrixId)
                .scope(scope)
                .teamId(teamId)
                .teamName(teamName)
                .companyName(companyName)
                .performanceCycleName(performanceCycleName)
                .assessmentMatrixName(assessmentMatrixName)
                .generalAverage(totalScore)
                .employeeCount(employeeCount)
                .completionPercentage(completionPercentage)
                .lastUpdated(Instant.now())
                .analyticsDataJson(analyticsDataJson)
                .build();
    }

    private Map<String, Object> calculatePillarAnalytics(
            List<EmployeeAssessmentV2> completedAssessments, AssessmentMatrixV2 matrix) {
        
        Map<String, Object> pillarAnalytics = new HashMap<>();
        PotentialScoreV2 potentialScore = matrix.getPotentialScore();
        
        if (potentialScore == null || potentialScore.getPillarIdToPillarScoreMap() == null) {
            return pillarAnalytics;
        }
        
        Map<String, List<PillarScoreV2>> pillarScores = new HashMap<>();
        
        for (EmployeeAssessmentV2 assessment : completedAssessments) {
            EmployeeAssessmentScoreV2 score = assessment.getEmployeeAssessmentScore();
            if (score != null && score.getPillarIdToPillarScoreMap() != null) {
                score.getPillarIdToPillarScoreMap().forEach((pillarId, pillarScore) -> {
                    pillarScores.computeIfAbsent(pillarId, k -> new ArrayList<>()).add(pillarScore);
                });
            }
        }
        
        pillarScores.forEach((pillarId, scores) -> {
            log.info("=== calculatePillarAnalytics processing pillarId: {} ===", pillarId);
            log.info("Number of scores for this pillar: {}", scores.size());
            
            Map<String, Object> pillarData = new HashMap<>();
            
            PillarV2 pillar = matrix.getPillarMap().get(pillarId);
            String pillarName = pillar != null ? pillar.getName() : pillarId;
            
            double avgActualScore = scores.stream()
                    .mapToDouble(PillarScoreV2::getScore)
                    .average()
                    .orElse(0.0);
            
            PillarScoreV2 potentialPillarScore = potentialScore.getPillarIdToPillarScoreMap().get(pillarId);
            double maxScore = potentialPillarScore != null ? potentialPillarScore.getScore() : 100.0;
            
            log.info("Pillar {} - avgActualScore: {}, maxScore: {}", pillarId, avgActualScore, maxScore);
            log.info("About to call calculateCategoryAnalytics for pillar: {}", pillarId);
            
            double percentage = calculatePercentage(avgActualScore, maxScore);
            double gapFromPotential = 100.0 - percentage;
            
            pillarData.put("name", pillarName);
            pillarData.put("percentage", percentage);
            pillarData.put("actualScore", avgActualScore);
            pillarData.put("potentialScore", maxScore);
            pillarData.put("gapFromPotential", gapFromPotential);
            
            Map<String, Object> categoryAnalyticsResult = calculateCategoryAnalytics(pillarId, scores, potentialPillarScore);
            log.info("calculateCategoryAnalytics returned {} categories for pillar {}", categoryAnalyticsResult.size(), pillarId);
            pillarData.put("categories", categoryAnalyticsResult);
            
            pillarAnalytics.put(pillarId, pillarData);
            log.info("=== Finished processing pillar: {} ===", pillarId);
        });
        
        return pillarAnalytics;
    }

    private Map<String, Object> calculateCategoryAnalytics(
            String pillarId, List<PillarScoreV2> pillarScores, PillarScoreV2 potentialPillarScore) {
        
        log.info("=== calculateCategoryAnalytics DEBUG START ===");
        log.info("Method called for pillarId: {}", pillarId);
        log.info("pillarScores parameter size: {}", pillarScores != null ? pillarScores.size() : "NULL");
        log.info("potentialPillarScore parameter: {}", potentialPillarScore != null ? "PRESENT" : "NULL");
        
        if (potentialPillarScore != null) {
            log.info("potentialPillarScore pillarId: {}", potentialPillarScore.getPillarId());
            log.info("potentialPillarScore categoryMap size: {}", 
                potentialPillarScore.getCategoryIdToCategoryScoreMap() != null ? 
                potentialPillarScore.getCategoryIdToCategoryScoreMap().size() : "NULL");
        }
        
        Map<String, Object> categoryAnalytics = new HashMap<>();
        Map<String, List<CategoryScoreV2>> categoryScores = new HashMap<>();
        
        // Debug: Log each pillar score being processed
        log.info("Processing {} pillar scores for category aggregation...", pillarScores.size());
        for (int i = 0; i < pillarScores.size(); i++) {
            PillarScoreV2 pillarScore = pillarScores.get(i);
            log.info("PillarScore[{}]: pillarId={}, categoryMap size={}", 
                i, 
                pillarScore.getPillarId(),
                pillarScore.getCategoryIdToCategoryScoreMap() != null ? pillarScore.getCategoryIdToCategoryScoreMap().size() : "NULL");
            
            if (pillarScore.getCategoryIdToCategoryScoreMap() != null) {
                pillarScore.getCategoryIdToCategoryScoreMap().forEach((categoryId, categoryScore) -> {
                    log.info("  Adding category: {} (name: {}, score: {}) to aggregation", 
                        categoryId, categoryScore.getCategoryName(), categoryScore.getScore());
                    categoryScores.computeIfAbsent(categoryId, k -> new ArrayList<>()).add(categoryScore);
                });
            } else {
                log.warn("PillarScore[{}] has NULL categoryIdToCategoryScoreMap", i);
            }
        }
        
        log.info("After aggregation, found {} unique categories: {}", 
            categoryScores.size(), categoryScores.keySet());
        
        // Debug: Log category processing
        categoryScores.forEach((categoryId, scores) -> {
            log.info("Processing category: {} with {} score entries", categoryId, scores.size());
            
            Map<String, Object> categoryData = new HashMap<>();
            
            String categoryName = scores.stream()
                    .map(CategoryScoreV2::getCategoryName)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(categoryId);
            
            double avgActualScore = scores.stream()
                    .mapToDouble(CategoryScoreV2::getScore)
                    .average()
                    .orElse(0.0);
            
            log.info("  Category {} - name: {}, avgScore: {}", categoryId, categoryName, avgActualScore);
            
            double maxScore = 100.0;
            if (potentialPillarScore != null && potentialPillarScore.getCategoryIdToCategoryScoreMap() != null) {
                CategoryScoreV2 potentialCategoryScore = potentialPillarScore.getCategoryIdToCategoryScoreMap().get(categoryId);
                if (potentialCategoryScore != null) {
                    maxScore = potentialCategoryScore.getScore();
                    log.info("  Category {} - found potential score: {}", categoryId, maxScore);
                } else {
                    log.warn("  Category {} - NO potential score found in potentialPillarScore", categoryId);
                }
            } else {
                log.warn("  Category {} - using default maxScore (potentialPillarScore or its categoryMap is null)", categoryId);
            }
            
            double percentage = calculatePercentage(avgActualScore, maxScore);
            
            categoryData.put("name", categoryName);
            categoryData.put("percentage", percentage);
            categoryData.put("actualScore", avgActualScore);
            categoryData.put("potentialScore", maxScore);
            
            log.info("  Category {} final data: name={}, percentage={}, actualScore={}, potentialScore={}", 
                categoryId, categoryName, percentage, avgActualScore, maxScore);
            
            categoryAnalytics.put(categoryId, categoryData);
        });
        
        log.info("calculateCategoryAnalytics RESULT: {} categories in final analytics", categoryAnalytics.size());
        log.info("Final category analytics keys: {}", categoryAnalytics.keySet());
        log.info("=== calculateCategoryAnalytics DEBUG END ===");
        
        return categoryAnalytics;
    }

    private Map<String, Object> generateWordCloud(List<EmployeeAssessmentV2> assessments) {
        Map<String, Object> wordCloudData = new HashMap<>();
        List<Map<String, Object>> words = new ArrayList<>();
        
        List<String> allNotes = new ArrayList<>();
        for (EmployeeAssessmentV2 assessment : assessments) {
            List<AnswerV2> answers = answerRepository.findByEmployeeAssessmentId(
                    assessment.getId(), assessment.getTenantId());
            
            answers.stream()
                    .map(AnswerV2::getNotes)
                    .filter(StringUtils::isNotBlank)
                    .forEach(allNotes::add);
        }
        
        if (allNotes.isEmpty()) {
            wordCloudData.put("status", "none");
            wordCloudData.put("totalResponses", 0);
            wordCloudData.put("words", words);
            return wordCloudData;
        }
        
        Map<String, Integer> wordFrequencies = extractWordFrequencies(allNotes);
        
        wordFrequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(MAX_WORD_CLOUD_WORDS)
                .forEach(entry -> {
                    Map<String, Object> wordData = new HashMap<>();
                    wordData.put("text", entry.getKey());
                    wordData.put("count", entry.getValue());
                    words.add(wordData);
                });
        
        String status = allNotes.size() < 10 ? "limited" : "sufficient";
        
        wordCloudData.put("status", status);
        wordCloudData.put("totalResponses", allNotes.size());
        wordCloudData.put("words", words);
        
        return wordCloudData;
    }

    private Map<String, Integer> extractWordFrequencies(List<String> texts) {
        Map<String, Integer> frequencies = new HashMap<>();
        
        for (String text : texts) {
            String[] words = text.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", " ")
                    .split("\\s+");
            
            for (String word : words) {
                if (word.length() > 2 && !STOP_WORDS.contains(word)) {
                    frequencies.merge(word, 1, Integer::sum);
                }
            }
        }
        
        return frequencies.entrySet().stream()
                .filter(entry -> entry.getValue() >= MIN_WORD_FREQUENCY)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private double calculatePercentage(double value, double total) {
        if (total == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(value)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Failed to convert to JSON", e);
            return "{}";
        }
    }

    private String buildAssessmentMatrixScopeId(String assessmentMatrixId, AnalyticsScope scope, String teamId) {
        if (scope == AnalyticsScope.ASSESSMENT_MATRIX) {
            return assessmentMatrixId + "#" + scope.name();
        } else {
            return assessmentMatrixId + "#" + scope.name() + "#" + teamId;
        }
    }

    /**
     * Delete analytics by ID
     */
    public void deleteById(String companyPerformanceCycleId, String assessmentMatrixScopeId) {
        dashboardAnalyticsRepository.deleteById(companyPerformanceCycleId, assessmentMatrixScopeId);
    }
}
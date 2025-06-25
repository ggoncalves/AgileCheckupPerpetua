package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.*;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.score.*;
import com.agilecheckup.persistency.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating and managing dashboard analytics.
 * Phase 3: Implementation using actual entity APIs discovered in Phase 2.
 */
@Slf4j
@Singleton
public class DashboardAnalyticsService {

    private static final String OVERVIEW_TEAM_ID = "OVERVIEW";
    private static final int MIN_WORD_FREQUENCY = 2;
    private static final int MAX_WORD_CLOUD_WORDS = 50;
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "as", "is", "was", "are", "were", "been",
            "be", "have", "has", "had", "do", "does", "did", "will", "would",
            "could", "should", "may", "might", "must", "can", "this", "that",
            "these", "those", "i", "you", "he", "she", "it", "we", "they"
    );

    private final DashboardAnalyticsRepository dashboardAnalyticsRepository;
    private final AssessmentMatrixService assessmentMatrixService;
    private final EmployeeAssessmentService employeeAssessmentService;
    private final TeamRepository teamRepository;
    private final AnswerRepository answerRepository;
    private final ObjectMapper objectMapper;

    @Inject
    public DashboardAnalyticsService(
            DashboardAnalyticsRepository dashboardAnalyticsRepository,
            AssessmentMatrixService assessmentMatrixService,
            EmployeeAssessmentService employeeAssessmentService,
            TeamRepository teamRepository,
            AnswerRepository answerRepository) {
        this.dashboardAnalyticsRepository = dashboardAnalyticsRepository;
        this.assessmentMatrixService = assessmentMatrixService;
        this.employeeAssessmentService = employeeAssessmentService;
        this.teamRepository = teamRepository;
        this.answerRepository = answerRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get analytics overview for an assessment matrix
     */
    public Optional<DashboardAnalytics> getOverview(String assessmentMatrixId) {
        Optional<AssessmentMatrix> matrixOpt = assessmentMatrixService.findById(assessmentMatrixId);
        if (matrixOpt.isEmpty()) {
            return Optional.empty();
        }
        
        AssessmentMatrix matrix = matrixOpt.get();
        String companyId = matrix.getTenantId(); // Using actual API
        String performanceCycleId = matrix.getPerformanceCycleId();
        
        return dashboardAnalyticsRepository.findByCompanyPerformanceCycleAndTeam(
                companyId, performanceCycleId, assessmentMatrixId, OVERVIEW_TEAM_ID);
    }

    /**
     * Get team-specific analytics
     */
    public Optional<DashboardAnalytics> getTeamAnalytics(String assessmentMatrixId, String teamId) {
        Optional<AssessmentMatrix> matrixOpt = assessmentMatrixService.findById(assessmentMatrixId);
        if (matrixOpt.isEmpty()) {
            return Optional.empty();
        }
        
        AssessmentMatrix matrix = matrixOpt.get();
        String companyId = matrix.getTenantId();
        String performanceCycleId = matrix.getPerformanceCycleId();
        
        return dashboardAnalyticsRepository.findByCompanyPerformanceCycleAndTeam(
                companyId, performanceCycleId, assessmentMatrixId, teamId);
    }

    /**
     * Get all analytics for an assessment matrix
     */
    public List<DashboardAnalytics> getAllAnalytics(String assessmentMatrixId) {
        Optional<AssessmentMatrix> matrixOpt = assessmentMatrixService.findById(assessmentMatrixId);
        if (matrixOpt.isEmpty()) {
            return Collections.emptyList();
        }
        
        AssessmentMatrix matrix = matrixOpt.get();
        String companyId = matrix.getTenantId();
        String performanceCycleId = matrix.getPerformanceCycleId();
        
        return dashboardAnalyticsRepository.findByCompanyAndPerformanceCycle(companyId, performanceCycleId)
                .stream()
                .filter(da -> da.getAssessmentMatrixId().equals(assessmentMatrixId))
                .collect(Collectors.toList());
    }

    /**
     * Update analytics for an entire assessment matrix
     */
    public void updateAssessmentMatrixAnalytics(String assessmentMatrixId) {
        log.info("Updating analytics for assessment matrix: {}", assessmentMatrixId);
        
        Optional<AssessmentMatrix> matrixOpt = assessmentMatrixService.findById(assessmentMatrixId);
        if (matrixOpt.isEmpty()) {
            log.error("Assessment matrix not found: {}", assessmentMatrixId);
            return;
        }
        
        AssessmentMatrix matrix = matrixOpt.get();
        String companyId = matrix.getTenantId();
        String performanceCycleId = matrix.getPerformanceCycleId();
        
        // Get all employee assessments for this matrix using actual service method
        List<EmployeeAssessment> allAssessments = employeeAssessmentService
                .findByAssessmentMatrix(assessmentMatrixId, companyId);
        
        if (allAssessments.isEmpty()) {
            log.info("No assessments found for matrix: {}", assessmentMatrixId);
            return;
        }
        
        // Group assessments by team
        Map<String, List<EmployeeAssessment>> assessmentsByTeam = allAssessments.stream()
                .filter(ea -> ea.getTeamId() != null)
                .collect(Collectors.groupingBy(EmployeeAssessment::getTeamId));
        
        List<DashboardAnalytics> analyticsToSave = new ArrayList<>();
        
        // Calculate analytics for each team
        for (Map.Entry<String, List<EmployeeAssessment>> entry : assessmentsByTeam.entrySet()) {
            String teamId = entry.getKey();
            List<EmployeeAssessment> teamAssessments = entry.getValue();
            
            DashboardAnalytics teamAnalytics = calculateTeamAnalytics(
                    companyId, performanceCycleId, assessmentMatrixId, 
                    teamId, teamAssessments, matrix);
            
            analyticsToSave.add(teamAnalytics);
        }
        
        // Calculate overview analytics
        DashboardAnalytics overviewAnalytics = calculateOverviewAnalytics(
                companyId, performanceCycleId, assessmentMatrixId, 
                allAssessments, matrix);
        analyticsToSave.add(overviewAnalytics);
        
        // Save all analytics
        analyticsToSave.forEach(dashboardAnalyticsRepository::save);
        
        log.info("Updated {} analytics records for assessment matrix: {}", 
                analyticsToSave.size(), assessmentMatrixId);
    }

    /**
     * Calculate analytics for a specific team
     */
    private DashboardAnalytics calculateTeamAnalytics(
            String companyId, String performanceCycleId, String assessmentMatrixId,
            String teamId, List<EmployeeAssessment> teamAssessments, AssessmentMatrix matrix) {
        
        // Get team details
        Team team = teamRepository.findById(teamId);
        String teamName = team != null ? team.getName() : "Unknown Team";
        
        // Calculate basic metrics
        int employeeCount = teamAssessments.size();
        long completedCount = teamAssessments.stream()
                .filter(ea -> ea.getAssessmentStatus() == AssessmentStatus.COMPLETED)
                .count();
        double completionPercentage = calculatePercentage(completedCount, employeeCount);
        
        // Calculate scores from completed assessments
        List<EmployeeAssessment> completedAssessments = teamAssessments.stream()
                .filter(ea -> ea.getAssessmentStatus() == AssessmentStatus.COMPLETED)
                .filter(ea -> ea.getEmployeeAssessmentScore() != null)
                .collect(Collectors.toList());
        
        double generalAverage = 0.0;
        Map<String, Object> analyticsData = new HashMap<>();
        
        if (!completedAssessments.isEmpty()) {
            // Calculate average score
            double totalScore = completedAssessments.stream()
                    .mapToDouble(ea -> ea.getEmployeeAssessmentScore().getScore())
                    .sum();
            generalAverage = calculatePercentage(totalScore, completedAssessments.size() * 100);
            
            // Calculate pillar analytics
            analyticsData.put("pillars", calculatePillarAnalytics(completedAssessments, matrix));
            
            // Generate word cloud from answers
            analyticsData.put("wordCloud", generateWordCloud(teamAssessments));
        }
        
        // Convert analytics data to JSON
        String analyticsDataJson = convertToJson(analyticsData);
        
        return DashboardAnalytics.builder()
                .companyPerformanceCycleId(companyId + "#" + performanceCycleId)
                .assessmentMatrixTeamId(assessmentMatrixId + "#" + teamId)
                .companyId(companyId)
                .performanceCycleId(performanceCycleId)
                .assessmentMatrixId(assessmentMatrixId)
                .teamId(teamId)
                .teamName(teamName)
                .generalAverage(generalAverage)
                .employeeCount(employeeCount)
                .completionPercentage(completionPercentage)
                .lastUpdated(LocalDateTime.now())
                .analyticsDataJson(analyticsDataJson)
                .build();
    }

    /**
     * Calculate overview analytics for all teams
     */
    private DashboardAnalytics calculateOverviewAnalytics(
            String companyId, String performanceCycleId, String assessmentMatrixId,
            List<EmployeeAssessment> allAssessments, AssessmentMatrix matrix) {
        
        return calculateTeamAnalytics(
                companyId, performanceCycleId, assessmentMatrixId,
                OVERVIEW_TEAM_ID, allAssessments, matrix);
    }

    /**
     * Calculate pillar-level analytics
     */
    private Map<String, Object> calculatePillarAnalytics(
            List<EmployeeAssessment> completedAssessments, AssessmentMatrix matrix) {
        
        Map<String, Object> pillarAnalytics = new HashMap<>();
        PotentialScore potentialScore = matrix.getPotentialScore();
        
        if (potentialScore == null || potentialScore.getPillarIdToPillarScoreMap() == null) {
            return pillarAnalytics;
        }
        
        // Aggregate scores by pillar
        Map<String, List<PillarScore>> pillarScores = new HashMap<>();
        
        for (EmployeeAssessment assessment : completedAssessments) {
            EmployeeAssessmentScore score = assessment.getEmployeeAssessmentScore();
            if (score != null && score.getPillarIdToPillarScoreMap() != null) {
                score.getPillarIdToPillarScoreMap().forEach((pillarId, pillarScore) -> {
                    pillarScores.computeIfAbsent(pillarId, k -> new ArrayList<>()).add(pillarScore);
                });
            }
        }
        
        // Calculate averages and gaps
        pillarScores.forEach((pillarId, scores) -> {
            Map<String, Object> pillarData = new HashMap<>();
            
            // Get pillar name from matrix
            Pillar pillar = matrix.getPillarMap().get(pillarId);
            String pillarName = pillar != null ? pillar.getName() : pillarId;
            
            // Calculate average actual score
            double avgActualScore = scores.stream()
                    .mapToDouble(PillarScore::getScore)
                    .average()
                    .orElse(0.0);
            
            // Get potential score for this pillar
            PillarScore potentialPillarScore = potentialScore.getPillarIdToPillarScoreMap().get(pillarId);
            double maxScore = potentialPillarScore != null ? potentialPillarScore.getScore() : 100.0;
            
            double percentage = calculatePercentage(avgActualScore, maxScore);
            double gapFromPotential = 100.0 - percentage;
            
            pillarData.put("name", pillarName);
            pillarData.put("percentage", percentage);
            pillarData.put("actualScore", avgActualScore);
            pillarData.put("potentialScore", maxScore);
            pillarData.put("gapFromPotential", gapFromPotential);
            
            // Calculate category analytics for this pillar
            pillarData.put("categories", calculateCategoryAnalytics(pillarId, scores, potentialPillarScore));
            
            pillarAnalytics.put(pillarId, pillarData);
        });
        
        return pillarAnalytics;
    }

    /**
     * Calculate category-level analytics
     */
    private Map<String, Object> calculateCategoryAnalytics(
            String pillarId, List<PillarScore> pillarScores, PillarScore potentialPillarScore) {
        
        Map<String, Object> categoryAnalytics = new HashMap<>();
        
        // Aggregate scores by category
        Map<String, List<CategoryScore>> categoryScores = new HashMap<>();
        
        for (PillarScore pillarScore : pillarScores) {
            if (pillarScore.getCategoryIdToCategoryScoreMap() != null) {
                pillarScore.getCategoryIdToCategoryScoreMap().forEach((categoryId, categoryScore) -> {
                    categoryScores.computeIfAbsent(categoryId, k -> new ArrayList<>()).add(categoryScore);
                });
            }
        }
        
        // Calculate averages for each category
        categoryScores.forEach((categoryId, scores) -> {
            Map<String, Object> categoryData = new HashMap<>();
            
            String categoryName = scores.stream()
                    .map(CategoryScore::getCategoryName)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(categoryId);
            
            double avgActualScore = scores.stream()
                    .mapToDouble(CategoryScore::getScore)
                    .average()
                    .orElse(0.0);
            
            // Get potential score for this category
            double maxScore = 100.0; // Default
            if (potentialPillarScore != null && potentialPillarScore.getCategoryIdToCategoryScoreMap() != null) {
                CategoryScore potentialCategoryScore = potentialPillarScore.getCategoryIdToCategoryScoreMap().get(categoryId);
                if (potentialCategoryScore != null) {
                    maxScore = potentialCategoryScore.getScore();
                }
            }
            
            double percentage = calculatePercentage(avgActualScore, maxScore);
            
            categoryData.put("name", categoryName);
            categoryData.put("percentage", percentage);
            categoryData.put("actualScore", avgActualScore);
            categoryData.put("potentialScore", maxScore);
            
            categoryAnalytics.put(categoryId, categoryData);
        });
        
        return categoryAnalytics;
    }

    /**
     * Generate word cloud from answer notes
     */
    private Map<String, Object> generateWordCloud(List<EmployeeAssessment> assessments) {
        Map<String, Object> wordCloudData = new HashMap<>();
        List<Map<String, Object>> words = new ArrayList<>();
        
        // Get all answer notes
        List<String> allNotes = new ArrayList<>();
        for (EmployeeAssessment assessment : assessments) {
            List<Answer> answers = answerRepository.findByEmployeeAssessmentId(
                    assessment.getId(), assessment.getTenantId());
            
            answers.stream()
                    .map(Answer::getNotes)
                    .filter(StringUtils::isNotBlank)
                    .forEach(allNotes::add);
        }
        
        if (allNotes.isEmpty()) {
            wordCloudData.put("status", "none");
            wordCloudData.put("totalResponses", 0);
            wordCloudData.put("words", words);
            return wordCloudData;
        }
        
        // Extract words and calculate frequencies
        Map<String, Integer> wordFrequencies = extractWordFrequencies(allNotes);
        
        // Convert to word cloud format
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

    /**
     * Extract word frequencies from text
     */
    private Map<String, Integer> extractWordFrequencies(List<String> texts) {
        Map<String, Integer> frequencies = new HashMap<>();
        
        for (String text : texts) {
            // Split into words and clean
            String[] words = text.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", " ")
                    .split("\\s+");
            
            // Count frequencies
            for (String word : words) {
                if (word.length() > 2 && !STOP_WORDS.contains(word)) {
                    frequencies.merge(word, 1, Integer::sum);
                }
            }
        }
        
        // Filter by minimum frequency
        return frequencies.entrySet().stream()
                .filter(entry -> entry.getValue() >= MIN_WORD_FREQUENCY)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    /**
     * Calculate percentage with rounding
     */
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

    /**
     * Convert object to JSON string
     */
    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Failed to convert to JSON", e);
            return "{}";
        }
    }
}
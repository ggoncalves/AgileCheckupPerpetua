package com.agilecheckup.service;

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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.agilecheckup.persistency.entity.AnalyticsScope;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.DashboardAnalytics;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.EmployeeAssessmentScore;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.PotentialScore;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.persistency.repository.DashboardAnalyticsRepository;
import com.agilecheckup.persistency.repository.TeamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for calculating and managing dashboard analytics.
 */
@Slf4j
@Singleton
public class DashboardAnalyticsService {

  private static final int MIN_WORD_FREQUENCY = 2;
  private static final int MAX_WORD_CLOUD_WORDS = 50;
  private static final Set<String> STOP_WORDS = Set.of(
      "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "from", "as", "is", "was", "are", "were", "been", "be", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "must", "can", "this", "that", "these", "those", "i", "you", "he", "she", "it", "we", "they"
  );

  private final DashboardAnalyticsRepository dashboardAnalyticsRepository;
  private final AssessmentMatrixService assessmentMatrixService;
  private final EmployeeAssessmentService employeeAssessmentService;
  private final CompanyService companyService;
  private final PerformanceCycleService performanceCycleService;
  private final TeamRepository teamRepository;
  private final AnswerRepository answerRepository;
  private final ObjectMapper objectMapper;

  @Inject
  public DashboardAnalyticsService(
                                   DashboardAnalyticsRepository dashboardAnalyticsRepository, AssessmentMatrixService assessmentMatrixService, EmployeeAssessmentService employeeAssessmentService, CompanyService companyService, PerformanceCycleService performanceCycleService, TeamRepository teamRepository, AnswerRepository answerRepository) {
    this.dashboardAnalyticsRepository = dashboardAnalyticsRepository;
    this.assessmentMatrixService = assessmentMatrixService;
    this.employeeAssessmentService = employeeAssessmentService;
    this.companyService = companyService;
    this.performanceCycleService = performanceCycleService;
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
      log.warn("AssessmentMatrix not found for id={}", assessmentMatrixId);
      return Optional.empty();
    }

    AssessmentMatrix matrix = matrixOpt.get();
    String performanceCycleId = matrix.getPerformanceCycleId();

    Optional<PerformanceCycle> cycleOpt = performanceCycleService.findById(performanceCycleId);
    String companyId = cycleOpt.map(PerformanceCycle::getCompanyId).orElse(null);

    try {
      Optional<DashboardAnalytics> result = dashboardAnalyticsRepository.findAssessmentMatrixOverview(
          companyId, performanceCycleId, assessmentMatrixId);

      return result;
    }
    catch (Exception e) {
      log.error("Error querying analytics for assessmentMatrixId={}, companyId={}, performanceCycleId={}", assessmentMatrixId, companyId, performanceCycleId, e);
      throw e;
    }
  }

  /**
   * Get team-specific analytics
   */
  public Optional<DashboardAnalytics> getTeamAnalytics(String assessmentMatrixId, String teamId) {
    Optional<AssessmentMatrix> matrixOpt = assessmentMatrixService.findById(assessmentMatrixId);
    if (matrixOpt.isEmpty()) {
      log.warn("AssessmentMatrix not found for id={}", assessmentMatrixId);
      return Optional.empty();
    }

    AssessmentMatrix matrix = matrixOpt.get();
    String performanceCycleId = matrix.getPerformanceCycleId();

    Optional<PerformanceCycle> cycleOpt = performanceCycleService.findById(performanceCycleId);
    String companyId = cycleOpt.map(PerformanceCycle::getCompanyId).orElse(null);

    try {
      Optional<DashboardAnalytics> result = dashboardAnalyticsRepository.findTeamAnalytics(
          companyId, performanceCycleId, assessmentMatrixId, teamId);

      return result;
    }
    catch (Exception e) {
      log.error("Error querying team analytics for assessmentMatrixId={}, teamId={}, companyId={}, performanceCycleId={}", assessmentMatrixId, teamId, companyId, performanceCycleId, e);
      throw e;
    }
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
    String performanceCycleId = matrix.getPerformanceCycleId();

    Optional<PerformanceCycle> cycleOpt = performanceCycleService.findById(performanceCycleId);
    String companyId = cycleOpt.map(PerformanceCycle::getCompanyId).orElse(null);

    if (companyId == null) {
      return Collections.emptyList();
    }

    return dashboardAnalyticsRepository.findByCompanyAndPerformanceCycle(companyId, performanceCycleId).stream().filter(da -> da.getAssessmentMatrixId().equals(assessmentMatrixId)).collect(Collectors.toList());
  }

  /**
   * Update analytics for an entire assessment matrix
   */
  public void updateAssessmentMatrixAnalytics(String assessmentMatrixId) {
    Optional<AssessmentMatrix> matrixOpt = assessmentMatrixService.findById(assessmentMatrixId);
    if (matrixOpt.isEmpty()) {
      log.error("Assessment matrix not found: {}", assessmentMatrixId);
      return;
    }

    AssessmentMatrix matrix = matrixOpt.get();
    String performanceCycleId = matrix.getPerformanceCycleId();

    Optional<PerformanceCycle> cycleOpt = performanceCycleService.findById(performanceCycleId);
    String companyId = cycleOpt.map(PerformanceCycle::getCompanyId).orElse(null);
    String performanceCycleName = cycleOpt.map(PerformanceCycle::getName).orElse("Unknown Cycle");

    Optional<Company> companyOpt = companyId != null ? companyService.findById(companyId) : Optional.empty();
    String companyName = companyOpt.map(Company::getName).orElse("Unknown Company");

    String assessmentMatrixName = matrix.getName();

    String tenantId = matrix.getTenantId();

    List<EmployeeAssessment> allAssessments = employeeAssessmentService.findByAssessmentMatrix(assessmentMatrixId, tenantId);

    if (allAssessments.isEmpty()) {
      log.warn("No assessments found for matrix: {} with tenantId: {}. Cannot compute analytics.", assessmentMatrixId, tenantId);
      return;
    }

    Map<String, List<EmployeeAssessment>> assessmentsByTeam = allAssessments.stream().filter(ea -> ea.getTeamId() != null).collect(Collectors.groupingBy(EmployeeAssessment::getTeamId));

    List<DashboardAnalytics> analyticsToSave = new ArrayList<>();

    for (Map.Entry<String, List<EmployeeAssessment>> entry : assessmentsByTeam.entrySet()) {
      String teamId = entry.getKey();
      List<EmployeeAssessment> teamAssessments = entry.getValue();

      DashboardAnalytics teamAnalytics = calculateAnalytics(
          companyId, performanceCycleId, assessmentMatrixId, AnalyticsScope.TEAM, teamId, teamAssessments, matrix, companyName, performanceCycleName, assessmentMatrixName);

      analyticsToSave.add(teamAnalytics);
    }

    DashboardAnalytics overviewAnalytics = calculateOverviewAnalytics(
        companyId, performanceCycleId, assessmentMatrixId, allAssessments, matrix, companyName, performanceCycleName, assessmentMatrixName);
    analyticsToSave.add(overviewAnalytics);

    analyticsToSave.forEach(dashboardAnalyticsRepository::save);
  }

  private DashboardAnalytics calculateOverviewAnalytics(
                                                        String companyId, String performanceCycleId, String assessmentMatrixId, List<EmployeeAssessment> allAssessments, AssessmentMatrix matrix, String companyName, String performanceCycleName, String assessmentMatrixName) {

    return calculateAnalytics(
        companyId, performanceCycleId, assessmentMatrixId, AnalyticsScope.ASSESSMENT_MATRIX, null, allAssessments, matrix, companyName, performanceCycleName, assessmentMatrixName);
  }

  private DashboardAnalytics calculateAnalytics(
                                                String companyId, String performanceCycleId, String assessmentMatrixId, AnalyticsScope scope, String teamId, List<EmployeeAssessment> assessments, AssessmentMatrix matrix, String companyName, String performanceCycleName, String assessmentMatrixName) {

    String teamName = null;
    if (scope == AnalyticsScope.TEAM && teamId != null) {
      Optional<Team> teamOpt = teamRepository.findById(teamId);
      teamName = teamOpt.map(Team::getName).orElse("Unknown Team");
    }

    int employeeCount = assessments.size();
    long completedCount = assessments.stream().filter(ea -> ea.getAssessmentStatus() == AssessmentStatus.COMPLETED).count();
    double completionPercentage = calculatePercentage(completedCount, employeeCount);

    List<EmployeeAssessment> completedAssessments = assessments.stream().filter(ea -> ea.getAssessmentStatus() == AssessmentStatus.COMPLETED).collect(Collectors.toList());

    List<EmployeeAssessment> scoredAssessments = completedAssessments.stream().filter(ea -> ea.getEmployeeAssessmentScore() != null).collect(Collectors.toList());

    double totalScore = 0.0;
    Map<String, Object> analyticsData = new HashMap<>();

    if (!completedAssessments.isEmpty()) {
      double sumOfScores = completedAssessments.stream().mapToDouble(ea -> ea.getEmployeeAssessmentScore() != null ? ea.getEmployeeAssessmentScore().getScore() : 0.0).sum();
      totalScore = sumOfScores / completedAssessments.size();

      if (!scoredAssessments.isEmpty()) {
        analyticsData.put("pillars", calculatePillarAnalytics(scoredAssessments, matrix));
      }

      analyticsData.put("wordCloud", generateWordCloud(assessments));
    }

    String analyticsDataJson = convertToJson(analyticsData);

    return DashboardAnalytics.builder().companyPerformanceCycleId(companyId + "#" + performanceCycleId).assessmentMatrixScopeId(buildAssessmentMatrixScopeId(assessmentMatrixId, scope, teamId)).companyId(companyId).performanceCycleId(performanceCycleId).assessmentMatrixId(assessmentMatrixId).scope(scope).teamId(teamId).teamName(teamName).companyName(companyName).performanceCycleName(performanceCycleName).assessmentMatrixName(assessmentMatrixName).generalAverage(totalScore).employeeCount(employeeCount).completionPercentage(completionPercentage).lastUpdated(Instant.now()).analyticsDataJson(analyticsDataJson).build();
  }

  private Map<String, Object> calculatePillarAnalytics(
                                                       List<EmployeeAssessment> completedAssessments, AssessmentMatrix matrix) {

    Map<String, Object> pillarAnalytics = new HashMap<>();
    PotentialScore potentialScore = matrix.getPotentialScore();

    if (potentialScore == null || potentialScore.getPillarIdToPillarScoreMap() == null) {
      return pillarAnalytics;
    }

    Map<String, List<PillarScore>> pillarScores = new HashMap<>();

    for (EmployeeAssessment assessment : completedAssessments) {
      EmployeeAssessmentScore score = assessment.getEmployeeAssessmentScore();
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

      Pillar pillar = matrix.getPillarMap().get(pillarId);
      String pillarName = pillar != null ? pillar.getName() : pillarId;

      double avgActualScore = scores.stream().mapToDouble(PillarScore::getScore).average().orElse(0.0);

      PillarScore potentialPillarScore = potentialScore.getPillarIdToPillarScoreMap().get(pillarId);
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
                                                         String pillarId, List<PillarScore> pillarScores, PillarScore potentialPillarScore) {

    log.info("=== calculateCategoryAnalytics DEBUG START ===");
    log.info("Method called for pillarId: {}", pillarId);
    log.info("pillarScores parameter size: {}", pillarScores != null ? pillarScores.size() : "NULL");
    log.info("potentialPillarScore parameter: {}", potentialPillarScore != null ? "PRESENT" : "NULL");

    if (potentialPillarScore != null) {
      log.info("potentialPillarScore pillarId: {}", potentialPillarScore.getPillarId());
      log.info("potentialPillarScore categoryMap size: {}", potentialPillarScore.getCategoryIdToCategoryScoreMap() != null ? potentialPillarScore.getCategoryIdToCategoryScoreMap().size() : "NULL");
    }

    Map<String, Object> categoryAnalytics = new HashMap<>();
    Map<String, List<CategoryScore>> categoryScores = new HashMap<>();

    // Debug: Log each pillar score being processed
    log.info("Processing {} pillar scores for category aggregation...", pillarScores.size());
    for (int i = 0; i < pillarScores.size(); i++) {
      PillarScore pillarScore = pillarScores.get(i);
      log.info("PillarScore[{}]: pillarId={}, categoryMap size={}", i, pillarScore.getPillarId(), pillarScore.getCategoryIdToCategoryScoreMap() != null ? pillarScore.getCategoryIdToCategoryScoreMap().size() : "NULL");

      if (pillarScore.getCategoryIdToCategoryScoreMap() != null) {
        pillarScore.getCategoryIdToCategoryScoreMap().forEach((categoryId, categoryScore) -> {
          log.info("  Adding category: {} (name: {}, score: {}) to aggregation", categoryId, categoryScore.getCategoryName(), categoryScore.getScore());
          categoryScores.computeIfAbsent(categoryId, k -> new ArrayList<>()).add(categoryScore);
        });
      }
      else {
        log.warn("PillarScore[{}] has NULL categoryIdToCategoryScoreMap", i);
      }
    }

    log.info("After aggregation, found {} unique categories: {}", categoryScores.size(), categoryScores.keySet());

    // Debug: Log category processing
    categoryScores.forEach((categoryId, scores) -> {
      log.info("Processing category: {} with {} score entries", categoryId, scores.size());

      Map<String, Object> categoryData = new HashMap<>();

      String categoryName = scores.stream().map(CategoryScore::getCategoryName).filter(Objects::nonNull).findFirst().orElse(categoryId);

      double avgActualScore = scores.stream().mapToDouble(CategoryScore::getScore).average().orElse(0.0);

      log.info("  Category {} - name: {}, avgScore: {}", categoryId, categoryName, avgActualScore);

      double maxScore = 100.0;
      if (potentialPillarScore != null && potentialPillarScore.getCategoryIdToCategoryScoreMap() != null) {
        CategoryScore potentialCategoryScore = potentialPillarScore.getCategoryIdToCategoryScoreMap().get(categoryId);
        if (potentialCategoryScore != null) {
          maxScore = potentialCategoryScore.getScore();
          log.info("  Category {} - found potential score: {}", categoryId, maxScore);
        }
        else {
          log.warn("  Category {} - NO potential score found in potentialPillarScore", categoryId);
        }
      }
      else {
        log.warn("  Category {} - using default maxScore (potentialPillarScore or its categoryMap is null)", categoryId);
      }

      double percentage = calculatePercentage(avgActualScore, maxScore);

      categoryData.put("name", categoryName);
      categoryData.put("percentage", percentage);
      categoryData.put("actualScore", avgActualScore);
      categoryData.put("potentialScore", maxScore);

      log.info("  Category {} final data: name={}, percentage={}, actualScore={}, potentialScore={}", categoryId, categoryName, percentage, avgActualScore, maxScore);

      categoryAnalytics.put(categoryId, categoryData);
    });

    log.info("calculateCategoryAnalytics RESULT: {} categories in final analytics", categoryAnalytics.size());
    log.info("Final category analytics keys: {}", categoryAnalytics.keySet());
    log.info("=== calculateCategoryAnalytics DEBUG END ===");

    return categoryAnalytics;
  }

  private Map<String, Object> generateWordCloud(List<EmployeeAssessment> assessments) {
    Map<String, Object> wordCloudData = new HashMap<>();
    List<Map<String, Object>> words = new ArrayList<>();

    List<String> allNotes = new ArrayList<>();
    for (EmployeeAssessment assessment : assessments) {
      List<Answer> answers = answerRepository.findByEmployeeAssessmentId(
          assessment.getId(), assessment.getTenantId());

      answers.stream().map(Answer::getNotes).filter(StringUtils::isNotBlank).forEach(allNotes::add);
    }

    if (allNotes.isEmpty()) {
      wordCloudData.put("status", "none");
      wordCloudData.put("totalResponses", 0);
      wordCloudData.put("words", words);
      return wordCloudData;
    }

    Map<String, Integer> wordFrequencies = extractWordFrequencies(allNotes);

    wordFrequencies.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(MAX_WORD_CLOUD_WORDS).forEach(entry -> {
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
      String[] words = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").split("\\s+");

      for (String word : words) {
        if (word.length() > 2 && !STOP_WORDS.contains(word)) {
          frequencies.merge(word, 1, Integer::sum);
        }
      }
    }

    return frequencies.entrySet().stream().filter(entry -> entry.getValue() >= MIN_WORD_FREQUENCY).collect(Collectors.toMap(
        Map.Entry::getKey, Map.Entry::getValue
    ));
  }

  private double calculatePercentage(double value, double total) {
    if (total == 0) {
      return 0.0;
    }
    return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  private String convertToJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    }
    catch (Exception e) {
      log.error("Failed to convert to JSON", e);
      return "{}";
    }
  }

  private String buildAssessmentMatrixScopeId(String assessmentMatrixId, AnalyticsScope scope, String teamId) {
    if (scope == AnalyticsScope.ASSESSMENT_MATRIX) {
      return assessmentMatrixId + "#" + scope.name();
    }
    else {
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
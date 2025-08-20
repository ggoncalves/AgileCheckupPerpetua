package com.agilecheckup.main.runner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.AnalyticsScope;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.DashboardAnalytics;
import com.agilecheckup.persistency.entity.Department;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.EmployeeAssessmentScore;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.QuestionScore;
import com.agilecheckup.service.AnswerService;
import com.agilecheckup.service.AssessmentMatrixService;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.DashboardAnalyticsService;
import com.agilecheckup.service.DepartmentService;
import com.agilecheckup.service.EmployeeAssessmentService;
import com.agilecheckup.service.PerformanceCycleService;
import com.agilecheckup.service.QuestionService;
import com.agilecheckup.service.TeamService;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DashboardAnalyticsTableRunner implements CrudRunner {

  private DashboardAnalyticsService dashboardAnalyticsService;
  private AssessmentMatrixService assessmentMatrixService;
  private EmployeeAssessmentService employeeAssessmentService;
  private AnswerService answerService;
  private CompanyService companyService;
  private PerformanceCycleService performanceCycleService;
  private DepartmentService departmentService;
  private TeamService teamService;
  private QuestionService questionService;

  private final TableRunnerHelper tableRunnerHelper = new TableRunnerHelper();
  private final boolean shouldCleanAfterComplete;

  // Test data tracking for cleanup
  private final List<DashboardAnalytics> createdAnalytics = new ArrayList<>();
  private final List<EmployeeAssessment> createdAssessments = new ArrayList<>();
  private final List<Answer> createdAnswers = new ArrayList<>();
  private final List<Question> createdQuestions = new ArrayList<>();
  private Company testCompany;
  private PerformanceCycle testPerformanceCycle;
  private Department testDepartment;
  private Team testTeam1;
  private Team testTeam2;
  private AssessmentMatrix testAssessmentMatrix;
  private final String testTenantId = "test-tenant-dashboard-v2-" + System.currentTimeMillis();

  public DashboardAnalyticsTableRunner(boolean shouldCleanAfterComplete) {
    this.shouldCleanAfterComplete = shouldCleanAfterComplete;
  }

  @Override
  public void run() {
    log.info("\n=== DashboardAnalytics Comprehensive Service Operations Demo ===");

    try {
      // 1. Setup test dependencies
      setupTestDependencies();

      if (testCompany == null || testPerformanceCycle == null || testAssessmentMatrix == null) {
        log.error("Failed to create test dependencies. Aborting demo.");
        return;
      }

      // 2. Create test data (assessments, questions, answers)
      log.info("\n1. Creating test data (assessments, questions, answers)...");
      createTestData();

      // 3. Test Analytics Generation
      log.info("\n2. Testing Analytics Generation...");
      testAnalyticsGeneration();

      // 4. Test Read Operations
      log.info("\n3. Testing Read Operations...");
      testReadOperations();

      // 5. Test Query Operations
      log.info("\n4. Testing Query Operations...");
      testQueryOperations();

      // 6. Test Update Analytics
      log.info("\n5. Testing Analytics Updates...");
      testAnalyticsUpdates();

      // 7. Test Data Verification
      log.info("\n6. Testing Data Verification...");
      testDataVerification();

      log.info("\n=== DashboardAnalytics Demo Completed Successfully ===");

    }
    catch (Exception e) {
      log.error("Error during DashboardAnalytics demo: {}", e.getMessage(), e);
    }
    finally {
      // 8. Cleanup all test data
      if (shouldCleanAfterComplete) {
        log.info("\n7. Cleaning up all test data...");
        cleanupAllTestData();
      }
    }
  }

  private void setupTestDependencies() {
    log.info("Setting up test dependencies (Company, Department, Teams, PerformanceCycle, AssessmentMatrix)...");

    // Create test company
    Optional<Company> companyOpt = getCompanyService().create(
                                                              "12345678901", "Test Company Dashboard  - " + System.currentTimeMillis(), "testdashboard@company.com", "Test company for DashboardAnalytics demo", testTenantId
    );

    if (companyOpt.isPresent()) {
      testCompany = companyOpt.get();
      log.info("Created test company: {} (ID: {})", testCompany.getName(), testCompany.getId());
    }
    else {
      log.error("Failed to create test company");
      return;
    }

    // Create test department
    Optional<Department> departmentOpt = getDepartmentService().create(
                                                                       "Test Department Dashboard ", "Test department for DashboardAnalytics demo", testTenantId, testCompany.getId()
    );

    if (departmentOpt.isPresent()) {
      testDepartment = departmentOpt.get();
      log.info("Created test department: {} (ID: {})", testDepartment.getName(), testDepartment.getId());
    }
    else {
      log.error("Failed to create test department");
      return;
    }

    // Create test teams
    Optional<Team> team1Opt = getTeamService().create(
                                                      "Development Team", "Software development team", testTenantId, testDepartment.getId()
    );

    Optional<Team> team2Opt = getTeamService().create(
                                                      "QA Team", "Quality assurance team", testTenantId, testDepartment.getId()
    );

    if (team1Opt.isPresent() && team2Opt.isPresent()) {
      testTeam1 = team1Opt.get();
      testTeam2 = team2Opt.get();
      log.info("Created test teams: {} (ID: {}), {} (ID: {})", testTeam1.getName(), testTeam1.getId(), testTeam2.getName(), testTeam2.getId());
    }
    else {
      log.error("Failed to create test teams");
      return;
    }

    // Create test performance cycle
    Optional<PerformanceCycle> cycleOpt = getPerformanceCycleService().create(
                                                                              testTenantId, "Test Cycle Dashboard  - " + System.currentTimeMillis(), "Test performance cycle for DashboardAnalytics demo", testCompany.getId(), true, true, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
    );

    if (cycleOpt.isPresent()) {
      testPerformanceCycle = cycleOpt.get();
      log.info("Created test performance cycle: {} (ID: {})", testPerformanceCycle.getName(), testPerformanceCycle.getId());
    }
    else {
      log.error("Failed to create test performance cycle");
      return;
    }

    // Create test assessment matrix
    Map<String, Pillar> pillarMap = tableRunnerHelper.createPillarsWithCategoriesMap();
    Optional<AssessmentMatrix> matrixOpt = getAssessmentMatrixService().create(
                                                                               "Test Assessment Matrix Dashboard ", "Assessment matrix for DashboardAnalytics demo", testTenantId, testPerformanceCycle.getId(), pillarMap
    );

    if (matrixOpt.isPresent()) {
      testAssessmentMatrix = matrixOpt.get();
      log.info("Created test assessment matrix: {} (ID: {})", testAssessmentMatrix.getName(), testAssessmentMatrix.getId());
    }
    else {
      log.error("Failed to create test assessment matrix");
    }
  }

  private void createTestData() {
    log.info("Creating comprehensive test data for analytics generation...");

    // Create test questions
    createTestQuestions();

    // Create employee assessments for both teams
    createEmployeeAssessments();

    // Create answers for the assessments
    createTestAnswers();

    log.info("Test data creation completed. Created {} assessments, {} questions, {} answers", createdAssessments.size(), createdQuestions.size(), createdAnswers.size());
  }

  private void createTestQuestions() {
    log.info("Creating test questions for analytics...");

    // Get first pillar and category for testing
    Pillar firstPillar = testAssessmentMatrix.getPillarMap().values().iterator().next();
    Category firstCategory = firstPillar.getCategoryMap().values().iterator().next();

    // Create different types of questions
    String[] questionTexts = {"How would you rate team collaboration?", "Is the team following agile practices?", "Rate the code quality from 1 to 10", "What improvements would you suggest?", "Which methodology works best for your team?"
    };

    QuestionType[] questionTypes = {QuestionType.STAR_FIVE, QuestionType.YES_NO, QuestionType.ONE_TO_TEN, QuestionType.OPEN_ANSWER, QuestionType.CUSTOMIZED
    };

    for (int i = 0; i < questionTexts.length; i++) {
      if (questionTypes[i] == QuestionType.CUSTOMIZED) {
        // Create custom question with options
        List<QuestionOption> options = createMockedQuestionOptionList("Method", 10.0, 20.0, 30.0);
        Optional<Question> questionOpt = getQuestionService().createCustomQuestion(
                                                                                   questionTexts[i], questionTypes[i], testTenantId, false, // single choice
                                                                                   false, // not flushed
                                                                                   options, testAssessmentMatrix.getId(), firstPillar.getId(), firstCategory.getId(), "Test custom question for analytics"
        );

        if (questionOpt.isPresent()) {
          createdQuestions.add(questionOpt.get());
          log.info("✓ Created custom question: {}", questionTexts[i]);
        }
      }
      else {
        // Create regular question
        Optional<Question> questionOpt = getQuestionService().create(
                                                                     questionTexts[i], questionTypes[i], testTenantId, (i + 1) * 20.0, // 20, 40, 60, 80 points
                                                                     testAssessmentMatrix.getId(), firstPillar.getId(), firstCategory.getId(), "Test question for analytics"
        );

        if (questionOpt.isPresent()) {
          createdQuestions.add(questionOpt.get());
          log.info("✓ Created {} question: {}", questionTypes[i], questionTexts[i]);
        }
      }
    }

    log.info("Created {} test questions", createdQuestions.size());
  }

  private void createEmployeeAssessments() {
    log.info("Creating employee assessments for multiple teams...");

    // Create assessments for Team 1 (Development Team)
    String[] devEmployees = {"Alice Developer", "Bob Senior Dev", "Charlie Junior Dev"};
    String[] devEmails = {"alice@test.com", "bob@test.com", "charlie@test.com"};

    for (int i = 0; i < devEmployees.length; i++) {
      NaturalPerson employee = createTestEmployee(devEmployees[i], devEmails[i], "1234567890" + i);
      Optional<EmployeeAssessment> assessmentOpt = getEmployeeAssessmentService().create(
                                                                                         testAssessmentMatrix.getId(), testTeam1.getId(), employee.getName(), employee.getEmail(), employee.getDocumentNumber(), employee.getPersonDocumentType(), employee.getGender(), employee.getGenderPronoun()
      );

      if (assessmentOpt.isPresent()) {
        createdAssessments.add(assessmentOpt.get());
        log.info("✓ Created assessment for {}: {}", devEmployees[i], assessmentOpt.get().getId());
      }
    }

    // Create assessments for Team 2 (QA Team)
    String[] qaEmployees = {"Diana QA Lead", "Eve Test Engineer"};
    String[] qaEmails = {"diana@test.com", "eve@test.com"};

    for (int i = 0; i < qaEmployees.length; i++) {
      NaturalPerson employee = createTestEmployee(qaEmployees[i], qaEmails[i], "2234567890" + i);
      Optional<EmployeeAssessment> assessmentOpt = getEmployeeAssessmentService().create(
                                                                                         testAssessmentMatrix.getId(), testTeam2.getId(), employee.getName(), employee.getEmail(), employee.getDocumentNumber(), employee.getPersonDocumentType(), employee.getGender(), employee.getGenderPronoun()
      );

      if (assessmentOpt.isPresent()) {
        createdAssessments.add(assessmentOpt.get());
        log.info("✓ Created assessment for {}: {}", qaEmployees[i], assessmentOpt.get().getId());
      }
    }

    log.info("Created {} employee assessments", createdAssessments.size());
  }

  private void createTestAnswers() {
    log.info("Creating test answers for assessments...");

    if (createdQuestions.isEmpty() || createdAssessments.isEmpty()) {
      log.warn("No questions or assessments available for answer creation");
      return;
    }

    LocalDateTime answeredAt = LocalDateTime.now().minusHours(2);

    // Create answers for each assessment
    for (EmployeeAssessment assessment : createdAssessments) {
      for (int i = 0; i < createdQuestions.size(); i++) {
        Question question = createdQuestions.get(i);
        String value = generateAnswerValue(question.getQuestionType(), assessment, i);

        Optional<Answer> answerOpt = getAnswerService().create(
                                                               assessment.getId(), question.getId(), answeredAt.plusMinutes(i * 5), value, testTenantId, "Test answer for analytics - " + assessment.getEmployee()
                                                                                                                                                                                                    .getName()
        );

        if (answerOpt.isPresent()) {
          createdAnswers.add(answerOpt.get());
        }
      }

      // Mark assessment as completed
      assessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
      // Simulate scoring
      assessment.setEmployeeAssessmentScore(createMockEmployeeAssessmentScore(assessment));
    }

    log.info("Created {} test answers", createdAnswers.size());
  }

  private String generateAnswerValue(QuestionType questionType, EmployeeAssessment assessment, int questionIndex) {
    // Generate varied answers based on team and employee
    boolean isTeam1 = assessment.getTeamId().equals(testTeam1.getId());
    String employeeName = assessment.getEmployee().getName();

    switch (questionType) {
      case YES_NO:
        return (isTeam1 && employeeName.contains("Senior")) ? "Yes" : "No";
      case STAR_FIVE:
        return isTeam1 ? "4" : "3";
      case ONE_TO_TEN:
        return isTeam1 ? String.valueOf(7 + questionIndex) : String.valueOf(5 + questionIndex);
      case OPEN_ANSWER:
        return isTeam1 ? "The development team works well together with good collaboration and communication practices." : "The QA team needs better integration with development processes and more automated testing tools.";
      case CUSTOMIZED:
        return "1"; // First option
      default:
        return "Default Answer";
    }
  }

  private EmployeeAssessmentScore createMockEmployeeAssessmentScore(EmployeeAssessment assessment) {
    // Create realistic scores based on team
    boolean isTeam1 = assessment.getTeamId().equals(testTeam1.getId());
    double baseScore = isTeam1 ? 85.0 : 75.0;

    // Add variation based on employee name
    String name = assessment.getEmployee().getName();
    if (name.contains("Senior") || name.contains("Lead")) {
      baseScore += 10.0;
    }
    else if (name.contains("Junior")) {
      baseScore -= 5.0;
    }

    Map<String, PillarScore> pillarScoreMap = new HashMap<>();

    // Create scores for all pillars with category breakdowns
    for (Map.Entry<String, Pillar> pillarEntry : testAssessmentMatrix.getPillarMap().entrySet()) {
      String pillarId = pillarEntry.getKey();
      Pillar pillar = pillarEntry.getValue();

      Map<String, CategoryScore> categoryScoreMap = new HashMap<>();
      double pillarTotalScore = 0.0;

      // Create scores for all categories in this pillar
      for (Map.Entry<String, Category> categoryEntry : pillar.getCategoryMap().entrySet()) {
        String categoryId = categoryEntry.getKey();
        Category category = categoryEntry.getValue();

        // Create question scores for this category
        List<QuestionScore> questionScores = new ArrayList<>();
        double categoryScore = baseScore - (Math.random() * 10.0); // Add some variation

        for (int i = 0; i < 3; i++) { // Mock 3 questions per category
          questionScores.add(QuestionScore.builder()
                                          .questionId("question-" + categoryId + "-" + i)
                                          .score(categoryScore / 3.0)
                                          .build());
        }

        categoryScoreMap.put(categoryId, CategoryScore.builder()
                                                      .categoryId(categoryId)
                                                      .categoryName(category.getName())
                                                      .score(categoryScore)
                                                      .questionScores(questionScores)
                                                      .build());

        pillarTotalScore += categoryScore;
      }

      pillarScoreMap.put(pillarId, PillarScore.builder()
                                              .pillarId(pillarId)
                                              .pillarName(pillar.getName())
                                              .score(pillarTotalScore)
                                              .categoryIdToCategoryScoreMap(categoryScoreMap)
                                              .build());
    }

    // Calculate total score from all pillars
    double totalScore = pillarScoreMap.values().stream().mapToDouble(PillarScore::getScore).sum();

    return EmployeeAssessmentScore.builder().score(totalScore).pillarIdToPillarScoreMap(pillarScoreMap).build();
  }

  private void testAnalyticsGeneration() {
    log.info("Testing analytics generation for assessment matrix...");

    try {
      // Generate analytics for the assessment matrix
      getDashboardAnalyticsService().updateAssessmentMatrixAnalytics(testAssessmentMatrix.getId());
      log.info("✓ Successfully triggered analytics generation for matrix: {}", testAssessmentMatrix.getId());

      // Give some time for analytics to be computed and stored
      Thread.sleep(1000);

    }
    catch (Exception e) {
      log.error("✗ Failed to generate analytics: {}", e.getMessage(), e);
    }
  }

  private void testReadOperations() {
    log.info("Testing read operations for dashboard analytics...");

    // Test get overview
    Optional<DashboardAnalytics> overviewOpt = getDashboardAnalyticsService().getOverview(testAssessmentMatrix.getId());
    if (overviewOpt.isPresent()) {
      DashboardAnalytics overview = overviewOpt.get();
      log.info("✓ Found overview analytics:");
      log.info("  Scope: {}", overview.getScope());
      log.info("  Employee Count: {}", overview.getEmployeeCount());
      log.info("  Completion Percentage: {}%", overview.getCompletionPercentage());
      log.info("  General Average: {}", overview.getGeneralAverage());
      log.info("  Company: {}", overview.getCompanyName());
      log.info("  Performance Cycle: {}", overview.getPerformanceCycleName());
      log.info("  Assessment Matrix: {}", overview.getAssessmentMatrixName());
      createdAnalytics.add(overview);
    }
    else {
      log.warn("✗ No overview analytics found");
    }

    // Test get team analytics
    Optional<DashboardAnalytics> team1AnalyticsOpt = getDashboardAnalyticsService().getTeamAnalytics(
                                                                                                     testAssessmentMatrix.getId(), testTeam1.getId());
    if (team1AnalyticsOpt.isPresent()) {
      DashboardAnalytics team1Analytics = team1AnalyticsOpt.get();
      log.info("✓ Found team analytics for {}:", team1Analytics.getTeamName());
      log.info("  Employee Count: {}", team1Analytics.getEmployeeCount());
      log.info("  Completion Percentage: {}%", team1Analytics.getCompletionPercentage());
      log.info("  General Average: {}", team1Analytics.getGeneralAverage());
      createdAnalytics.add(team1Analytics);
    }
    else {
      log.warn("✗ No team analytics found for team1");
    }

    Optional<DashboardAnalytics> team2AnalyticsOpt = getDashboardAnalyticsService().getTeamAnalytics(
                                                                                                     testAssessmentMatrix.getId(), testTeam2.getId());
    if (team2AnalyticsOpt.isPresent()) {
      DashboardAnalytics team2Analytics = team2AnalyticsOpt.get();
      log.info("✓ Found team analytics for {}:", team2Analytics.getTeamName());
      log.info("  Employee Count: {}", team2Analytics.getEmployeeCount());
      log.info("  Completion Percentage: {}%", team2Analytics.getCompletionPercentage());
      log.info("  General Average: {}", team2Analytics.getGeneralAverage());
      createdAnalytics.add(team2Analytics);
    }
    else {
      log.warn("✗ No team analytics found for team2");
    }
  }

  private void testQueryOperations() {
    log.info("Testing query operations...");

    // Test get all analytics for assessment matrix
    List<DashboardAnalytics> allAnalytics = getDashboardAnalyticsService().getAllAnalytics(testAssessmentMatrix.getId());
    log.info("✓ Found {} total analytics for assessment matrix", allAnalytics.size());

    // Verify we have both team and overview analytics
    long overviewCount = allAnalytics.stream().filter(a -> a.getScope() == AnalyticsScope.ASSESSMENT_MATRIX).count();
    long teamCount = allAnalytics.stream().filter(a -> a.getScope() == AnalyticsScope.TEAM).count();

    log.info("  Overview analytics: {}", overviewCount);
    log.info("  Team analytics: {}", teamCount);

    if (overviewCount >= 1 && teamCount >= 2) {
      log.info("✓ Correct analytics distribution found");
    }
    else {
      log.warn("✗ Expected 1 overview + 2 team analytics, found {} overview + {} team", overviewCount, teamCount);
    }

    // Add all found analytics to our cleanup list
    for (DashboardAnalytics analytics : allAnalytics) {
      if (!createdAnalytics.contains(analytics)) {
        createdAnalytics.add(analytics);
      }
    }
  }

  private void testAnalyticsUpdates() {
    log.info("Testing analytics updates after data changes...");

    // Update some answers to simulate data changes
    if (!createdAnswers.isEmpty()) {
      Answer answerToUpdate = createdAnswers.get(0);
      String originalValue = answerToUpdate.getValue();

      // Update answer value
      String newValue = "Updated Value";
      if (answerToUpdate.getQuestionType() == QuestionType.STAR_FIVE) {
        newValue = "5"; // Increase rating
      }

      Optional<Answer> updatedAnswerOpt = getAnswerService().update(
                                                                    answerToUpdate.getId(), LocalDateTime.now(), newValue, "Updated for analytics testing"
      );

      if (updatedAnswerOpt.isPresent()) {
        log.info("✓ Updated answer: {} -> {}", originalValue, newValue);

        // Regenerate analytics
        getDashboardAnalyticsService().updateAssessmentMatrixAnalytics(testAssessmentMatrix.getId());
        log.info("✓ Regenerated analytics after data update");

        // Give time for processing
        try {
          Thread.sleep(500);
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        // Verify analytics were updated
        Optional<DashboardAnalytics> updatedOverviewOpt = getDashboardAnalyticsService().getOverview(testAssessmentMatrix.getId());
        if (updatedOverviewOpt.isPresent()) {
          log.info("✓ Analytics successfully updated after data change");
          log.info("  New general average: {}", updatedOverviewOpt.get().getGeneralAverage());
        }
      }
    }
  }

  private void testDataVerification() {
    log.info("Testing data verification and integrity...");

    if (createdAnalytics.isEmpty()) {
      log.warn("No analytics data to verify");
      return;
    }

    for (DashboardAnalytics analytics : createdAnalytics) {
      // Verify basic data integrity
      boolean isValid = true;
      List<String> issues = new ArrayList<>();

      if (analytics.getCompanyId() == null || !analytics.getCompanyId().equals(testCompany.getId())) {
        issues.add("Invalid company ID");
        isValid = false;
      }

      if (analytics.getPerformanceCycleId() == null || !analytics.getPerformanceCycleId()
                                                                 .equals(testPerformanceCycle.getId())) {
        issues.add("Invalid performance cycle ID");
        isValid = false;
      }

      if (analytics.getAssessmentMatrixId() == null || !analytics.getAssessmentMatrixId()
                                                                 .equals(testAssessmentMatrix.getId())) {
        issues.add("Invalid assessment matrix ID");
        isValid = false;
      }

      if (analytics.getEmployeeCount() == null || analytics.getEmployeeCount() < 0) {
        issues.add("Invalid employee count");
        isValid = false;
      }

      if (analytics.getCompletionPercentage() == null || analytics.getCompletionPercentage() < 0 || analytics.getCompletionPercentage() > 100) {
        issues.add("Invalid completion percentage");
        isValid = false;
      }

      // Test category breakdowns within each pillar
      boolean categoriesValid = testCategoryBreakdowns(analytics);
      if (!categoriesValid) {
        issues.add("Missing category breakdowns within pillars");
        isValid = false;
      }

      if (isValid) {
        log.info("✓ Analytics data verification passed for {}: {}", analytics.getScope(), analytics.getScope() == AnalyticsScope.TEAM ? analytics.getTeamName() : "Overview");
      }
      else {
        log.warn("✗ Analytics data verification failed for {}: {}", analytics.getScope(), String.join(", ", issues));
      }
    }

    log.info("Data verification completed for {} analytics records", createdAnalytics.size());
  }

  private boolean testCategoryBreakdowns(DashboardAnalytics analytics) {
    log.info("Testing category breakdowns within pillars for {}: {}", analytics.getScope(), analytics.getScope() == AnalyticsScope.TEAM ? analytics.getTeamName() : "Overview");

    String analyticsDataJson = analytics.getAnalyticsDataJson();
    if (analyticsDataJson == null || analyticsDataJson.isEmpty() || "{}".equals(analyticsDataJson)) {
      log.warn("✗ No analytics data JSON found");
      return false;
    }

    try {
      // Parse the JSON to verify structure
      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      @SuppressWarnings("unchecked") Map<String, Object> analyticsData = mapper.readValue(analyticsDataJson, Map.class);

      // Check if pillars exist
      @SuppressWarnings("unchecked") Map<String, Object> pillars = (Map<String, Object>) analyticsData.get("pillars");
      if (pillars == null || pillars.isEmpty()) {
        log.warn("✗ No pillars found in analytics data");
        return false;
      }

      // Check each pillar for categories
      boolean allPillarsHaveCategories = true;
      int totalPillars = 0;
      int pillarsWithCategories = 0;
      int totalCategories = 0;

      for (Map.Entry<String, Object> pillarEntry : pillars.entrySet()) {
        String pillarId = pillarEntry.getKey();
        @SuppressWarnings("unchecked") Map<String, Object> pillarData = (Map<String, Object>) pillarEntry.getValue();

        totalPillars++;
        String pillarName = (String) pillarData.get("name");

        // Check if categories exist within this pillar
        @SuppressWarnings("unchecked") Map<String, Object> categories = (Map<String, Object>) pillarData.get("categories");
        if (categories == null || categories.isEmpty()) {
          log.warn("✗ No categories found within pillar '{}' ({})", pillarName, pillarId);
          allPillarsHaveCategories = false;
        }
        else {
          pillarsWithCategories++;
          int categoriesInThisPillar = categories.size();
          totalCategories += categoriesInThisPillar;

          log.info("✓ Found {} categories within pillar '{}' ({})", categoriesInThisPillar, pillarName, pillarId);

          // Verify each category has required fields
          for (Map.Entry<String, Object> categoryEntry : categories.entrySet()) {
            String categoryId = categoryEntry.getKey();
            @SuppressWarnings("unchecked") Map<String, Object> categoryData = (Map<String, Object>) categoryEntry.getValue();

            String categoryName = (String) categoryData.get("name");
            Double percentage = (Double) categoryData.get("percentage");
            Double actualScore = (Double) categoryData.get("actualScore");
            Double potentialScore = (Double) categoryData.get("potentialScore");

            if (categoryName == null || percentage == null || actualScore == null || potentialScore == null) {
              log.warn("✗ Incomplete category data for '{}' ({}) in pillar '{}'", categoryName, categoryId, pillarName);
              allPillarsHaveCategories = false;
            }
            else {
              log.info("  ✓ Category '{}' ({}): {}% ({}/{} points)", categoryName, categoryId, percentage, actualScore, potentialScore);
            }
          }
        }
      }

      log.info("Category breakdown verification results:");
      log.info("  Total pillars: {}", totalPillars);
      log.info("  Pillars with categories: {}", pillarsWithCategories);
      log.info("  Total categories found: {}", totalCategories);

      if (allPillarsHaveCategories && totalCategories > 0) {
        log.info("✓ All pillars have category breakdowns with valid data");
        return true;
      }
      else {
        log.warn("✗ Category breakdown verification failed");
        return false;
      }

    }
    catch (Exception e) {
      log.error("✗ Error parsing analytics data JSON for category verification: {}", e.getMessage());
      return false;
    }
  }

  private NaturalPerson createTestEmployee(String name, String email, String documentNumber) {
    return NaturalPerson.builder()
                        .name(name)
                        .email(email)
                        .documentNumber(documentNumber)
                        .personDocumentType(PersonDocumentType.CPF)
                        .gender(Gender.MALE)
                        .genderPronoun(GenderPronoun.HE)
                        .build();
  }

  private List<QuestionOption> createMockedQuestionOptionList(String prefix, Double... points) {
    return IntStream.range(0, points.length)
                    .mapToObj(index -> createQuestionOption(index + 1, prefix, points[index]))
                    .collect(Collectors.toList());
  }

  private QuestionOption createQuestionOption(Integer id, String prefix, double points) {
    return QuestionOption.builder().id(id).text(prefix + " " + id).points(points).build();
  }

  private void cleanupAllTestData() {
    log.info("Starting comprehensive cleanup of all test data...");

    // Delete created answers
    for (Answer answer : createdAnswers) {
      try {
        boolean deleted = getAnswerService().deleteById(answer.getId());
        if (deleted) {
          log.info("✓ Cleaned up answer: {}", answer.getId());
        }
        else {
          log.warn("✗ Failed to clean up answer: {}", answer.getId());
        }
      }
      catch (Exception e) {
        log.error("Error cleaning up answer {}: {}", answer.getId(), e.getMessage());
      }
    }

    // Delete created employee assessments
    for (EmployeeAssessment assessment : createdAssessments) {
      try {
        boolean deleted = getEmployeeAssessmentService().deleteById(assessment.getId());
        if (deleted) {
          log.info("✓ Cleaned up employee assessment: {}", assessment.getId());
        }
        else {
          log.warn("✗ Failed to clean up employee assessment: {}", assessment.getId());
        }
      }
      catch (Exception e) {
        log.error("Error cleaning up employee assessment {}: {}", assessment.getId(), e.getMessage());
      }
    }

    // Delete created questions
    for (Question question : createdQuestions) {
      try {
        getQuestionService().deleteById(question.getId());
        log.info("✓ Cleaned up question: {}", question.getQuestion());
      }
      catch (Exception e) {
        log.error("Error cleaning up question {}: {}", question.getQuestion(), e.getMessage());
      }
    }

    // Delete created analytics explicitly (even though they may be auto-cleaned)
    for (DashboardAnalytics analytics : createdAnalytics) {
      try {
        getDashboardAnalyticsService().deleteById(analytics.getCompanyPerformanceCycleId(), analytics.getAssessmentMatrixScopeId());
        log.info("✓ Cleaned up analytics: {}", analytics.getAssessmentMatrixScopeId());
      }
      catch (Exception e) {
        log.error("Error cleaning up analytics {}: {}", analytics.getAssessmentMatrixScopeId(), e.getMessage());
      }
    }

    // Delete test assessment matrix
    if (testAssessmentMatrix != null) {
      try {
        boolean deleted = getAssessmentMatrixService().deleteById(testAssessmentMatrix.getId());
        if (deleted) {
          log.info("✓ Cleaned up assessment matrix: {}", testAssessmentMatrix.getName());
        }
        else {
          log.warn("✗ Failed to clean up assessment matrix: {}", testAssessmentMatrix.getName());
        }
      }
      catch (Exception e) {
        log.error("Error cleaning up assessment matrix: {}", e.getMessage());
      }
    }

    // Delete test performance cycle
    if (testPerformanceCycle != null) {
      try {
        boolean deleted = getPerformanceCycleService().deleteById(testPerformanceCycle.getId());
        if (deleted) {
          log.info("✓ Cleaned up performance cycle: {}", testPerformanceCycle.getName());
        }
        else {
          log.warn("✗ Failed to clean up performance cycle: {}", testPerformanceCycle.getName());
        }
      }
      catch (Exception e) {
        log.error("Error cleaning up performance cycle: {}", e.getMessage());
      }
    }

    // Delete test teams
    if (testTeam1 != null) {
      try {
        boolean deleted = getTeamService().deleteById(testTeam1.getId());
        if (deleted) {
          log.info("✓ Cleaned up team: {}", testTeam1.getName());
        }
        else {
          log.warn("✗ Failed to clean up team: {}", testTeam1.getName());
        }
      }
      catch (Exception e) {
        log.error("Error cleaning up team1: {}", e.getMessage());
      }
    }

    if (testTeam2 != null) {
      try {
        boolean deleted = getTeamService().deleteById(testTeam2.getId());
        if (deleted) {
          log.info("✓ Cleaned up team: {}", testTeam2.getName());
        }
        else {
          log.warn("✗ Failed to clean up team: {}", testTeam2.getName());
        }
      }
      catch (Exception e) {
        log.error("Error cleaning up team2: {}", e.getMessage());
      }
    }

    // Delete test department
    if (testDepartment != null) {
      try {
        boolean deleted = getDepartmentService().deleteById(testDepartment.getId());
        if (deleted) {
          log.info("✓ Cleaned up department: {}", testDepartment.getName());
        }
        else {
          log.warn("✗ Failed to clean up department: {}", testDepartment.getName());
        }
      }
      catch (Exception e) {
        log.error("Error cleaning up department: {}", e.getMessage());
      }
    }

    // Delete test company
    if (testCompany != null) {
      try {
        boolean deleted = getCompanyService().deleteById(testCompany.getId());
        if (deleted) {
          log.info("✓ Cleaned up company: {}", testCompany.getName());
        }
        else {
          log.warn("✗ Failed to clean up company: {}", testCompany.getName());
        }
      }
      catch (Exception e) {
        log.error("Error cleaning up company: {}", e.getMessage());
      }
    }

    log.info("Cleanup completed. Cleaned {} analytics, {} assessments, {} answers, {} questions, 1 matrix, 1 cycle, 2 teams, 1 department, 1 company", createdAnalytics.size(), createdAssessments.size(), createdAnswers.size(), createdQuestions.size());

    // Clear all tracking lists
    createdAnalytics.clear();
    createdAssessments.clear();
    createdAnswers.clear();
    createdQuestions.clear();
  }

  // Service getters with lazy initialization
  private DashboardAnalyticsService getDashboardAnalyticsService() {
    if (dashboardAnalyticsService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      dashboardAnalyticsService = serviceComponent.buildDashboardAnalyticsService();
    }
    return dashboardAnalyticsService;
  }

  private AssessmentMatrixService getAssessmentMatrixService() {
    if (assessmentMatrixService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      assessmentMatrixService = serviceComponent.buildAssessmentMatrixService();
    }
    return assessmentMatrixService;
  }

  private EmployeeAssessmentService getEmployeeAssessmentService() {
    if (employeeAssessmentService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      employeeAssessmentService = serviceComponent.buildEmployeeAssessmentService();
    }
    return employeeAssessmentService;
  }

  private AnswerService getAnswerService() {
    if (answerService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      answerService = serviceComponent.buildAnswerService();
    }
    return answerService;
  }

  private CompanyService getCompanyService() {
    if (companyService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      companyService = serviceComponent.buildCompanyService();
    }
    return companyService;
  }

  private PerformanceCycleService getPerformanceCycleService() {
    if (performanceCycleService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      performanceCycleService = serviceComponent.buildPerformanceCycleService();
    }
    return performanceCycleService;
  }

  private DepartmentService getDepartmentService() {
    if (departmentService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      departmentService = serviceComponent.buildDepartmentService();
    }
    return departmentService;
  }

  private TeamService getTeamService() {
    if (teamService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      teamService = serviceComponent.buildTeamService();
    }
    return teamService;
  }

  private QuestionService getQuestionService() {
    if (questionService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      questionService = serviceComponent.buildQuestionService();
    }
    return questionService;
  }
}
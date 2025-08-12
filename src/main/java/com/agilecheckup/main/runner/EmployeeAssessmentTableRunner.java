package com.agilecheckup.main.runner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.Department;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.service.AnswerService;
import com.agilecheckup.service.AssessmentMatrixService;
import com.agilecheckup.service.AssessmentNavigationService;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.DepartmentService;
import com.agilecheckup.service.EmployeeAssessmentService;
import com.agilecheckup.service.PerformanceCycleService;
import com.agilecheckup.service.QuestionService;
import com.agilecheckup.service.TeamService;
import com.agilecheckup.service.dto.AnswerWithProgressResponse;
import com.agilecheckup.service.dto.EmployeeValidationRequest;
import com.agilecheckup.service.dto.EmployeeValidationResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EmployeeAssessmentTableRunner implements CrudRunner {

  private EmployeeAssessmentService employeeAssessmentService;
  private AssessmentMatrixService assessmentMatrixService;
  private TeamService teamService;
  private CompanyService companyService;
  private DepartmentService departmentService;
  private PerformanceCycleService performanceCycleService;
  private AnswerService answerService;
  private QuestionService questionService;
  private AssessmentNavigationService assessmentNavigationService;
  private TableRunnerHelper tableRunnerHelper;
  private Company testCompany;
  private Department testDepartment;
  private Team testTeam;
  private PerformanceCycle testPerformanceCycle;
  private AssessmentMatrix testAssessmentMatrix;
  private Map<String, Pillar> testPillarMap;
  private List<Question> testQuestions;
  private List<Answer> testAnswers;
  private String testTenantId;
  private final boolean shouldCleanAfterComplete;

  public EmployeeAssessmentTableRunner(boolean shouldCleanAfterComplete) {
    this.shouldCleanAfterComplete = shouldCleanAfterComplete;
  }

  public void run() {
    log.info("\n=== EmployeeAssessment  Migration Demo ===");

    try {
      // 1. Setup test data (Company, Department, Team, PerformanceCycle, AssessmentMatrix for FK relationships)
      setupTestData();

      if (testCompany == null || testDepartment == null || testTeam == null || testPerformanceCycle == null || testAssessmentMatrix == null) {
        log.error("Failed to create test dependencies. Aborting demo.");
        return;
      }

      // 2. Demonstrate  CRUD operations
      log.info("\n1. Demonstrating  EmployeeAssessment operations...");
      List<EmployeeAssessment> v2Assessments = demonstrateOperations();

      // 3. Demonstrate GSI query capabilities
      log.info("\n2. Demonstrating  GSI query capabilities...");
      demonstrateQueryCapabilities();

      // 4. Demonstrate business logic (validation, status transitions, scoring)
      log.info("\n3. Demonstrating business logic...");
      demonstrateBusinessLogic(v2Assessments);

      // 5. Demonstrate save-and-next flow
      log.info("\n4. Demonstrating save-and-next answer flow...");
      demonstrateSaveAndNextFlow(v2Assessments);

      // 6. Demonstrate employee validation workflow
      log.info("\n5. Demonstrating employee validation workflow...");
      demonstrateEmployeeValidation();

      // 7. Show side-by-side comparison with V1 if available
      log.info("\n6. Side-by-side V1 vs  comparison:");
      demonstrateSideBySideComparison();

      // 8. Cleanup
      if (shouldCleanAfterComplete) {
        log.info("\n7. Cleaning up test data...");
        cleanupTestData(v2Assessments);
      }

    }
    catch (Exception e) {
      log.error("Error in EmployeeAssessmentTableRunner: {}", e.getMessage(), e);
    }
    finally {
      // Always cleanup test dependencies
      cleanupTestDependencies();
    }

    log.info("\n=== EmployeeAssessment  Migration Demo Complete ===");
  }

  private void setupTestData() {
    log.info("Setting up test dependencies (Company, Department, Team, PerformanceCycle, AssessmentMatrix)...");

    // Create test company
    Optional<Company> companyOpt = getCompanyService().create(
        "12345678000199",  // documentNumber
        "EmployeeAssessment Test Company",  // name
        "ea-test@company.com",  // email
        "Company for EmployeeAssessment  testing",  // description
        "ea-test-tenant-" + System.currentTimeMillis()  // unique tenantId
    );

    if (companyOpt.isPresent()) {
      testCompany = companyOpt.get();
      testTenantId = testCompany.getTenantId();
      log.info("Created test company: {} (Tenant: {})", testCompany.getName(), testTenantId);

      // Create test department
      Optional<Department> departmentOpt = getDepartmentService().create(
          "Test Department", "Department for EmployeeAssessment testing", testTenantId, testCompany.getId()
      );

      if (departmentOpt.isPresent()) {
        testDepartment = departmentOpt.get();
        log.info("Created test department: {} (ID: {})", testDepartment.getName(), testDepartment.getId());

        // Create test team
        Optional<Team> teamOpt = getTeamService().create(
            "Test Team", "Team for EmployeeAssessment testing", testTenantId, testDepartment.getId()  // Use department ID, not company ID
        );

        if (teamOpt.isPresent()) {
          testTeam = teamOpt.get();
          log.info("Created test team: {} (ID: {})", testTeam.getName(), testTeam.getId());

          // Create test performance cycle
          Optional<PerformanceCycle> performanceCycleOpt = getPerformanceCycleService().create(
              testTenantId, "Test Performance Cycle", "Performance cycle for EmployeeAssessment testing", testCompany.getId(), true,  // isActive
              false,  // isTimeSensitive
              LocalDate.now(),  // startDate
              null  // endDate (null means not time sensitive)
          );

          if (performanceCycleOpt.isPresent()) {
            testPerformanceCycle = performanceCycleOpt.get();
            log.info("Created test performance cycle: {} (ID: {})", testPerformanceCycle.getName(), testPerformanceCycle.getId());

            // Create test pillars and categories first
            log.info("About to create test pillars and categories...");
            try {
              createTestPillarsAndCategories();
              log.info("Completed creating test pillars and categories");
            }
            catch (Exception e) {
              log.error("Failed to create test pillars and categories: {}", e.getMessage(), e);
              return;
            }

            // Create test assessment matrix with pillarMap
            Optional<AssessmentMatrix> matrixOpt = getAssessmentMatrixService().create(
                "Test Assessment Matrix", "Assessment matrix for EmployeeAssessment testing", testTenantId, testPerformanceCycle.getId(), testPillarMap  // Use the created pillarMap
            );

            if (matrixOpt.isPresent()) {
              testAssessmentMatrix = matrixOpt.get();
              log.info("Created test assessment matrix: {} (ID: {})", testAssessmentMatrix.getName(), testAssessmentMatrix.getId());

              // Create test questions for the assessment matrix
              try {
                createTestQuestions();
              }
              catch (Exception e) {
                log.error("Failed to create test questions: {}", e.getMessage(), e);
                return;
              }
            }
            else {
              log.error("Failed to create test assessment matrix");
            }
          }
          else {
            log.error("Failed to create test performance cycle");
          }
        }
        else {
          log.error("Failed to create test team");
        }
      }
      else {
        log.error("Failed to create test department");
      }
    }
    else {
      log.error("Failed to create test company");
    }
  }

  private void createTestPillarsAndCategories() {
    log.info("Creating test pillars and categories...");

    // In , pillars and categories are embedded within the AssessmentMatrix
    // We use TableRunnerHelper to create a standard pillarMap structure
    testPillarMap = getTableRunnerHelper().createPillarsWithCategoriesMap();

    log.info("Created pillar map with {} pillars", testPillarMap.size());
    for (Map.Entry<String, Pillar> entry : testPillarMap.entrySet()) {
      Pillar pillar = entry.getValue();
      log.info("✓ Pillar: {} (ID: {}) with {} categories", pillar.getName(), entry.getKey(), pillar.getCategoryMap().size());

      for (Map.Entry<String, Category> catEntry : pillar.getCategoryMap().entrySet()) {
        Category category = catEntry.getValue();
        log.info("  - Category: {} (ID: {})", category.getName(), catEntry.getKey());
      }
    }
  }

  private void createTestQuestions() {
    log.info("Creating test questions for assessment matrix...");
    testQuestions = new ArrayList<>();

    if (testPillarMap == null || testPillarMap.isEmpty()) {
      log.error("Cannot create questions - pillar map is empty or null");
      return;
    }

    // Get pillar and category IDs from the pillarMap
    String[] pillarIds = testPillarMap.keySet().toArray(new String[0]);
    if (pillarIds.length < 2) {
      log.error("Need at least 2 pillars to create test questions");
      return;
    }

    Pillar pillar1 = testPillarMap.get(pillarIds[0]);
    Pillar pillar2 = testPillarMap.get(pillarIds[1]);

    String[] categories1 = pillar1.getCategoryMap().keySet().toArray(new String[0]);
    String[] categories2 = pillar2.getCategoryMap().keySet().toArray(new String[0]);

    if (categories1.length < 2 || categories2.length < 1) {
      log.error("Need at least 2 categories in pillar 1 and 1 in pillar 2");
      return;
    }

    log.info("Creating questions with {} pillars", testPillarMap.size());

    // Create different types of questions using actual pillar and category IDs
    Optional<Question> question1Opt = getQuestionService().create(
        "How well do you communicate with your team?", QuestionType.ONE_TO_TEN, testTenantId, 10.0, // points
        testAssessmentMatrix.getId(), pillarIds[0], categories1[0], null // extraDescription
    );

    Optional<Question> question2Opt = getQuestionService().create(
        "Do you make decisions effectively?", QuestionType.YES_NO, testTenantId, 5.0, testAssessmentMatrix.getId(), pillarIds[0], categories1[1], null
    );

    Optional<Question> question3Opt = getQuestionService().create(
        "Rate your problem-solving abilities", QuestionType.STAR_FIVE, testTenantId, 15.0, testAssessmentMatrix.getId(), pillarIds[1], categories2[0], null
    );

    if (question1Opt.isPresent()) {
      testQuestions.add(question1Opt.get());
      log.info("✓ Created question 1: {}", question1Opt.get().getQuestion());
    }
    if (question2Opt.isPresent()) {
      testQuestions.add(question2Opt.get());
      log.info("✓ Created question 2: {}", question2Opt.get().getQuestion());
    }
    if (question3Opt.isPresent()) {
      testQuestions.add(question3Opt.get());
      log.info("✓ Created question 3: {}", question3Opt.get().getQuestion());
    }

    log.info("Created {} test questions", testQuestions.size());
  }

  private List<EmployeeAssessment> demonstrateOperations() {
    List<EmployeeAssessment> assessments = new ArrayList<>();

    // Create  EmployeeAssessments
    log.info("Creating  EmployeeAssessments...");

    // Employee 1
    Optional<EmployeeAssessment> assessment1Opt = getEmployeeAssessmentService().create(
        testAssessmentMatrix.getId(), testTeam.getId(), "John Doe", "john.doe@example.com", "123456789", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
    );

    if (assessment1Opt.isPresent()) {
      EmployeeAssessment assessment1 = assessment1Opt.get();
      assessments.add(assessment1);
      log.info("✓  EmployeeAssessment created: {} (ID: {})", assessment1.getEmployee().getName(), assessment1.getId());
      log.info("  - Status: {}", assessment1.getAssessmentStatus());
      log.info("  - Email normalized: {}", assessment1.getEmployeeEmailNormalized());
      log.info("  - Answered questions: {}", assessment1.getAnsweredQuestionCount());
    }

    // Employee 2
    Optional<EmployeeAssessment> assessment2Opt = getEmployeeAssessmentService().create(
        testAssessmentMatrix.getId(), testTeam.getId(), "Jane Smith", "JANE.SMITH@EXAMPLE.COM",  // Test email normalization
        "987654321", PersonDocumentType.CPF, Gender.FEMALE, GenderPronoun.SHE
    );

    if (assessment2Opt.isPresent()) {
      EmployeeAssessment assessment2 = assessment2Opt.get();
      assessments.add(assessment2);
      log.info("✓  EmployeeAssessment created: {} (ID: {})", assessment2.getEmployee().getName(), assessment2.getId());
      log.info("  - Email normalized: {} (from uppercase)", assessment2.getEmployeeEmailNormalized());
    }

    // Test uniqueness constraint
    log.info("Testing uniqueness constraint (should fail)...");
    try {
      getEmployeeAssessmentService().create(
          testAssessmentMatrix.getId(), testTeam.getId(), "John Duplicate", "john.doe@example.com",  // Same email as first employee
          "111111111", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
      );
      log.error("❌ Uniqueness constraint failed - duplicate was allowed!");
    }
    catch (Exception e) {
      log.info("✓ Uniqueness constraint working: {}", e.getMessage());
    }

    // Update assessment
    if (!assessments.isEmpty()) {
      EmployeeAssessment toUpdate = assessments.get(0);
      log.info("Updating  EmployeeAssessment...");
      Optional<EmployeeAssessment> updatedOpt = getEmployeeAssessmentService().update(
          toUpdate.getId(), testAssessmentMatrix.getId(), testTeam.getId(), "John Updated Doe", "john.updated@example.com", "123456789", PersonDocumentType.CPF, Gender.MALE, GenderPronoun.HE
      );

      if (updatedOpt.isPresent()) {
        log.info("✓  EmployeeAssessment updated: {} -> {}", toUpdate.getEmployee().getName(), updatedOpt.get().getEmployee().getName());
      }
    }

    return assessments;
  }

  private void demonstrateQueryCapabilities() {
    // Test GSI queries
    log.info("Testing  GSI query capabilities...");

    // Query by tenant
    List<EmployeeAssessment> tenantAssessments = getEmployeeAssessmentService().findAllByTenantId(testTenantId);
    log.info("✓ Found {} assessments for tenant: {}", tenantAssessments.size(), testTenantId);

    // Query by assessment matrix
    List<EmployeeAssessment> matrixAssessments = getEmployeeAssessmentService().findByAssessmentMatrix(testAssessmentMatrix.getId(), testTenantId);
    log.info("✓ Found {} assessments for matrix: {}", matrixAssessments.size(), testAssessmentMatrix.getId());

    // Test existence query
    boolean exists = getEmployeeAssessmentService().getRepository().existsByAssessmentMatrixAndEmployeeEmail(testAssessmentMatrix.getId(), "john.updated@example.com");
    log.info("✓ Employee exists check: {}", exists);

    // Test case-insensitive existence
    boolean existsCase = getEmployeeAssessmentService().getRepository().existsByAssessmentMatrixAndEmployeeEmail(testAssessmentMatrix.getId(), "JOHN.UPDATED@EXAMPLE.COM");
    log.info("✓ Case-insensitive exists check: {}", existsCase);
  }

  private void demonstrateBusinessLogic(List<EmployeeAssessment> assessments) {
    if (assessments.isEmpty()) {
      log.warn("No assessments available for business logic demo");
      return;
    }

    EmployeeAssessment assessment = assessments.get(0);
    String assessmentId = assessment.getId();

    // Test status transitions
    log.info("Testing assessment status transitions...");

    // Initial status should be INVITED
    log.info("Initial status: {}", assessment.getAssessmentStatus());

    // Transition to CONFIRMED
    Optional<EmployeeAssessment> confirmedOpt = getEmployeeAssessmentService().updateAssessmentStatus(assessmentId, AssessmentStatus.CONFIRMED);
    if (confirmedOpt.isPresent()) {
      log.info("✓ Status transitioned to: {}", confirmedOpt.get().getAssessmentStatus());
    }

    // Test question count increment (will be done via save-and-next flow)
    log.info("Testing question count (will be incremented by save-and-next flow)...");
    log.info("Questions answered before save-and-next: {}", assessment.getAnsweredQuestionCount());

    // Test lastActivityDate update
    log.info("Testing lastActivityDate update...");
    getEmployeeAssessmentService().updateLastActivityDate(assessmentId);
    log.info("✓ LastActivityDate updated");
  }

  private void demonstrateSaveAndNextFlow(List<EmployeeAssessment> assessments) {
    if (assessments.isEmpty() || testQuestions.isEmpty()) {
      log.warn("No assessments or questions available for save-and-next demo");
      return;
    }

    EmployeeAssessment assessment = assessments.get(0);
    String assessmentId = assessment.getId();
    testAnswers = new ArrayList<>();

    log.info("Testing save-and-next flow for assessment: {}", assessmentId);
    log.info("Assessment has {} questions to answer", testQuestions.size());

    // Answer each question using save-and-next flow
    for (int i = 0; i < testQuestions.size(); i++) {
      Question question = testQuestions.get(i);
      String value = generateAnswerValue(question.getQuestionType(), i);

      log.info("Answering question {}/{}: {} (Type: {})", i + 1, testQuestions.size(), question.getQuestion(), question.getQuestionType());
      log.info("Answer value: {}", value);

      try {
        AnswerWithProgressResponse response = getAssessmentNavigationService().saveAnswerAndGetNext(
            assessmentId, question.getId(), LocalDateTime.now(), value, testTenantId, "Test answer notes for question " + (i + 1)
        );

        log.info("✓ Answer saved successfully");
        log.info("  - Progress: {}/{} questions answered", response.getCurrentProgress(), response.getTotalQuestions());

        if (response.getQuestion() != null) {
          log.info("  - Next question: {}", response.getQuestion().getQuestion());
        }
        else {
          log.info("  - Assessment completed - no more questions");
        }

        // Verify assessment status changes
        Optional<EmployeeAssessment> updatedAssessment = getEmployeeAssessmentService().findById(assessmentId);
        if (updatedAssessment.isPresent()) {
          EmployeeAssessment updated = updatedAssessment.get();
          log.info("  - Assessment status: {}", updated.getAssessmentStatus());
          log.info("  - Questions answered count: {}", updated.getAnsweredQuestionCount());

          if (updated.getEmployeeAssessmentScore() != null) {
            log.info("  - Assessment score calculated: {}", updated.getEmployeeAssessmentScore().getScore());
          }
        }

      }
      catch (Exception e) {
        log.error("❌ Failed to save answer for question {}: {}", i + 1, e.getMessage(), e);
        break;
      }
    }

    // Verify final state
    Optional<EmployeeAssessment> finalAssessment = getEmployeeAssessmentService().findById(assessmentId);
    if (finalAssessment.isPresent()) {
      EmployeeAssessment finalState = finalAssessment.get();
      log.info("Final assessment state:");
      log.info("  - Status: {}", finalState.getAssessmentStatus());
      log.info("  - Questions answered: {}/{}", finalState.getAnsweredQuestionCount(), testQuestions.size());
      log.info("  - Last activity: {}", finalState.getLastActivityDate());

      if (finalState.getEmployeeAssessmentScore() != null) {
        log.info("  - Total score: {}", finalState.getEmployeeAssessmentScore().getScore());
        log.info("  - Pillar scores: {}", finalState.getEmployeeAssessmentScore().getPillarIdToPillarScoreMap().size());
      }
    }

    // Get all answers created for cleanup
    List<Answer> allAnswers = getAnswerService().findByEmployeeAssessmentId(assessmentId, testTenantId);
    testAnswers.addAll(allAnswers);
    log.info("Created {} answers total for cleanup", testAnswers.size());
  }

  private String generateAnswerValue(QuestionType questionType, int questionIndex) {
    switch (questionType) {
      case YES_NO:
        return questionIndex % 2 == 0 ? "true" : "false";
      case ONE_TO_TEN:
        return String.valueOf((questionIndex % 10) + 1);
      case STAR_THREE:
        return String.valueOf((questionIndex % 3) + 1);
      case STAR_FIVE:
        return String.valueOf((questionIndex % 5) + 1);
      case GOOD_BAD:
        return questionIndex % 2 == 0 ? "true" : "false";
      case OPEN_ANSWER:
        return "This is a test open answer for question " + (questionIndex + 1);
      case CUSTOMIZED:
        return "Option " + ((questionIndex % 3) + 1);
      default:
        return "1";
    }
  }

  private void demonstrateEmployeeValidation() {
    log.info("Testing employee validation workflow...");

    // Test successful validation
    EmployeeValidationRequest request = new EmployeeValidationRequest();
    request.setAssessmentMatrixId(testAssessmentMatrix.getId());
    request.setTenantId(testTenantId);
    request.setEmail("john.updated@example.com");

    EmployeeValidationResponse response = getEmployeeAssessmentService().validateEmployee(request);
    log.info("✓ Validation result: Success={}, Message={}", "SUCCESS".equals(response.getStatus()), response.getMessage());
    log.info("  - Employee: {}, Status: {}", response.getName(), response.getAssessmentStatus());

    // Test validation with non-existent employee
    EmployeeValidationRequest notFoundRequest = new EmployeeValidationRequest();
    notFoundRequest.setAssessmentMatrixId(testAssessmentMatrix.getId());
    notFoundRequest.setTenantId(testTenantId);
    notFoundRequest.setEmail("nonexistent@example.com");

    EmployeeValidationResponse notFoundResponse = getEmployeeAssessmentService().validateEmployee(notFoundRequest);
    log.info("✓ Not found result: Success={}, Message={}", "SUCCESS".equals(notFoundResponse.getStatus()), notFoundResponse.getMessage());
  }

  private void demonstrateSideBySideComparison() {
    // Get  assessments to showcase features
    List<EmployeeAssessment> v2Assessments = getEmployeeAssessmentService().findAllByTenantId(testTenantId);

    if (!v2Assessments.isEmpty()) {
      EmployeeAssessment v2Assessment = v2Assessments.get(0);

      log.info(" EmployeeAssessment Features (Migration Complete):");
      log.info("  - ID: {}", v2Assessment.getId());
      log.info("  - Employee: {}", v2Assessment.getEmployee().getName());
      log.info("  - Email normalized: {}", v2Assessment.getEmployeeEmailNormalized());
      log.info("  - SDK Version: AWS SDK  Enhanced Client");
      log.info("  - Query Method: Efficient GSI queries");
      log.info("  - Attribute Converters: Custom JSON converters for complex types");
      log.info("  - Enhanced business logic and validation");
      log.info("  - Type-safe operations with enhanced error handling");
    }
    else {
      log.info(" EmployeeAssessment Migration Benefits:");
      log.info("  - AWS SDK  Enhanced Client with type-safe operations");
      log.info("  - Efficient GSI queries for existence checks and filtering");
      log.info("  - Custom attribute converters for complex types");
      log.info("  - Preserved business logic and method signatures");
      log.info("  - Enhanced error handling and logging");
      log.info("  - Complete removal of legacy V1 dependencies");
    }
  }

  private void cleanupTestData(List<EmployeeAssessment> assessments) {
    // Clean Answers first (FK dependency)
    if (testAnswers != null && !testAnswers.isEmpty()) {
      log.info("Cleaning up {} test answers...", testAnswers.size());
      for (Answer answer : testAnswers) {
        try {
          getAnswerService().deleteById(answer.getId());
          log.info("✓ Cleaned up answer: {}", answer.getId());
        }
        catch (Exception e) {
          log.warn("Failed to cleanup answer {}: {}", answer.getId(), e.getMessage());
        }
      }
    }

    // Clean any remaining answers from assessments
    for (EmployeeAssessment assessment : assessments) {
      try {
        List<Answer> remainingAnswers = getAnswerService().findByEmployeeAssessmentId(assessment.getId(), testTenantId);
        for (Answer answer : remainingAnswers) {
          getAnswerService().deleteById(answer.getId());
          log.info("✓ Cleaned up remaining answer: {}", answer.getId());
        }
      }
      catch (Exception e) {
        log.warn("Error cleaning remaining answers for assessment {}: {}", assessment.getId(), e.getMessage());
      }
    }

    // Clean  assessments
    for (EmployeeAssessment assessment : assessments) {
      try {
        getEmployeeAssessmentService().deleteById(assessment.getId());
        log.info("✓ Cleaned up  assessment: {}", assessment.getId());
      }
      catch (Exception e) {
        log.warn("Failed to cleanup  assessment {}: {}", assessment.getId(), e.getMessage());
      }
    }

    // Clean any remaining test assessments from the tenant
    try {
      List<EmployeeAssessment> remainingAssessments = getEmployeeAssessmentService().findAllByTenantId(testTenantId);
      for (EmployeeAssessment assessment : remainingAssessments) {
        getEmployeeAssessmentService().deleteById(assessment.getId());
        log.info("✓ Cleaned up remaining  assessment: {}", assessment.getId());
      }
    }
    catch (Exception e) {
      log.warn("Error cleaning remaining assessments: {}", e.getMessage());
    }
  }

  private void cleanupTestDependencies() {
    // Cleanup in reverse order of creation

    // Clean test questions first (FK dependency on assessment matrix pillars/categories)
    if (testQuestions != null && !testQuestions.isEmpty()) {
      log.info("Cleaning up {} test questions...", testQuestions.size());
      for (Question question : testQuestions) {
        try {
          getQuestionService().deleteById(question.getId());
          log.info("✓ Cleaned up question: {}", question.getId());
        }
        catch (Exception e) {
          log.warn("Failed to cleanup question {}: {}", question.getId(), e.getMessage());
        }
      }
    }

    // Note: In , pillars and categories are embedded in the AssessmentMatrix
    // They don't need separate cleanup - they'll be cleaned up with the matrix

    if (testAssessmentMatrix != null) {
      try {
        getAssessmentMatrixService().deleteById(testAssessmentMatrix.getId());
        log.info("✓ Cleaned up test assessment matrix: {}", testAssessmentMatrix.getId());
      }
      catch (Exception e) {
        log.warn("Failed to cleanup test assessment matrix {}: {}", testAssessmentMatrix.getId(), e.getMessage());
      }
    }

    if (testPerformanceCycle != null) {
      try {
        getPerformanceCycleService().deleteById(testPerformanceCycle.getId());
        log.info("✓ Cleaned up test performance cycle: {}", testPerformanceCycle.getId());
      }
      catch (Exception e) {
        log.warn("Failed to cleanup test performance cycle {}: {}", testPerformanceCycle.getId(), e.getMessage());
      }
    }

    if (testTeam != null) {
      try {
        getTeamService().deleteById(testTeam.getId());
        log.info("✓ Cleaned up test team: {}", testTeam.getId());
      }
      catch (Exception e) {
        log.warn("Failed to cleanup test team {}: {}", testTeam.getId(), e.getMessage());
      }
    }

    if (testDepartment != null) {
      try {
        getDepartmentService().deleteById(testDepartment.getId());
        log.info("✓ Cleaned up test department: {}", testDepartment.getId());
      }
      catch (Exception e) {
        log.warn("Failed to cleanup test department {}: {}", testDepartment.getId(), e.getMessage());
      }
    }

    if (testCompany != null) {
      try {
        getCompanyService().deleteById(testCompany.getId());
        log.info("✓ Cleaned up test company: {}", testCompany.getId());
      }
      catch (Exception e) {
        log.warn("Failed to cleanup test company {}: {}", testCompany.getId(), e.getMessage());
      }
    }
  }

  // Service getters with lazy initialization
  private EmployeeAssessmentService getEmployeeAssessmentService() {
    if (employeeAssessmentService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      employeeAssessmentService = serviceComponent.buildEmployeeAssessmentService();
    }
    return employeeAssessmentService;
  }


  private AssessmentMatrixService getAssessmentMatrixService() {
    if (assessmentMatrixService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      assessmentMatrixService = serviceComponent.buildAssessmentMatrixService();
    }
    return assessmentMatrixService;
  }

  private TeamService getTeamService() {
    if (teamService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      teamService = serviceComponent.buildTeamService();
    }
    return teamService;
  }

  private AnswerService getAnswerService() {
    if (answerService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      answerService = serviceComponent.buildAnswerService();
    }
    return answerService;
  }

  private QuestionService getQuestionService() {
    if (questionService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      questionService = serviceComponent.buildQuestionService();
    }
    return questionService;
  }

  private AssessmentNavigationService getAssessmentNavigationService() {
    if (assessmentNavigationService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      assessmentNavigationService = serviceComponent.buildAssessmentNavigationService();
    }
    return assessmentNavigationService;
  }

  private CompanyService getCompanyService() {
    if (companyService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      companyService = serviceComponent.buildCompanyService();
    }
    return companyService;
  }

  private DepartmentService getDepartmentService() {
    if (departmentService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      departmentService = serviceComponent.buildDepartmentService();
    }
    return departmentService;
  }

  private PerformanceCycleService getPerformanceCycleService() {
    if (performanceCycleService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      performanceCycleService = serviceComponent.buildPerformanceCycleService();
    }
    return performanceCycleService;
  }

  private TableRunnerHelper getTableRunnerHelper() {
    if (tableRunnerHelper == null) {
      tableRunnerHelper = new TableRunnerHelper();
    }
    return tableRunnerHelper;
  }
}
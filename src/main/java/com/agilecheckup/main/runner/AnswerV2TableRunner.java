package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.CategoryV2;
import com.agilecheckup.persistency.entity.CompanyV2;
import com.agilecheckup.persistency.entity.DepartmentV2;
import com.agilecheckup.persistency.entity.EmployeeAssessmentV2;
import com.agilecheckup.persistency.entity.PerformanceCycleV2;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.TeamV2;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPersonV2;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.entity.question.AnswerV2;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.service.AnswerServiceV2;
import com.agilecheckup.service.AssessmentMatrixServiceV2;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.DepartmentService;
import com.agilecheckup.service.EmployeeAssessmentServiceV2;
import com.agilecheckup.service.PerformanceCycleService;
import com.agilecheckup.service.QuestionService;
import com.agilecheckup.service.TeamService;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
public class AnswerV2TableRunner implements CrudRunner {

    private AnswerServiceV2 answerServiceV2;
    private EmployeeAssessmentServiceV2 employeeAssessmentServiceV2;
    private QuestionService questionService;
    private AssessmentMatrixServiceV2 assessmentMatrixServiceV2;
    private PerformanceCycleService performanceCycleService;
    private CompanyService companyService;
    private DepartmentService departmentService;
    private TeamService teamService;

    private final TableRunnerHelper tableRunnerHelper = new TableRunnerHelper();
    private final boolean shouldCleanAfterComplete;
    
    // Test data tracking for cleanup
    private final List<AnswerV2> createdAnswers = new ArrayList<>();
    private final List<Question> createdQuestions = new ArrayList<>();
    private CompanyV2 testCompany;
    private DepartmentV2 testDepartment;
    private TeamV2 testTeam;
    private PerformanceCycleV2 testPerformanceCycle;
    private AssessmentMatrixV2 testAssessmentMatrix;
    private EmployeeAssessmentV2 testEmployeeAssessment;
    private final String testTenantId = "test-tenant-answer-v2-" + System.currentTimeMillis();

    public AnswerV2TableRunner(boolean shouldCleanAfterComplete) {
        this.shouldCleanAfterComplete = shouldCleanAfterComplete;
    }

    @Override
    public void run() {
        log.info("\n=== AnswerV2 Comprehensive Service Operations Demo ===");
        
        try {
            // 1. Setup test dependencies
            setupTestDependencies();
            
            if (testCompany == null || testPerformanceCycle == null || testAssessmentMatrix == null || testEmployeeAssessment == null) {
                log.error("Failed to create test dependencies. Aborting demo.");
                return;
            }
            
            // 2. Test Create Operations
            log.info("\n1. Testing V2 Create Operations...");
            testCreateOperations();
            
            // 3. Test Read Operations
            log.info("\n2. Testing V2 Read Operations...");
            testReadOperations();
            
            // 4. Test Update Operations
            log.info("\n3. Testing V2 Update Operations...");
            testUpdateOperations();
            
            // 5. Test Query Operations
            log.info("\n4. Testing V2 Query Operations...");
            testQueryOperations();
            
            // 6. Test Assessment Integration
            log.info("\n5. Testing Assessment Integration...");
            testAssessmentIntegration();
            
            // 7. Test Duplicate Prevention
            log.info("\n6. Testing Duplicate Prevention...");
            testDuplicatePrevention();
            
            // 8. Test Assessment Completion Logic
            log.info("\n7. Testing Assessment Completion Logic...");
            testAssessmentCompletion();
            
            // 9. Test Delete Operations
            log.info("\n8. Testing V2 Delete Operations...");
            testDeleteOperations();
            
            log.info("\n=== AnswerV2 Demo Completed Successfully ===");
            
        } catch (Exception e) {
            log.error("Error during AnswerV2 demo: {}", e.getMessage(), e);
        } finally {
            // 10. Cleanup all test data
            if (shouldCleanAfterComplete) {
                log.info("\n9. Cleaning up all test data...");
                cleanupAllTestData();
            }
        }
    }

    private void setupTestDependencies() {
        log.info("Setting up test dependencies (Company, Department, Team, PerformanceCycle, AssessmentMatrix, Questions, EmployeeAssessment)...");
        
        // Create test company
        Optional<CompanyV2> companyOpt = getCompanyService().create(
            "12345678901",
            "Test Company V2 Answer - " + System.currentTimeMillis(),
            "testanswer@company.com",
            "Test company for AnswerV2 demo",
            testTenantId
        );
        
        if (companyOpt.isPresent()) {
            testCompany = companyOpt.get();
            log.info("Created test company: {} (ID: {})", testCompany.getName(), testCompany.getId());
        } else {
            log.error("Failed to create test company");
            return;
        }
        
        // Create test department
        Optional<DepartmentV2> departmentOpt = getDepartmentService().create(
            "Test Department V2 Answer",
            "Test department for AnswerV2 demo",
            testTenantId,
            testCompany.getId()
        );
        
        if (departmentOpt.isPresent()) {
            testDepartment = departmentOpt.get();
            log.info("Created test department: {} (ID: {})", testDepartment.getName(), testDepartment.getId());
        } else {
            log.error("Failed to create test department");
            return;
        }
        
        // Create test team
        Optional<TeamV2> teamOpt = getTeamService().create(
            "Test Team V2 Answer",
            "Test team for AnswerV2 demo",
            testTenantId,
            testDepartment.getId()
        );
        
        if (teamOpt.isPresent()) {
            testTeam = teamOpt.get();
            log.info("Created test team: {} (ID: {})", testTeam.getName(), testTeam.getId());
        } else {
            log.error("Failed to create test team");
            return;
        }
        
        // Create test performance cycle
        Optional<PerformanceCycleV2> cycleOpt = getPerformanceCycleService().create(
            testTenantId,
            "Test Cycle V2 Answer - " + System.currentTimeMillis(),
            "Test performance cycle for AnswerV2 demo",
            testCompany.getId(),
            true,
            true,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        );
        
        if (cycleOpt.isPresent()) {
            testPerformanceCycle = cycleOpt.get();
            log.info("Created test performance cycle: {} (ID: {})", testPerformanceCycle.getName(), testPerformanceCycle.getId());
        } else {
            log.error("Failed to create test performance cycle");
            return;
        }
        
        // Create test assessment matrix
        Map<String, PillarV2> pillarMap = tableRunnerHelper.createPillarsWithCategoriesMapV2();
        Optional<AssessmentMatrixV2> matrixOpt = getAssessmentMatrixServiceV2().create(
            "Test Assessment Matrix V2 Answer",
            "Assessment matrix for AnswerV2 demo",
            testTenantId,
            testPerformanceCycle.getId(),
            pillarMap
        );
        
        if (matrixOpt.isPresent()) {
            testAssessmentMatrix = matrixOpt.get();
            log.info("Created test assessment matrix: {} (ID: {})", testAssessmentMatrix.getName(), testAssessmentMatrix.getId());
        } else {
            log.error("Failed to create test assessment matrix");
            return;
        }
        
        // Create test questions for answering
        createTestQuestions();
        
        // Create test employee assessment
        NaturalPersonV2 testEmployee = createTestEmployee();
        Optional<EmployeeAssessmentV2> assessmentOpt = getEmployeeAssessmentServiceV2().create(
            testAssessmentMatrix.getId(),
            testTeam.getId(),
            testEmployee.getName(),
            testEmployee.getEmail(),
            testEmployee.getDocumentNumber(),
            testEmployee.getPersonDocumentType(),
            testEmployee.getGender(),
            testEmployee.getGenderPronoun()
        );
        
        if (assessmentOpt.isPresent()) {
            testEmployeeAssessment = assessmentOpt.get();
            log.info("Created test employee assessment: {} (ID: {})", 
                testEmployee.getName(), testEmployeeAssessment.getId());
        } else {
            log.error("Failed to create test employee assessment");
        }
    }

    private void createTestQuestions() {
        log.info("Creating test questions for answering...");
        
        // Get first pillar and category for testing
        PillarV2 firstPillar = testAssessmentMatrix.getPillarMap().values().iterator().next();
        CategoryV2 firstCategory = firstPillar.getCategoryMap().values().iterator().next();
        
        // Create different types of questions
        String[] questionTexts = {
            "Is your team following agile principles?",
            "How satisfied are you with team communication?",
            "Rate your team's productivity level",
            "What improvements would you suggest?",
            "Which tools does your team use most?"
        };
        
        QuestionType[] questionTypes = {
            QuestionType.YES_NO,
            QuestionType.STAR_FIVE,
            QuestionType.ONE_TO_TEN,
            QuestionType.OPEN_ANSWER,
            QuestionType.CUSTOMIZED
        };
        
        for (int i = 0; i < questionTexts.length; i++) {
            if (questionTypes[i] == QuestionType.CUSTOMIZED) {
                // Create custom question with options
                List<QuestionOption> options = createMockedQuestionOptionList("Option", 5.0, 10.0, 15.0);
                Optional<Question> questionOpt = getQuestionService().createCustomQuestion(
                    questionTexts[i],
                    questionTypes[i],
                    testTenantId,
                    false, // single choice
                    false, // not flushed
                    options,
                    testAssessmentMatrix.getId(),
                    firstPillar.getId(),
                    firstCategory.getId(),
                    "Test custom question for answers"
                );
                
                if (questionOpt.isPresent()) {
                    createdQuestions.add(questionOpt.get());
                    log.info("✓ Created custom question: {}", questionTexts[i]);
                }
            } else {
                // Create regular question
                Optional<Question> questionOpt = getQuestionService().create(
                    questionTexts[i],
                    questionTypes[i],
                    testTenantId,
                    (i + 1) * 10.0, // 10, 20, 30, 40 points
                    testAssessmentMatrix.getId(),
                    firstPillar.getId(),
                    firstCategory.getId(),
                    "Test question for answers"
                );
                
                if (questionOpt.isPresent()) {
                    createdQuestions.add(questionOpt.get());
                    log.info("✓ Created {} question: {}", questionTypes[i], questionTexts[i]);
                }
            }
        }
        
        log.info("Created {} test questions", createdQuestions.size());
    }

    private NaturalPersonV2 createTestEmployee() {
        return NaturalPersonV2.builder()
            .name("John Test Employee")
            .email("john.employee@test.com")
            .documentNumber("12345678901")
            .personDocumentType(PersonDocumentType.CPF)
            .gender(Gender.MALE)
            .genderPronoun(GenderPronoun.HE)
            .build();
    }

    private void testCreateOperations() {
        if (createdQuestions.isEmpty()) {
            log.warn("No questions available for answer creation testing");
            return;
        }
        
        log.info("Testing create operations with different answer types...");
        
        LocalDateTime answeredAt = LocalDateTime.now().minusMinutes(30);
        
        // Test YES_NO answer
        Question yesNoQuestion = createdQuestions.stream()
            .filter(q -> q.getQuestionType() == QuestionType.YES_NO)
            .findFirst()
            .orElse(null);
            
        if (yesNoQuestion != null) {
            Optional<AnswerV2> yesNoAnswerOpt = getAnswerServiceV2().create(
                testEmployeeAssessment.getId(),
                yesNoQuestion.getId(),
                answeredAt,
                "Yes",
                testTenantId,
                "Test yes/no answer"
            );
            
            if (yesNoAnswerOpt.isPresent()) {
                AnswerV2 yesNoAnswer = yesNoAnswerOpt.get();
                createdAnswers.add(yesNoAnswer);
                log.info("✓ Created YES_NO answer: {} -> {} (Score: {})", 
                    yesNoQuestion.getQuestion(), yesNoAnswer.getValue(), yesNoAnswer.getScore());
            } else {
                log.error("✗ Failed to create YES_NO answer");
            }
        }
        
        // Test STAR_FIVE answer
        Question starFiveQuestion = createdQuestions.stream()
            .filter(q -> q.getQuestionType() == QuestionType.STAR_FIVE)
            .findFirst()
            .orElse(null);
            
        if (starFiveQuestion != null) {
            Optional<AnswerV2> starFiveAnswerOpt = getAnswerServiceV2().create(
                testEmployeeAssessment.getId(),
                starFiveQuestion.getId(),
                answeredAt.plusMinutes(5),
                "4",
                testTenantId,
                "Test star rating answer"
            );
            
            if (starFiveAnswerOpt.isPresent()) {
                AnswerV2 starFiveAnswer = starFiveAnswerOpt.get();
                createdAnswers.add(starFiveAnswer);
                log.info("✓ Created STAR_FIVE answer: {} -> {} stars (Score: {})", 
                    starFiveQuestion.getQuestion(), starFiveAnswer.getValue(), starFiveAnswer.getScore());
            } else {
                log.error("✗ Failed to create STAR_FIVE answer");
            }
        }
        
        // Test ONE_TO_TEN answer
        Question oneToTenQuestion = createdQuestions.stream()
            .filter(q -> q.getQuestionType() == QuestionType.ONE_TO_TEN)
            .findFirst()
            .orElse(null);
            
        if (oneToTenQuestion != null) {
            Optional<AnswerV2> oneToTenAnswerOpt = getAnswerServiceV2().create(
                testEmployeeAssessment.getId(),
                oneToTenQuestion.getId(),
                answeredAt.plusMinutes(10),
                "8",
                testTenantId,
                "Test scale answer"
            );
            
            if (oneToTenAnswerOpt.isPresent()) {
                AnswerV2 oneToTenAnswer = oneToTenAnswerOpt.get();
                createdAnswers.add(oneToTenAnswer);
                log.info("✓ Created ONE_TO_TEN answer: {} -> {} (Score: {})", 
                    oneToTenQuestion.getQuestion(), oneToTenAnswer.getValue(), oneToTenAnswer.getScore());
            } else {
                log.error("✗ Failed to create ONE_TO_TEN answer");
            }
        }
        
        // Test OPEN_ANSWER
        Question openQuestion = createdQuestions.stream()
            .filter(q -> q.getQuestionType() == QuestionType.OPEN_ANSWER)
            .findFirst()
            .orElse(null);
            
        if (openQuestion != null) {
            Optional<AnswerV2> openAnswerOpt = getAnswerServiceV2().create(
                testEmployeeAssessment.getId(),
                openQuestion.getId(),
                answeredAt.plusMinutes(15),
                "We should improve our daily standup meetings and implement better code review processes.",
                testTenantId,
                "Test open text answer"
            );
            
            if (openAnswerOpt.isPresent()) {
                AnswerV2 openAnswer = openAnswerOpt.get();
                createdAnswers.add(openAnswer);
                log.info("✓ Created OPEN_ANSWER: {} -> [{}...] (Pending Review: {})", 
                    openQuestion.getQuestion(), 
                    openAnswer.getValue().substring(0, Math.min(30, openAnswer.getValue().length())),
                    openAnswer.isPendingReview());
            } else {
                log.error("✗ Failed to create OPEN_ANSWER");
            }
        }
        
        // Test CUSTOMIZED answer
        Question customQuestion = createdQuestions.stream()
            .filter(q -> q.getQuestionType() == QuestionType.CUSTOMIZED)
            .findFirst()
            .orElse(null);
            
        if (customQuestion != null) {
            Optional<AnswerV2> customAnswerOpt = getAnswerServiceV2().create(
                testEmployeeAssessment.getId(),
                customQuestion.getId(),
                answeredAt.plusMinutes(20),
                "2", // Select option 2
                testTenantId,
                "Test custom option answer"
            );
            
            if (customAnswerOpt.isPresent()) {
                AnswerV2 customAnswer = customAnswerOpt.get();
                createdAnswers.add(customAnswer);
                log.info("✓ Created CUSTOMIZED answer: {} -> Option {} (Score: {})", 
                    customQuestion.getQuestion(), customAnswer.getValue(), customAnswer.getScore());
            } else {
                log.error("✗ Failed to create CUSTOMIZED answer");
            }
        }
        
        log.info("Create operations completed. Created {} answers.", createdAnswers.size());
    }

    private void testReadOperations() {
        if (createdAnswers.isEmpty()) {
            log.warn("No answers available for read testing");
            return;
        }
        
        log.info("Testing read operations...");
        
        // Test findById
        AnswerV2 testAnswer = createdAnswers.get(0);
        Optional<AnswerV2> foundAnswerOpt = getAnswerServiceV2().findById(testAnswer.getId());
        if (foundAnswerOpt.isPresent()) {
            AnswerV2 foundAnswer = foundAnswerOpt.get();
            log.info("✓ Found answer by ID: {} -> {} (Score: {})", 
                testAnswer.getId(), foundAnswer.getValue(), foundAnswer.getScore());
            
            // Verify data integrity
            if (testAnswer.getValue().equals(foundAnswer.getValue()) &&
                testAnswer.getEmployeeAssessmentId().equals(foundAnswer.getEmployeeAssessmentId()) &&
                testAnswer.getTenantId().equals(foundAnswer.getTenantId())) {
                log.info("  Data integrity verified ✓");
            } else {
                log.warn("  Data integrity mismatch ✗");
            }
        } else {
            log.error("✗ Failed to find answer by ID: {}", testAnswer.getId());
        }
        
        // Test findAll
        List<AnswerV2> allAnswers = getAnswerServiceV2().findAll();
        log.info("✓ Found {} total answers in system", allAnswers.size());
        
        // Test findAll (AnswerServiceV2 extends AbstractCrudServiceV2 which has findAll)
        log.info("✓ Total answers found in system: {}", allAnswers.size());
        
        log.info("Read operations completed.");
    }

    private void testUpdateOperations() {
        if (createdAnswers.isEmpty()) {
            log.warn("No answers available for update testing");
            return;
        }
        
        log.info("Testing update operations...");
        
        // Test update answer by ID
        AnswerV2 answerToUpdate = createdAnswers.stream()
            .filter(a -> a.getQuestionType() != QuestionType.OPEN_ANSWER) // Avoid open answers for simpler testing
            .findFirst()
            .orElse(null);
            
        if (answerToUpdate != null) {
            String originalValue = answerToUpdate.getValue();
            Double originalScore = answerToUpdate.getScore();
            
            // Update with different value
            String newValue = "No"; // Change YES_NO from Yes to No
            if (answerToUpdate.getQuestionType() == QuestionType.STAR_FIVE) {
                newValue = "2"; // Change star rating
            } else if (answerToUpdate.getQuestionType() == QuestionType.ONE_TO_TEN) {
                newValue = "3"; // Change scale rating
            }
            
            Optional<AnswerV2> updatedAnswerOpt = getAnswerServiceV2().update(
                answerToUpdate.getId(),
                LocalDateTime.now(),
                newValue,
                "Updated answer notes"
            );
            
            if (updatedAnswerOpt.isPresent()) {
                AnswerV2 updatedAnswer = updatedAnswerOpt.get();
                log.info("✓ Updated answer: {} -> {} (Score: {} -> {})", 
                    originalValue, updatedAnswer.getValue(), originalScore, updatedAnswer.getScore());
                log.info("  Notes updated: {}", updatedAnswer.getNotes());
            } else {
                log.error("✗ Failed to update answer");
            }
        }
        
        log.info("Update operations completed.");
    }

    private void testQueryOperations() {
        if (createdAnswers.isEmpty()) {
            log.warn("No answers available for query testing");
            return;
        }
        
        log.info("Testing query operations...");
        
        // Test findByEmployeeAssessmentId
        List<AnswerV2> assessmentAnswers = getAnswerServiceV2().findByEmployeeAssessmentId(
            testEmployeeAssessment.getId(), testTenantId);
        log.info("✓ Found {} answers for employee assessment: {}", 
            assessmentAnswers.size(), testEmployeeAssessment.getId());
        
        // Verify all answers belong to correct assessment and tenant
        boolean allCorrect = assessmentAnswers.stream()
            .allMatch(a -> a.getEmployeeAssessmentId().equals(testEmployeeAssessment.getId()) && 
                          a.getTenantId().equals(testTenantId));
        if (allCorrect) {
            log.info("  All answers correctly filtered by assessment and tenant ✓");
        } else {
            log.warn("  Some answers have incorrect assessment or tenant association ✗");
        }
        
        // Test findAnsweredQuestionIds
        Set<String> answeredQuestionIds = getAnswerServiceV2().findAnsweredQuestionIds(
            testEmployeeAssessment.getId(), testTenantId);
        log.info("✓ Found {} answered question IDs for assessment", answeredQuestionIds.size());
        
        // Verify answered question IDs match our created answers
        Set<String> expectedQuestionIds = createdAnswers.stream()
            .filter(a -> a.getEmployeeAssessmentId().equals(testEmployeeAssessment.getId()))
            .map(AnswerV2::getQuestionId)
            .collect(Collectors.toSet());
            
        if (answeredQuestionIds.equals(expectedQuestionIds)) {
            log.info("  Answered question IDs match created answers ✓");
        } else {
            log.info("  Answered question IDs: {}, Expected: {}", answeredQuestionIds.size(), expectedQuestionIds.size());
        }
        
        // Test existsById
        if (!createdAnswers.isEmpty()) {
            AnswerV2 testAnswer = createdAnswers.get(0);
            boolean exists = getAnswerServiceV2().existsById(testAnswer.getId());
            if (exists) {
                log.info("✓ Confirmed answer exists: {}", testAnswer.getId());
            } else {
                log.error("✗ Answer should exist but doesn't: {}", testAnswer.getId());
            }
        }
        
        log.info("Query operations completed.");
    }

    private void testAssessmentIntegration() {
        if (createdAnswers.isEmpty()) {
            log.warn("No answers available for assessment integration testing");
            return;
        }
        
        log.info("Testing assessment integration...");
        
        // Check employee assessment after answer creation
        Optional<EmployeeAssessmentV2> currentAssessmentOpt = getEmployeeAssessmentServiceV2().findById(testEmployeeAssessment.getId());
        if (currentAssessmentOpt.isPresent()) {
            EmployeeAssessmentV2 currentAssessment = currentAssessmentOpt.get();
            log.info("✓ Current assessment answered question count: {}", currentAssessment.getAnsweredQuestionCount());
            log.info("  Assessment status: {}", currentAssessment.getAssessmentStatus());
            log.info("  Last activity date: {}", currentAssessment.getLastActivityDate());
            
            // Verify answered count matches created answers
            long expectedCount = createdAnswers.stream()
                .filter(a -> a.getEmployeeAssessmentId().equals(testEmployeeAssessment.getId()))
                .count();
                
            if (currentAssessment.getAnsweredQuestionCount() >= expectedCount) {
                log.info("  Answered question count correctly updated ✓");
            } else {
                log.warn("  Answered question count mismatch: expected at least {}, got {}", 
                    expectedCount, currentAssessment.getAnsweredQuestionCount());
            }
        }
        
        log.info("Assessment integration testing completed.");
    }

    private void testDuplicatePrevention() {
        if (createdAnswers.isEmpty() || createdQuestions.isEmpty()) {
            log.warn("No answers or questions available for duplicate prevention testing");
            return;
        }
        
        log.info("Testing duplicate prevention...");
        
        // Try to create duplicate answer
        AnswerV2 existingAnswer = createdAnswers.get(0);
        Question question = createdQuestions.stream()
            .filter(q -> q.getId().equals(existingAnswer.getQuestionId()))
            .findFirst()
            .orElse(null);
            
        if (question != null) {
            String originalValue = existingAnswer.getValue();
            String newValue = "Updated Duplicate";
            
            // Attempt to create duplicate (should update existing)
            Optional<AnswerV2> duplicateAnswerOpt = getAnswerServiceV2().create(
                testEmployeeAssessment.getId(),
                question.getId(),
                LocalDateTime.now(),
                newValue,
                testTenantId,
                "Duplicate prevention test"
            );
            
            if (duplicateAnswerOpt.isPresent()) {
                AnswerV2 duplicateAnswer = duplicateAnswerOpt.get();
                
                // Should be same ID (updated existing)
                if (duplicateAnswer.getId().equals(existingAnswer.getId())) {
                    log.info("✓ Duplicate prevention working: updated existing answer");
                    log.info("  Value changed: {} -> {}", originalValue, duplicateAnswer.getValue());
                    log.info("  Same answer ID maintained: {}", duplicateAnswer.getId());
                } else {
                    log.warn("✗ Duplicate prevention failed: created new answer instead of updating");
                }
                
                // Verify no duplicate answers exist
                List<AnswerV2> assessmentAnswers = getAnswerServiceV2().findByEmployeeAssessmentId(
                    testEmployeeAssessment.getId(), testTenantId);
                    
                long answerCount = assessmentAnswers.stream()
                    .filter(a -> a.getQuestionId().equals(question.getId()))
                    .count();
                    
                if (answerCount == 1) {
                    log.info("  Verified only one answer exists for question ✓");
                } else {
                    log.warn("  Found {} answers for same question (should be 1) ✗", answerCount);
                }
            } else {
                log.error("✗ Failed to handle duplicate answer creation");
            }
        }
        
        log.info("Duplicate prevention testing completed.");
    }

    private void testAssessmentCompletion() {
        log.info("Testing assessment completion logic...");
        
        // Get current assessment matrix question count
        Optional<AssessmentMatrixV2> matrixOpt = getAssessmentMatrixServiceV2().findById(testAssessmentMatrix.getId());
        if (!matrixOpt.isPresent()) {
            log.warn("Assessment matrix not found for completion testing");
            return;
        }
        
        AssessmentMatrixV2 matrix = matrixOpt.get();
        int totalQuestions = matrix.getQuestionCount();
        log.info("Total questions in assessment matrix: {}", totalQuestions);
        
        // Check current answered questions
        Optional<EmployeeAssessmentV2> assessmentOpt = getEmployeeAssessmentServiceV2().findById(testEmployeeAssessment.getId());
        if (!assessmentOpt.isPresent()) {
            log.warn("Employee assessment not found for completion testing");
            return;
        }
        
        EmployeeAssessmentV2 assessment = assessmentOpt.get();
        int currentAnsweredCount = assessment.getAnsweredQuestionCount();
        log.info("Currently answered questions: {}", currentAnsweredCount);
        log.info("Current assessment status: {}", assessment.getAssessmentStatus());
        
        if (currentAnsweredCount >= totalQuestions) {
            log.info("✓ Assessment should be completed");
            
            if (assessment.getAssessmentStatus() == AssessmentStatus.COMPLETED) {
                log.info("  Assessment status correctly set to COMPLETED ✓");
            } else {
                log.warn("  Assessment status should be COMPLETED but is {} ✗", assessment.getAssessmentStatus());
            }
        } else {
            log.info("✓ Assessment not yet completed ({}/{})", currentAnsweredCount, totalQuestions);
            
            if (assessment.getAssessmentStatus() == AssessmentStatus.IN_PROGRESS) {
                log.info("  Assessment status correctly IN_PROGRESS ✓");
            } else {
                log.info("  Assessment status: {}", assessment.getAssessmentStatus());
            }
        }
        
        log.info("Assessment completion testing completed.");
    }

    private void testDeleteOperations() {
        if (createdAnswers.isEmpty()) {
            log.warn("No answers available for delete testing");
            return;
        }
        
        log.info("Testing delete operations...");
        
        // Test individual delete (we'll delete one answer as a test)
        if (createdAnswers.size() > 1) {
            AnswerV2 answerToDelete = createdAnswers.get(createdAnswers.size() - 1);
            String answerValue = answerToDelete.getValue();
            String answerId = answerToDelete.getId();
            
            // Test delete by ID (AbstractCrudServiceV2 has deleteById but not delete)
            boolean deleted = getAnswerServiceV2().deleteById(answerId);
            if (!deleted) {
                log.error("✗ Failed to delete answer: {}", answerValue);
                return;
            }
            
            // Verify deletion
            Optional<AnswerV2> deletedAnswerOpt = getAnswerServiceV2().findById(answerId);
            if (deletedAnswerOpt.isEmpty()) {
                log.info("✓ Successfully deleted answer: {}", answerValue);
                createdAnswers.remove(answerToDelete);
            } else {
                log.error("✗ Failed to delete answer: {}", answerValue);
            }
        }
        
        // Test deleteById
        if (createdAnswers.size() > 1) {
            AnswerV2 answerToDeleteById = createdAnswers.get(createdAnswers.size() - 1);
            String answerValue = answerToDeleteById.getValue();
            String answerId = answerToDeleteById.getId();
            
            boolean deleted = getAnswerServiceV2().deleteById(answerId);
            if (deleted) {
                log.info("✓ Successfully deleted answer by ID: {}", answerValue);
                createdAnswers.remove(answerToDeleteById);
            } else {
                log.error("✗ Failed to delete answer by ID: {}", answerValue);
            }
        }
        
        log.info("Delete operations completed. Remaining answers: {}", createdAnswers.size());
    }

    private List<QuestionOption> createMockedQuestionOptionList(String prefix, Double... points) {
        return IntStream.range(0, points.length)
            .mapToObj(index -> createQuestionOption(index + 1, prefix, points[index]))
            .collect(Collectors.toList());
    }

    private QuestionOption createQuestionOption(Integer id, String prefix, double points) {
        return QuestionOption.builder()
            .id(id)
            .text(prefix + " " + id)
            .points(points)
            .build();
    }

    private void cleanupAllTestData() {
        log.info("Starting comprehensive cleanup of all test data...");
        
        // Delete remaining answers
        for (AnswerV2 answer : createdAnswers) {
            try {
                boolean deleted = getAnswerServiceV2().deleteById(answer.getId());
                if (deleted) {
                    log.info("✓ Cleaned up answer: {}", answer.getValue());
                } else {
                    log.warn("✗ Failed to clean up answer: {}", answer.getValue());
                }
            } catch (Exception e) {
                log.error("Error cleaning up answer {}: {}", answer.getValue(), e.getMessage());
            }
        }
        
        // Delete test employee assessment
        if (testEmployeeAssessment != null) {
            try {
                boolean deleted = getEmployeeAssessmentServiceV2().deleteById(testEmployeeAssessment.getId());
                if (deleted) {
                    log.info("✓ Cleaned up employee assessment: {}", testEmployeeAssessment.getId());
                } else {
                    log.warn("✗ Failed to clean up employee assessment: {}", testEmployeeAssessment.getId());
                }
            } catch (Exception e) {
                log.error("Error cleaning up employee assessment: {}", e.getMessage());
            }
        }
        
        // Delete created questions
        for (Question question : createdQuestions) {
            try {
                getQuestionService().delete(question);
                log.info("✓ Cleaned up question: {}", question.getQuestion());
            } catch (Exception e) {
                log.error("Error cleaning up question {}: {}", question.getQuestion(), e.getMessage());
            }
        }
        
        // Delete test assessment matrix
        if (testAssessmentMatrix != null) {
            try {
                boolean deleted = getAssessmentMatrixServiceV2().deleteById(testAssessmentMatrix.getId());
                if (deleted) {
                    log.info("✓ Cleaned up assessment matrix: {}", testAssessmentMatrix.getName());
                } else {
                    log.warn("✗ Failed to clean up assessment matrix: {}", testAssessmentMatrix.getName());
                }
            } catch (Exception e) {
                log.error("Error cleaning up assessment matrix: {}", e.getMessage());
            }
        }
        
        // Delete test performance cycle
        if (testPerformanceCycle != null) {
            try {
                boolean deleted = getPerformanceCycleService().deleteById(testPerformanceCycle.getId());
                if (deleted) {
                    log.info("✓ Cleaned up performance cycle: {}", testPerformanceCycle.getName());
                } else {
                    log.warn("✗ Failed to clean up performance cycle: {}", testPerformanceCycle.getName());
                }
            } catch (Exception e) {
                log.error("Error cleaning up performance cycle: {}", e.getMessage());
            }
        }
        
        // Delete test team
        if (testTeam != null) {
            try {
                boolean deleted = getTeamService().deleteById(testTeam.getId());
                if (deleted) {
                    log.info("✓ Cleaned up team: {}", testTeam.getName());
                } else {
                    log.warn("✗ Failed to clean up team: {}", testTeam.getName());
                }
            } catch (Exception e) {
                log.error("Error cleaning up team: {}", e.getMessage());
            }
        }
        
        // Delete test department
        if (testDepartment != null) {
            try {
                boolean deleted = getDepartmentService().deleteById(testDepartment.getId());
                if (deleted) {
                    log.info("✓ Cleaned up department: {}", testDepartment.getName());
                } else {
                    log.warn("✗ Failed to clean up department: {}", testDepartment.getName());
                }
            } catch (Exception e) {
                log.error("Error cleaning up department: {}", e.getMessage());
            }
        }
        
        // Delete test company
        if (testCompany != null) {
            try {
                boolean deleted = getCompanyService().deleteById(testCompany.getId());
                if (deleted) {
                    log.info("✓ Cleaned up company: {}", testCompany.getName());
                } else {
                    log.warn("✗ Failed to clean up company: {}", testCompany.getName());
                }
            } catch (Exception e) {
                log.error("Error cleaning up company: {}", e.getMessage());
            }
        }
        
        log.info("Cleanup completed. Cleaned {} answers, {} questions, 1 assessment, 1 matrix, 1 cycle, 1 team, 1 department, 1 company", 
            createdAnswers.size(), createdQuestions.size());
    }

    // Service getters with lazy initialization
    private AnswerServiceV2 getAnswerServiceV2() {
        if (answerServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            answerServiceV2 = serviceComponent.buildAnswerServiceV2();
        }
        return answerServiceV2;
    }

    private EmployeeAssessmentServiceV2 getEmployeeAssessmentServiceV2() {
        if (employeeAssessmentServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            employeeAssessmentServiceV2 = serviceComponent.buildEmployeeAssessmentServiceV2();
        }
        return employeeAssessmentServiceV2;
    }

    private QuestionService getQuestionService() {
        if (questionService == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            questionService = serviceComponent.buildQuestionService();
        }
        return questionService;
    }

    private AssessmentMatrixServiceV2 getAssessmentMatrixServiceV2() {
        if (assessmentMatrixServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            assessmentMatrixServiceV2 = serviceComponent.buildAssessmentMatrixServiceV2();
        }
        return assessmentMatrixServiceV2;
    }

    private PerformanceCycleService getPerformanceCycleService() {
        if (performanceCycleService == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            performanceCycleService = serviceComponent.buildPerformanceCycleService();
        }
        return performanceCycleService;
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

    private TeamService getTeamService() {
        if (teamService == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            teamService = serviceComponent.buildTeamService();
        }
        return teamService;
    }
}
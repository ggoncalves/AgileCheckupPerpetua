package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.service.AssessmentMatrixService;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.PerformanceCycleService;
import com.agilecheckup.service.QuestionService;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
public class QuestionTableRunner implements CrudRunner {

    private QuestionService questionService;
    private AssessmentMatrixService assessmentMatrixService;
    private PerformanceCycleService performanceCycleService;
    private CompanyService companyService;

    private final TableRunnerHelper tableRunnerHelper = new TableRunnerHelper();
    private final boolean shouldCleanAfterComplete;
    
    // Test data tracking for cleanup
    private final List<Question> createdQuestions = new ArrayList<>();
    private Company testCompany;
    private PerformanceCycle testPerformanceCycle;
    private AssessmentMatrix testAssessmentMatrix;
    private final String testTenantId = "test-tenant-question-v2-" + System.currentTimeMillis();

    public QuestionTableRunner(boolean shouldCleanAfterComplete) {
        this.shouldCleanAfterComplete = shouldCleanAfterComplete;
    }

    @Override
    public void run() {
        log.info("\n=== Question Comprehensive CRUD Operations Demo ===");
        
        try {
            // 1. Setup test dependencies
            setupTestDependencies();
            
            if (testCompany == null || testPerformanceCycle == null || testAssessmentMatrix == null) {
                log.error("Failed to create test dependencies. Aborting demo.");
                return;
            }
            
            // 2. Test Create Operations
            log.info("\n1. Testing  Create Operations...");
            testCreateOperations();
            
            // 3. Test Read Operations
            log.info("\n2. Testing  Read Operations...");
            testReadOperations();
            
            // 4. Test Update Operations
            log.info("\n3. Testing  Update Operations...");
            testUpdateOperations();
            
            // 5. Test Query Operations
            log.info("\n4. Testing  Query Operations...");
            testQueryOperations();
            
            // 6. Test Category and Matrix Integration
            log.info("\n5. Testing Category and Matrix Integration...");
            testCategoryIntegration();
            
            // 7. Test Potential Score Calculation
            log.info("\n6. Testing Potential Score Calculation...");
            testPotentialScoreCalculation();
            
            // 8. Test Delete Operations
            log.info("\n7. Testing  Delete Operations...");
            testDeleteOperations();
            
            log.info("\n=== Question Demo Completed Successfully ===");
            
        } catch (Exception e) {
            log.error("Error during Question demo: {}", e.getMessage(), e);
        } finally {
            // 9. Cleanup all test data
            if (shouldCleanAfterComplete) {
                log.info("\n8. Cleaning up all test data...");
                cleanupAllTestData();
            }
        }
    }

    private void setupTestDependencies() {
        log.info("Setting up test dependencies (Company, PerformanceCycle, AssessmentMatrix)...");
        
        // Create test company
        Optional<Company> companyOpt = getCompanyService().create(
            "12345678901",
            "Test Company  Question - " + System.currentTimeMillis(),
            "testquestion@company.com",
            "Test company for Question demo",
            testTenantId
        );
        
        if (companyOpt.isPresent()) {
            testCompany = companyOpt.get();
            log.info("Created test company: {} (ID: {})", testCompany.getName(), testCompany.getId());
        } else {
            log.error("Failed to create test company");
            return;
        }
        
        // Create test performance cycle
        Optional<PerformanceCycle> cycleOpt = getPerformanceCycleService().create(
            testTenantId,
            "Test Cycle  Question - " + System.currentTimeMillis(),
            "Test performance cycle for Question demo",
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
        Map<String, Pillar> pillarMap = tableRunnerHelper.createPillarsWithCategoriesMap();
        Optional<AssessmentMatrix> matrixOpt = getAssessmentMatrixService().create(
            "Test Assessment Matrix  Question",
            "Assessment matrix for Question demo",
            testTenantId,
            testPerformanceCycle.getId(),
            pillarMap
        );
        
        if (matrixOpt.isPresent()) {
            testAssessmentMatrix = matrixOpt.get();
            log.info("Created test assessment matrix: {} (ID: {})", testAssessmentMatrix.getName(), testAssessmentMatrix.getId());
        } else {
            log.error("Failed to create test assessment matrix");
        }
    }

    private void testCreateOperations() {
        log.info("Testing create operations with different question types...");
        
        // Get first pillar and category for testing
        Pillar firstPillar = testAssessmentMatrix.getPillarMap().values().iterator().next();
        Category firstCategory = firstPillar.getCategoryMap().values().iterator().next();
        
        // Create YES_NO question
        Optional<Question> yesNoQuestionOpt = getQuestionService().create(
            "Is agile methodology effective in your team?",
            QuestionType.YES_NO,
            testTenantId,
            10.0,
            testAssessmentMatrix.getId(),
            firstPillar.getId(),
            firstCategory.getId(),
            "Basic yes/no question"
        );
        
        if (yesNoQuestionOpt.isPresent()) {
            Question yesNoQuestion = yesNoQuestionOpt.get();
            createdQuestions.add(yesNoQuestion);
            log.info("✓ Created YES_NO question: {} (ID: {})", yesNoQuestion.getQuestion(), yesNoQuestion.getId());
        } else {
            log.error("✗ Failed to create YES_NO question");
        }
        
        // Create STAR_FIVE question
        Optional<Question> starFiveQuestionOpt = getQuestionService().create(
            "Rate your team's communication skills",
            QuestionType.STAR_FIVE,
            testTenantId,
            15.0,
            testAssessmentMatrix.getId(),
            firstPillar.getId(),
            firstCategory.getId(),
            "Star rating question"
        );
        
        if (starFiveQuestionOpt.isPresent()) {
            Question starFiveQuestion = starFiveQuestionOpt.get();
            createdQuestions.add(starFiveQuestion);
            log.info("✓ Created STAR_FIVE question: {} (ID: {})", starFiveQuestion.getQuestion(), starFiveQuestion.getId());
        } else {
            log.error("✗ Failed to create STAR_FIVE question");
        }
        
        // Create ONE_TO_TEN question
        Optional<Question> oneToTenQuestionOpt = getQuestionService().create(
            "How would you rate your overall satisfaction?",
            QuestionType.ONE_TO_TEN,
            testTenantId,
            20.0,
            testAssessmentMatrix.getId(),
            firstPillar.getId(),
            firstCategory.getId(),
            "Scale 1-10 question"
        );
        
        if (oneToTenQuestionOpt.isPresent()) {
            Question oneToTenQuestion = oneToTenQuestionOpt.get();
            createdQuestions.add(oneToTenQuestion);
            log.info("✓ Created ONE_TO_TEN question: {} (ID: {})", oneToTenQuestion.getQuestion(), oneToTenQuestion.getId());
        } else {
            log.error("✗ Failed to create ONE_TO_TEN question");
        }
        
        // Create CUSTOMIZED question with options
        List<QuestionOption> customOptions = createMockedQuestionOptionList("Choice", 5.0, 10.0, 15.0, 20.0);
        Optional<Question> customQuestionOpt = getQuestionService().createCustomQuestion(
            "Which agile practices does your team use most?",
            QuestionType.CUSTOMIZED,
            testTenantId,
            true,  // isMultipleChoice
            false, // showFlushed
            customOptions,
            testAssessmentMatrix.getId(),
            firstPillar.getId(),
            firstCategory.getId(),
            "Custom multiple choice question"
        );
        
        if (customQuestionOpt.isPresent()) {
            Question customQuestion = customQuestionOpt.get();
            createdQuestions.add(customQuestion);
            log.info("✓ Created CUSTOMIZED question: {} (ID: {}) with {} options", 
                customQuestion.getQuestion(), customQuestion.getId(), customOptions.size());
        } else {
            log.error("✗ Failed to create CUSTOMIZED question");
        }
        
        // Create OPEN_ANSWER question
        Optional<Question> openQuestionOpt = getQuestionService().create(
            "What improvements would you suggest for your team?",
            QuestionType.OPEN_ANSWER,
            testTenantId,
            25.0,
            testAssessmentMatrix.getId(),
            firstPillar.getId(),
            firstCategory.getId(),
            "Open text question"
        );
        
        if (openQuestionOpt.isPresent()) {
            Question openQuestion = openQuestionOpt.get();
            createdQuestions.add(openQuestion);
            log.info("✓ Created OPEN_ANSWER question: {} (ID: {})", openQuestion.getQuestion(), openQuestion.getId());
        } else {
            log.error("✗ Failed to create OPEN_ANSWER question");
        }
        
        log.info("Create operations completed. Created {} questions.", createdQuestions.size());
    }

    private void testReadOperations() {
        if (createdQuestions.isEmpty()) {
            log.warn("No questions available for read testing");
            return;
        }
        
        log.info("Testing read operations...");
        
        // Test findById
        Question testQuestion = createdQuestions.get(0);
        Optional<Question> foundQuestionOpt = getQuestionService().findById(testQuestion.getId());
        if (foundQuestionOpt.isPresent()) {
            Question foundQuestion = foundQuestionOpt.get();
            log.info("✓ Found question by ID: {} -> {}", testQuestion.getId(), foundQuestion.getQuestion());
            
            // Verify data integrity
            if (testQuestion.getQuestion().equals(foundQuestion.getQuestion()) &&
                testQuestion.getQuestionType().equals(foundQuestion.getQuestionType()) &&
                testQuestion.getTenantId().equals(foundQuestion.getTenantId())) {
                log.info("  Data integrity verified ✓");
            } else {
                log.warn("  Data integrity mismatch ✗");
            }
        } else {
            log.error("✗ Failed to find question by ID: {}", testQuestion.getId());
        }
        
        // Test findAll
        List<Question> allQuestions = getQuestionService().findAll();
        log.info("✓ Found {} total questions in system", allQuestions.size());
        
        // Test findAllByTenantId
        List<Question> tenantQuestions = getQuestionService().findAllByTenantId(testTenantId);
        log.info("✓ Found {} questions for tenant: {}", tenantQuestions.size(), testTenantId);
        
        log.info("Read operations completed.");
    }

    private void testUpdateOperations() {
        if (createdQuestions.isEmpty()) {
            log.warn("No questions available for update testing");
            return;
        }
        
        log.info("Testing update operations...");
        
        // Get first pillar and category
        Pillar firstPillar = testAssessmentMatrix.getPillarMap().values().iterator().next();
        Category firstCategory = firstPillar.getCategoryMap().values().iterator().next();
        
        // Test update regular question
        Question questionToUpdate = createdQuestions.stream()
            .filter(q -> q.getQuestionType() != QuestionType.CUSTOMIZED)
            .findFirst()
            .orElse(null);
            
        if (questionToUpdate != null) {
            String originalQuestion = questionToUpdate.getQuestion();
            Optional<Question> updatedQuestionOpt = getQuestionService().update(
                questionToUpdate.getId(),
                originalQuestion + " (Updated)",
                questionToUpdate.getQuestionType(),
                testTenantId,
                questionToUpdate.getPoints() + 5.0,
                testAssessmentMatrix.getId(),
                firstPillar.getId(),
                firstCategory.getId(),
                "Updated description"
            );
            
            if (updatedQuestionOpt.isPresent()) {
                Question updatedQuestion = updatedQuestionOpt.get();
                log.info("✓ Updated regular question: {} -> {}", originalQuestion, updatedQuestion.getQuestion());
                log.info("  Points updated: {} -> {}", questionToUpdate.getPoints(), updatedQuestion.getPoints());
            } else {
                log.error("✗ Failed to update regular question");
            }
        }
        
        // Test update custom question
        Question customQuestionToUpdate = createdQuestions.stream()
            .filter(q -> q.getQuestionType() == QuestionType.CUSTOMIZED)
            .findFirst()
            .orElse(null);
            
        if (customQuestionToUpdate != null) {
            String originalQuestion = customQuestionToUpdate.getQuestion();
            List<QuestionOption> updatedOptions = createMockedQuestionOptionList("Updated", 8.0, 16.0, 24.0);
            
            Optional<Question> updatedCustomQuestionOpt = getQuestionService().updateCustomQuestion(
                customQuestionToUpdate.getId(),
                originalQuestion + " (Updated with new options)",
                QuestionType.CUSTOMIZED,
                testTenantId,
                false, // Change to single choice
                true,  // Change showFlushed
                updatedOptions,
                testAssessmentMatrix.getId(),
                firstPillar.getId(),
                firstCategory.getId(),
                "Updated custom question description"
            );
            
            if (updatedCustomQuestionOpt.isPresent()) {
                Question updatedCustomQuestion = updatedCustomQuestionOpt.get();
                log.info("✓ Updated custom question: {} -> {}", originalQuestion, updatedCustomQuestion.getQuestion());
                log.info("  Multiple choice: {} -> {}", 
                    customQuestionToUpdate.getOptionGroup().isMultipleChoice(), 
                    updatedCustomQuestion.getOptionGroup().isMultipleChoice());
                log.info("  Options count: {} -> {}", 
                    customQuestionToUpdate.getOptionGroup().getOptionMap().size(),
                    updatedCustomQuestion.getOptionGroup().getOptionMap().size());
            } else {
                log.error("✗ Failed to update custom question");
            }
        }
        
        log.info("Update operations completed.");
    }

    private void testQueryOperations() {
        if (createdQuestions.isEmpty()) {
            log.warn("No questions available for query testing");
            return;
        }
        
        log.info("Testing query operations...");
        
        // Test findByAssessmentMatrixId
        List<Question> matrixQuestions = getQuestionService().findByAssessmentMatrixId(
            testAssessmentMatrix.getId(), testTenantId);
        log.info("✓ Found {} questions for assessment matrix: {}", 
            matrixQuestions.size(), testAssessmentMatrix.getName());
        
        // Verify all questions belong to correct matrix and tenant
        boolean allCorrect = matrixQuestions.stream()
            .allMatch(q -> q.getAssessmentMatrixId().equals(testAssessmentMatrix.getId()) && 
                          q.getTenantId().equals(testTenantId));
        if (allCorrect) {
            log.info("  All questions correctly filtered by matrix and tenant ✓");
        } else {
            log.warn("  Some questions have incorrect matrix or tenant association ✗");
        }
        
        // Test existsById
        if (!createdQuestions.isEmpty()) {
            Question testQuestion = createdQuestions.get(0);
            boolean exists = getQuestionService().existsById(testQuestion.getId());
            if (exists) {
                log.info("✓ Confirmed question exists: {}", testQuestion.getId());
            } else {
                log.error("✗ Question should exist but doesn't: {}", testQuestion.getId());
            }
        }
        
        log.info("Query operations completed.");
    }

    private void testCategoryIntegration() {
        if (createdQuestions.isEmpty()) {
            log.warn("No questions available for category integration testing");
            return;
        }
        
        log.info("Testing category integration...");
        
        // Get category from first created question
        Question testQuestion = createdQuestions.get(0);
        String categoryId = testQuestion.getCategoryId();
        
        // Test hasCategoryQuestions
        boolean hasQuestions = getQuestionService().hasCategoryQuestions(
            testAssessmentMatrix.getId(), categoryId, testTenantId);
        if (hasQuestions) {
            log.info("✓ Confirmed category {} has questions", categoryId);
        } else {
            log.error("✗ Category {} should have questions but doesn't", categoryId);
        }
        
        // Test with non-existent category
        boolean hasQuestionsNonExistent = getQuestionService().hasCategoryQuestions(
            testAssessmentMatrix.getId(), "non-existent-category", testTenantId);
        if (!hasQuestionsNonExistent) {
            log.info("✓ Confirmed non-existent category has no questions");
        } else {
            log.warn("✗ Non-existent category should not have questions");
        }
        
        log.info("Category integration testing completed.");
    }

    private void testPotentialScoreCalculation() {
        if (createdQuestions.isEmpty()) {
            log.warn("No questions available for potential score calculation");
            return;
        }
        
        log.info("Testing potential score calculation impact...");
        
        // Get current matrix state
        Optional<AssessmentMatrix> currentMatrixOpt = getAssessmentMatrixService().findById(testAssessmentMatrix.getId());
        if (currentMatrixOpt.isPresent()) {
            AssessmentMatrix currentMatrix = currentMatrixOpt.get();
            Integer currentQuestionCount = currentMatrix.getQuestionCount();
            log.info("Current question count in matrix: {}", currentQuestionCount);
            
            // Test potential score calculation
            AssessmentMatrix scoredMatrix = getAssessmentMatrixService().updateCurrentPotentialScore(
                testAssessmentMatrix.getId(), testTenantId);
            
            if (scoredMatrix.getPotentialScore() != null) {
                log.info("✓ Potential score calculated - Total: {}", scoredMatrix.getPotentialScore().getScore());
                
                // Calculate expected score based on our created questions
                double expectedScore = createdQuestions.stream()
                    .filter(q -> q.getAssessmentMatrixId().equals(testAssessmentMatrix.getId()))
                    .mapToDouble(q -> {
                        if (q.getQuestionType() == QuestionType.CUSTOMIZED && q.getOptionGroup() != null) {
                            return q.getOptionGroup().getOptionMap().values().stream()
                                .mapToDouble(opt -> opt.getPoints())
                                .max()
                                .orElse(0.0);
                        } else if (q.getPoints() != null) {
                            return q.getPoints();
                        }
                        return 0.0;
                    })
                    .sum();
                    
                log.info("  Expected score based on created questions: {}", expectedScore);
                
                if (Math.abs(scoredMatrix.getPotentialScore().getScore() - expectedScore) < 0.01) {
                    log.info("  Score calculation verified ✓");
                } else {
                    log.info("  Score calculation difference (may include other questions): {}",
                        Math.abs(scoredMatrix.getPotentialScore().getScore() - expectedScore));
                }
            } else {
                log.info("✓ Potential score calculation completed (no score set)");
            }
        }
        
        log.info("Potential score calculation testing completed.");
    }

    private void testDeleteOperations() {
        if (createdQuestions.isEmpty()) {
            log.warn("No questions available for delete testing");
            return;
        }
        
        log.info("Testing delete operations...");
        
        // Test individual delete (we'll delete one question as a test)
        if (createdQuestions.size() > 1) {
            Question questionToDelete = createdQuestions.get(createdQuestions.size() - 1);
            String questionText = questionToDelete.getQuestion();
            String questionId = questionToDelete.getId();
            
            // Test delete by entity
            getQuestionService().delete(questionToDelete);
            
            // Verify deletion
            Optional<Question> deletedQuestionOpt = getQuestionService().findById(questionId);
            if (deletedQuestionOpt.isEmpty()) {
                log.info("✓ Successfully deleted question: {}", questionText);
                createdQuestions.remove(questionToDelete);
            } else {
                log.error("✗ Failed to delete question: {}", questionText);
            }
        }
        
        // Test deleteById
        if (createdQuestions.size() > 1) {
            Question questionToDeleteById = createdQuestions.get(createdQuestions.size() - 1);
            String questionText = questionToDeleteById.getQuestion();
            String questionId = questionToDeleteById.getId();
            
            boolean deleted = getQuestionService().deleteById(questionId);
            if (deleted) {
                log.info("✓ Successfully deleted question by ID: {}", questionText);
                createdQuestions.remove(questionToDeleteById);
            } else {
                log.error("✗ Failed to delete question by ID: {}", questionText);
            }
        }
        
        log.info("Delete operations completed. Remaining questions: {}", createdQuestions.size());
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
        
        // Delete remaining questions
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
                boolean deleted = getAssessmentMatrixService().deleteById(testAssessmentMatrix.getId());
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
        
        log.info("Cleanup completed. Cleaned {} questions, 1 matrix, 1 cycle, 1 company", 
            createdQuestions.size());
    }

    // Service getters with lazy initialization
    private QuestionService getQuestionService() {
        if (questionService == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            questionService = serviceComponent.buildQuestionService();
        }
        return questionService;
    }

    private AssessmentMatrixService getAssessmentMatrixService() {
        if (assessmentMatrixService == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            assessmentMatrixService = serviceComponent.buildAssessmentMatrixService();
        }
        return assessmentMatrixService;
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
}
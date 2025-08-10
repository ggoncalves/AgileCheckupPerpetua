package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.AssessmentConfiguration;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.Category;
import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionNavigationType;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.service.AssessmentMatrixService;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.PerformanceCycleService;
import com.agilecheckup.service.QuestionService;
import com.agilecheckup.service.dto.AssessmentDashboardData;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
public class AssessmentMatrixTableRunner implements CrudRunner {

    private AssessmentMatrixService assessmentMatrixService;
    private PerformanceCycleService performanceCycleService;
    private CompanyService companyService;
    private QuestionService questionService;

    private final TableRunnerHelper tableRunnerHelper = new TableRunnerHelper();
    private final boolean shouldCleanAfterComplete;
    
    // Test data tracking for cleanup
    private final List<AssessmentMatrix> createdMatrices = new ArrayList<>();
    private final List<Question> createdQuestions = new ArrayList<>();
    private Company testCompany;
    private PerformanceCycle testPerformanceCycle;
    private final String testTenantId = "test-tenant-v2-" + System.currentTimeMillis();

    public AssessmentMatrixTableRunner(boolean shouldCleanAfterComplete) {
        this.shouldCleanAfterComplete = shouldCleanAfterComplete;
    }

    @Override
    public void run() {
        log.info("\n=== AssessmentMatrix Comprehensive CRUD Operations Demo ===");
        
        try {
            // 1. Setup test dependencies
            setupTestDependencies();
            
            if (testCompany == null || testPerformanceCycle == null) {
                log.error("Failed to create test dependencies. Aborting demo.");
                return;
            }
            
            // 2. Test Create Operations
            log.info("\n1. Testing  Create Operations...");
            testCreateOperations();
            
            // 3. Create test questions (before updates, so pillar/category IDs exist)
            log.info("\n2. Creating test questions for calculate operations...");
            createTestQuestionsForCalculateOperations();
            
            // 4. Test Update Operations
            log.info("\n3. Testing  Update Operations...");
            testUpdateOperations();
            
            // 5. Test Calculate Points Operations
            log.info("\n4. Testing  Calculate Points Operations...");
            testCalculatePointsOperations();
            
            // 6. Test Query Operations
            log.info("\n5. Testing  Query Operations...");
            testQueryOperations();
            
            // 7. Test Delete Operations
            log.info("\n6. Testing  Delete Operations...");
            testDeleteOperations();
            
            log.info("\n=== AssessmentMatrix Demo Completed Successfully ===");
            
        } catch (Exception e) {
            log.error("Error during AssessmentMatrix demo: {}", e.getMessage(), e);
        } finally {
            // 8. Cleanup all test data
            if (shouldCleanAfterComplete) {
                log.info("\n7. Cleaning up all test data...");
                cleanupAllTestData();
            }
        }
    }

    private void setupTestDependencies() {
        log.info("Setting up test dependencies (Company and PerformanceCycle)...");
        
        // Create test company
        Optional<Company> companyOpt = getCompanyService().create(
            "12345678901",
            "Test Company  - " + System.currentTimeMillis(),
            "test@company.com",
            "Test company for AssessmentMatrix demo",
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
            "Test Cycle  - " + System.currentTimeMillis(),
            "Test performance cycle for AssessmentMatrix demo",
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
        }
    }

    private void testCreateOperations() {
        log.info("Testing create operations with different configurations...");
        
        // Create matrix with basic configuration
        Map<String, Pillar> pillarMap1 = tableRunnerHelper.createPillarsWithCategoriesMap();
        Optional<AssessmentMatrix> matrix1Opt = getAssessmentMatrixService().create(
            "Basic Assessment Matrix ",
            "Basic matrix created for  testing",
            testTenantId,
            testPerformanceCycle.getId(),
            pillarMap1
        );
        
        if (matrix1Opt.isPresent()) {
            AssessmentMatrix matrix1 = matrix1Opt.get();
            createdMatrices.add(matrix1);
            log.info("✓ Created basic matrix: {} with {} pillars", matrix1.getName(), matrix1.getPillarMap().size());
        } else {
            log.error("✗ Failed to create basic matrix");
        }
        
        // Create matrix with custom configuration
        AssessmentConfiguration customConfig = AssessmentConfiguration.builder()
            .allowQuestionReview(false)
            .requireAllQuestions(true)
            .autoSave(false)
            .navigationMode(QuestionNavigationType.SEQUENTIAL)
            .build();
            
        Map<String, Pillar> pillarMap2 = tableRunnerHelper.createPillarsWithCategoriesMap();
        Optional<AssessmentMatrix> matrix2Opt = getAssessmentMatrixService().create(
            "Custom Config Matrix ",
            "Matrix with custom configuration for  testing",
            testTenantId,
            testPerformanceCycle.getId(),
            pillarMap2,
            customConfig
        );
        
        if (matrix2Opt.isPresent()) {
            AssessmentMatrix matrix2 = matrix2Opt.get();
            createdMatrices.add(matrix2);
            log.info("✓ Created custom config matrix: {} with navigation mode: {}", 
                matrix2.getName(), matrix2.getConfiguration().getNavigationMode());
        } else {
            log.error("✗ Failed to create custom config matrix");
        }
        
        log.info("Create operations completed. Created {} matrices.", createdMatrices.size());
    }

    private void testUpdateOperations() {
        if (createdMatrices.isEmpty()) {
            log.warn("No matrices available for update testing");
            return;
        }
        
        log.info("Testing update operations...");
        
        AssessmentMatrix matrixToUpdate = createdMatrices.get(0);
        String originalName = matrixToUpdate.getName();
        
        // Update with new pillar structure
        Map<String, Pillar> newPillarMap = createEnhancedPillarStructure();
        Optional<AssessmentMatrix> updatedMatrixOpt = getAssessmentMatrixService().update(
            matrixToUpdate.getId(),
            originalName + " (Updated)",
            matrixToUpdate.getDescription() + " - Updated with enhanced structure",
            testTenantId,
            testPerformanceCycle.getId(),
            newPillarMap
        );
        
        if (updatedMatrixOpt.isPresent()) {
            AssessmentMatrix updatedMatrix = updatedMatrixOpt.get();
            log.info("✓ Updated matrix: {} -> {}", originalName, updatedMatrix.getName());
            log.info("  Pillar count: {} -> {}", matrixToUpdate.getPillarMap().size(), updatedMatrix.getPillarMap().size());
        } else {
            log.error("✗ Failed to update matrix: {}", originalName);
        }
        
        // Update with custom configuration
        if (createdMatrices.size() > 1) {
            AssessmentMatrix matrix2 = createdMatrices.get(1);
            AssessmentConfiguration newConfig = AssessmentConfiguration.builder()
                .allowQuestionReview(true)
                .requireAllQuestions(false)
                .autoSave(true)
                .navigationMode(QuestionNavigationType.RANDOM)
                .build();
                
            Optional<AssessmentMatrix> updatedMatrix2Opt = getAssessmentMatrixService().update(
                matrix2.getId(),
                matrix2.getName() + " (Config Updated)",
                matrix2.getDescription() + " - Configuration updated",
                testTenantId,
                testPerformanceCycle.getId(),
                matrix2.getPillarMap(),
                newConfig
            );
            
            if (updatedMatrix2Opt.isPresent()) {
                AssessmentMatrix updatedMatrix2 = updatedMatrix2Opt.get();
                log.info("✓ Updated matrix configuration: {} -> navigation mode: {}", 
                    matrix2.getName(), updatedMatrix2.getConfiguration().getNavigationMode());
            } else {
                log.error("✗ Failed to update matrix configuration");
            }
        }
        
        log.info("Update operations completed.");
    }

    private void createTestQuestionsForCalculateOperations() {
        if (createdMatrices.isEmpty()) {
            log.warn("No matrices available for question creation");
            return;
        }
        
        log.info("Creating test questions using original matrix structure...");
        
        // Use the first created matrix in its original state (before any updates)
        AssessmentMatrix originalMatrix = createdMatrices.get(0);
        createTestQuestions(originalMatrix);
        
        log.info("Test questions created successfully");
    }

    private void testCalculatePointsOperations() {
        if (createdMatrices.isEmpty()) {
            log.warn("No matrices available for calculate points testing");
            return;
        }
        
        log.info("Testing calculate points operations...");
        
        AssessmentMatrix testMatrix = createdMatrices.get(0);
        
        // Test increment question count
        Optional<AssessmentMatrix> currentMatrixOpt = getAssessmentMatrixService().findById(testMatrix.getId());
        AssessmentMatrix currentMatrix = currentMatrixOpt.orElse(testMatrix);
        int originalCount = currentMatrix.getQuestionCount() != null ? currentMatrix.getQuestionCount() : 0;
        AssessmentMatrix incrementedMatrix = getAssessmentMatrixService().incrementQuestionCount(testMatrix.getId());
        log.info("✓ Incremented question count: {} -> {}", originalCount, incrementedMatrix.getQuestionCount());
        
        // Test decrement question count
        AssessmentMatrix decrementedMatrix = getAssessmentMatrixService().decrementQuestionCount(testMatrix.getId());
        log.info("✓ Decremented question count: {} -> {}", incrementedMatrix.getQuestionCount(), decrementedMatrix.getQuestionCount());
        
        // Test potential score calculation
        AssessmentMatrix scoredMatrix = getAssessmentMatrixService().updateCurrentPotentialScore(testMatrix.getId(), testTenantId);
        if (scoredMatrix.getPotentialScore() != null) {
            log.info("✓ Updated potential score - Total potential: {}", scoredMatrix.getPotentialScore().getScore());
            log.info("  Pillar scores: {}", scoredMatrix.getPotentialScore().getPillarIdToPillarScoreMap().size());
        } else {
            log.info("✓ Potential score calculation completed (no questions yet)");
        }
        
        log.info("Calculate points operations completed.");
    }

    private void testQueryOperations() {
        if (createdMatrices.isEmpty()) {
            log.warn("No matrices available for query testing");
            return;
        }
        
        log.info("Testing query operations...");
        
        AssessmentMatrix testMatrix = createdMatrices.get(0);
        
        // Test findById
        Optional<AssessmentMatrix> foundMatrixOpt = getAssessmentMatrixService().findById(testMatrix.getId());
        if (foundMatrixOpt.isPresent()) {
            log.info("✓ Found matrix by ID: {}", foundMatrixOpt.get().getName());
        } else {
            log.error("✗ Failed to find matrix by ID: {}", testMatrix.getId());
        }
        
        // Test findAllByTenantId
        List<AssessmentMatrix> tenantMatrices = getAssessmentMatrixService().findAllByTenantId(testTenantId);
        log.info("✓ Found {} matrices for tenant: {}", tenantMatrices.size(), testTenantId);
        
        // Test getAssessmentDashboard
        Optional<AssessmentDashboardData> dashboardOpt = getAssessmentMatrixService().getAssessmentDashboard(testMatrix.getId(), testTenantId);
        if (dashboardOpt.isPresent()) {
            AssessmentDashboardData dashboard = dashboardOpt.get();
            log.info("✓ Retrieved assessment dashboard - Matrix: {}, Total employees: {}", 
                dashboard.getMatrixName(), dashboard.getTotalEmployees());
        } else {
            log.info("✓ Assessment dashboard query completed (no assessments yet)");
        }
        
        log.info("Query operations completed.");
    }

    private void testDeleteOperations() {
        if (createdMatrices.isEmpty()) {
            log.warn("No matrices available for delete testing");
            return;
        }
        
        log.info("Testing delete operations...");
        
        // Test individual delete (we'll delete one matrix as a test)
        if (createdMatrices.size() > 1) {
            AssessmentMatrix matrixToDelete = createdMatrices.get(createdMatrices.size() - 1);
            String matrixName = matrixToDelete.getName();
            
            boolean deleted = getAssessmentMatrixService().deleteById(matrixToDelete.getId());
            if (deleted) {
                log.info("✓ Successfully deleted matrix: {}", matrixName);
                createdMatrices.remove(matrixToDelete);
            } else {
                log.error("✗ Failed to delete matrix: {}", matrixName);
            }
        }
        
        log.info("Delete operations completed.");
    }

    private void createTestQuestions(AssessmentMatrix matrix) {
        if (matrix.getPillarMap() == null || matrix.getPillarMap().isEmpty()) {
            return;
        }
        
        // Create one test question for the first category of the first pillar
        Pillar firstPillar = matrix.getPillarMap().values().iterator().next();
        if (firstPillar.getCategoryMap() == null || firstPillar.getCategoryMap().isEmpty()) {
            return;
        }
        
        Category firstCategory = firstPillar.getCategoryMap().values().iterator().next();
        
        Optional<Question> questionOpt = getQuestionService().create(
            "Test Question ",
            QuestionType.STAR_FIVE,
            testTenantId,
            10.0,
            matrix.getId(),
            firstPillar.getId(),
            firstCategory.getId(),
            "Test question for  demo"
        );
        
        if (questionOpt.isPresent()) {
            createdQuestions.add(questionOpt.get());
            log.info("Created test question for matrix: {}", matrix.getName());
        }
    }

    private Map<String, Pillar> createEnhancedPillarStructure() {
        // Create a more complex pillar structure for update testing
        Map<String, Pillar> enhancedPillars = new HashMap<>(tableRunnerHelper.createPillarsWithCategoriesMap());
        
        // Add an additional pillar
        Map<String, Category> additionalCategories = new HashMap<>();
        
        Category enhancedCat1 = Category.builder()
            .name("Enhanced Category 1")
            .description("Additional category for update testing")
            .build();
        additionalCategories.put("enhanced-cat-1", enhancedCat1);
        
        Category enhancedCat2 = Category.builder()
            .name("Enhanced Category 2")
            .description("Second additional category for update testing")
            .build();
        additionalCategories.put("enhanced-cat-2", enhancedCat2);
        
        Pillar additionalPillar = Pillar.builder()
            .name("Enhanced Pillar")
            .description("Additional pillar created during update testing")
            .categoryMap(additionalCategories)
            .build();
            
        enhancedPillars.put(additionalPillar.getId(), additionalPillar);
        
        return enhancedPillars;
    }

    private void cleanupAllTestData() {
        log.info("Starting comprehensive cleanup of all test data...");
        
        // Delete remaining assessment matrices
        for (AssessmentMatrix matrix : createdMatrices) {
            try {
                boolean deleted = getAssessmentMatrixService().deleteById(matrix.getId());
                if (deleted) {
                    log.info("✓ Cleaned up matrix: {}", matrix.getName());
                } else {
                    log.warn("✗ Failed to clean up matrix: {}", matrix.getName());
                }
            } catch (Exception e) {
                log.error("Error cleaning up matrix {}: {}", matrix.getName(), e.getMessage());
            }
        }
        
        // Delete test questions
        for (Question question : createdQuestions) {
            try {
                getQuestionService().deleteById(question.getId());
                log.info("✓ Cleaned up question: {}", question.getQuestion());
            } catch (Exception e) {
                log.error("Error cleaning up question {}: {}", question.getQuestion(), e.getMessage());
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
        
        log.info("Cleanup completed. Cleaned {} matrices, {} questions, 1 cycle, 1 company", 
            createdMatrices.size(), createdQuestions.size());
    }

    // Service getters with lazy initialization
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

    private QuestionService getQuestionService() {
        if (questionService == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            questionService = serviceComponent.buildQuestionService();
        }
        return questionService;
    }
}
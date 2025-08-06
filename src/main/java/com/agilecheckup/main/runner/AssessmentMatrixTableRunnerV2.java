package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.AssessmentConfigurationV2;
import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.CategoryV2;
import com.agilecheckup.persistency.entity.CompanyV2;
import com.agilecheckup.persistency.entity.PerformanceCycleV2;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.persistency.entity.QuestionNavigationType;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.QuestionV2;
import com.agilecheckup.service.AssessmentMatrixServiceV2;
import com.agilecheckup.service.CompanyServiceV2;
import com.agilecheckup.service.PerformanceCycleServiceV2;
import com.agilecheckup.service.QuestionServiceV2;
import com.agilecheckup.service.dto.AssessmentDashboardData;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
public class AssessmentMatrixTableRunnerV2 implements CrudRunner {

    private AssessmentMatrixServiceV2 assessmentMatrixServiceV2;
    private PerformanceCycleServiceV2 performanceCycleServiceV2;
    private CompanyServiceV2 companyServiceV2;
    private QuestionServiceV2 questionServiceV2;

    private final TableRunnerHelper tableRunnerHelper = new TableRunnerHelper();
    private final boolean shouldCleanAfterComplete;
    
    // Test data tracking for cleanup
    private final List<AssessmentMatrixV2> createdMatrices = new ArrayList<>();
    private final List<QuestionV2> createdQuestions = new ArrayList<>();
    private CompanyV2 testCompany;
    private PerformanceCycleV2 testPerformanceCycle;
    private final String testTenantId = "test-tenant-v2-" + System.currentTimeMillis();

    public AssessmentMatrixTableRunnerV2(boolean shouldCleanAfterComplete) {
        this.shouldCleanAfterComplete = shouldCleanAfterComplete;
    }

    @Override
    public void run() {
        log.info("\n=== AssessmentMatrixV2 Comprehensive CRUD Operations Demo ===");
        
        try {
            // 1. Setup test dependencies
            setupTestDependencies();
            
            if (testCompany == null || testPerformanceCycle == null) {
                log.error("Failed to create test dependencies. Aborting demo.");
                return;
            }
            
            // 2. Test Create Operations
            log.info("\n1. Testing V2 Create Operations...");
            testCreateOperations();
            
            // 3. Create test questions (before updates, so pillar/category IDs exist)
            log.info("\n2. Creating test questions for calculate operations...");
            createTestQuestionsForCalculateOperations();
            
            // 4. Test Update Operations
            log.info("\n3. Testing V2 Update Operations...");
            testUpdateOperations();
            
            // 5. Test Calculate Points Operations
            log.info("\n4. Testing V2 Calculate Points Operations...");
            testCalculatePointsOperations();
            
            // 6. Test Query Operations
            log.info("\n5. Testing V2 Query Operations...");
            testQueryOperations();
            
            // 7. Test Delete Operations
            log.info("\n6. Testing V2 Delete Operations...");
            testDeleteOperations();
            
            log.info("\n=== AssessmentMatrixV2 Demo Completed Successfully ===");
            
        } catch (Exception e) {
            log.error("Error during AssessmentMatrixV2 demo: {}", e.getMessage(), e);
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
        Optional<CompanyV2> companyOpt = getCompanyService().create(
            "12345678901",
            "Test Company V2 - " + System.currentTimeMillis(),
            "test@company.com",
            "Test company for AssessmentMatrixV2 demo",
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
        Optional<PerformanceCycleV2> cycleOpt = getPerformanceCycleService().create(
            testTenantId,
            "Test Cycle V2 - " + System.currentTimeMillis(),
            "Test performance cycle for AssessmentMatrixV2 demo",
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
        Map<String, PillarV2> pillarMap1 = tableRunnerHelper.createPillarsWithCategoriesMapV2();
        Optional<AssessmentMatrixV2> matrix1Opt = getAssessmentMatrixServiceV2().create(
            "Basic Assessment Matrix V2",
            "Basic matrix created for V2 testing",
            testTenantId,
            testPerformanceCycle.getId(),
            pillarMap1
        );
        
        if (matrix1Opt.isPresent()) {
            AssessmentMatrixV2 matrix1 = matrix1Opt.get();
            createdMatrices.add(matrix1);
            log.info("✓ Created basic matrix: {} with {} pillars", matrix1.getName(), matrix1.getPillarMap().size());
        } else {
            log.error("✗ Failed to create basic matrix");
        }
        
        // Create matrix with custom configuration
        AssessmentConfigurationV2 customConfig = AssessmentConfigurationV2.builder()
            .allowQuestionReview(false)
            .requireAllQuestions(true)
            .autoSave(false)
            .navigationMode(QuestionNavigationType.SEQUENTIAL)
            .build();
            
        Map<String, PillarV2> pillarMap2 = tableRunnerHelper.createPillarsWithCategoriesMapV2();
        Optional<AssessmentMatrixV2> matrix2Opt = getAssessmentMatrixServiceV2().create(
            "Custom Config Matrix V2",
            "Matrix with custom configuration for V2 testing",
            testTenantId,
            testPerformanceCycle.getId(),
            pillarMap2,
            customConfig
        );
        
        if (matrix2Opt.isPresent()) {
            AssessmentMatrixV2 matrix2 = matrix2Opt.get();
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
        
        AssessmentMatrixV2 matrixToUpdate = createdMatrices.get(0);
        String originalName = matrixToUpdate.getName();
        
        // Update with new pillar structure
        Map<String, PillarV2> newPillarMap = createEnhancedPillarStructure();
        Optional<AssessmentMatrixV2> updatedMatrixOpt = getAssessmentMatrixServiceV2().update(
            matrixToUpdate.getId(),
            originalName + " (Updated)",
            matrixToUpdate.getDescription() + " - Updated with enhanced structure",
            testTenantId,
            testPerformanceCycle.getId(),
            newPillarMap
        );
        
        if (updatedMatrixOpt.isPresent()) {
            AssessmentMatrixV2 updatedMatrix = updatedMatrixOpt.get();
            log.info("✓ Updated matrix: {} -> {}", originalName, updatedMatrix.getName());
            log.info("  Pillar count: {} -> {}", matrixToUpdate.getPillarMap().size(), updatedMatrix.getPillarMap().size());
        } else {
            log.error("✗ Failed to update matrix: {}", originalName);
        }
        
        // Update with custom configuration
        if (createdMatrices.size() > 1) {
            AssessmentMatrixV2 matrix2 = createdMatrices.get(1);
            AssessmentConfigurationV2 newConfig = AssessmentConfigurationV2.builder()
                .allowQuestionReview(true)
                .requireAllQuestions(false)
                .autoSave(true)
                .navigationMode(QuestionNavigationType.RANDOM)
                .build();
                
            Optional<AssessmentMatrixV2> updatedMatrix2Opt = getAssessmentMatrixServiceV2().update(
                matrix2.getId(),
                matrix2.getName() + " (Config Updated)",
                matrix2.getDescription() + " - Configuration updated",
                testTenantId,
                testPerformanceCycle.getId(),
                matrix2.getPillarMap(),
                newConfig
            );
            
            if (updatedMatrix2Opt.isPresent()) {
                AssessmentMatrixV2 updatedMatrix2 = updatedMatrix2Opt.get();
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
        AssessmentMatrixV2 originalMatrix = createdMatrices.get(0);
        createTestQuestions(originalMatrix);
        
        log.info("Test questions created successfully");
    }

    private void testCalculatePointsOperations() {
        if (createdMatrices.isEmpty()) {
            log.warn("No matrices available for calculate points testing");
            return;
        }
        
        log.info("Testing calculate points operations...");
        
        AssessmentMatrixV2 testMatrix = createdMatrices.get(0);
        
        // Test increment question count
        Optional<AssessmentMatrixV2> currentMatrixOpt = getAssessmentMatrixServiceV2().findById(testMatrix.getId());
        AssessmentMatrixV2 currentMatrix = currentMatrixOpt.orElse(testMatrix);
        int originalCount = currentMatrix.getQuestionCount() != null ? currentMatrix.getQuestionCount() : 0;
        AssessmentMatrixV2 incrementedMatrix = getAssessmentMatrixServiceV2().incrementQuestionCount(testMatrix.getId());
        log.info("✓ Incremented question count: {} -> {}", originalCount, incrementedMatrix.getQuestionCount());
        
        // Test decrement question count
        AssessmentMatrixV2 decrementedMatrix = getAssessmentMatrixServiceV2().decrementQuestionCount(testMatrix.getId());
        log.info("✓ Decremented question count: {} -> {}", incrementedMatrix.getQuestionCount(), decrementedMatrix.getQuestionCount());
        
        // Test potential score calculation
        AssessmentMatrixV2 scoredMatrix = getAssessmentMatrixServiceV2().updateCurrentPotentialScore(testMatrix.getId(), testTenantId);
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
        
        AssessmentMatrixV2 testMatrix = createdMatrices.get(0);
        
        // Test findById
        Optional<AssessmentMatrixV2> foundMatrixOpt = getAssessmentMatrixServiceV2().findById(testMatrix.getId());
        if (foundMatrixOpt.isPresent()) {
            log.info("✓ Found matrix by ID: {}", foundMatrixOpt.get().getName());
        } else {
            log.error("✗ Failed to find matrix by ID: {}", testMatrix.getId());
        }
        
        // Test findAllByTenantId
        List<AssessmentMatrixV2> tenantMatrices = getAssessmentMatrixServiceV2().findAllByTenantId(testTenantId);
        log.info("✓ Found {} matrices for tenant: {}", tenantMatrices.size(), testTenantId);
        
        // Test getAssessmentDashboard
        Optional<AssessmentDashboardData> dashboardOpt = getAssessmentMatrixServiceV2().getAssessmentDashboard(testMatrix.getId(), testTenantId);
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
            AssessmentMatrixV2 matrixToDelete = createdMatrices.get(createdMatrices.size() - 1);
            String matrixName = matrixToDelete.getName();
            
            boolean deleted = getAssessmentMatrixServiceV2().deleteById(matrixToDelete.getId());
            if (deleted) {
                log.info("✓ Successfully deleted matrix: {}", matrixName);
                createdMatrices.remove(matrixToDelete);
            } else {
                log.error("✗ Failed to delete matrix: {}", matrixName);
            }
        }
        
        log.info("Delete operations completed.");
    }

    private void createTestQuestions(AssessmentMatrixV2 matrix) {
        if (matrix.getPillarMap() == null || matrix.getPillarMap().isEmpty()) {
            return;
        }
        
        // Create one test question for the first category of the first pillar
        PillarV2 firstPillar = matrix.getPillarMap().values().iterator().next();
        if (firstPillar.getCategoryMap() == null || firstPillar.getCategoryMap().isEmpty()) {
            return;
        }
        
        CategoryV2 firstCategory = firstPillar.getCategoryMap().values().iterator().next();
        
        Optional<QuestionV2> questionOpt = getQuestionServiceV2().create(
            "Test Question V2",
            QuestionType.STAR_FIVE,
            testTenantId,
            10.0,
            matrix.getId(),
            firstPillar.getId(),
            firstCategory.getId(),
            "Test question for V2 demo"
        );
        
        if (questionOpt.isPresent()) {
            createdQuestions.add(questionOpt.get());
            log.info("Created test question for matrix: {}", matrix.getName());
        }
    }

    private Map<String, PillarV2> createEnhancedPillarStructure() {
        // Create a more complex pillar structure for update testing
        Map<String, PillarV2> enhancedPillars = new HashMap<>(tableRunnerHelper.createPillarsWithCategoriesMapV2());
        
        // Add an additional pillar
        Map<String, CategoryV2> additionalCategories = new HashMap<>();
        
        CategoryV2 enhancedCat1 = CategoryV2.builder()
            .name("Enhanced Category 1")
            .description("Additional category for update testing")
            .build();
        additionalCategories.put("enhanced-cat-1", enhancedCat1);
        
        CategoryV2 enhancedCat2 = CategoryV2.builder()
            .name("Enhanced Category 2")
            .description("Second additional category for update testing")
            .build();
        additionalCategories.put("enhanced-cat-2", enhancedCat2);
        
        PillarV2 additionalPillar = PillarV2.builder()
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
        for (AssessmentMatrixV2 matrix : createdMatrices) {
            try {
                boolean deleted = getAssessmentMatrixServiceV2().deleteById(matrix.getId());
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
        for (QuestionV2 question : createdQuestions) {
            try {
                getQuestionServiceV2().deleteById(question.getId());
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
    private AssessmentMatrixServiceV2 getAssessmentMatrixServiceV2() {
        if (assessmentMatrixServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            assessmentMatrixServiceV2 = serviceComponent.buildAssessmentMatrixServiceV2();
        }
        return assessmentMatrixServiceV2;
    }

    private PerformanceCycleServiceV2 getPerformanceCycleService() {
        if (performanceCycleServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            performanceCycleServiceV2 = serviceComponent.buildPerformanceCycleService();
        }
        return performanceCycleServiceV2;
    }

    private CompanyServiceV2 getCompanyService() {
        if (companyServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            companyServiceV2 = serviceComponent.buildCompanyService();
        }
        return companyServiceV2;
    }

    private QuestionServiceV2 getQuestionServiceV2() {
        if (questionServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            questionServiceV2 = serviceComponent.buildQuestionServiceV2();
        }
        return questionServiceV2;
    }
}
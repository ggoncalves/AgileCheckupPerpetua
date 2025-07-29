package com.agilecheckup.main.runner;

import com.agilecheckup.persistency.entity.AssessmentConfiguration;
import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.CategoryV2;
import com.agilecheckup.persistency.entity.CompanyV2;
import com.agilecheckup.persistency.entity.PerformanceCycleV2;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.persistency.entity.QuestionNavigationType;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.service.AssessmentMatrixServiceV2;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.PerformanceCycleService;
import com.agilecheckup.service.QuestionService;
import com.agilecheckup.service.dto.AssessmentDashboardData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentMatrixTableRunnerV2Test {

    @Mock
    private AssessmentMatrixServiceV2 assessmentMatrixServiceV2;

    @Mock
    private PerformanceCycleService performanceCycleService;

    @Mock
    private CompanyService companyService;

    @Mock
    private QuestionService questionService;

    @Mock
    private TableRunnerHelper tableRunnerHelper;

    private AssessmentMatrixTableRunnerV2 runner;

    private CompanyV2 testCompany;
    private PerformanceCycleV2 testPerformanceCycle;
    private AssessmentMatrixV2 testMatrix;
    private Map<String, PillarV2> testPillarMap;

    @BeforeEach
    void setUp() throws Exception {
        runner = new AssessmentMatrixTableRunnerV2(true);
        
        // Use reflection to inject mocked services
        injectMockServices();
        
        // Setup test data
        setupTestData();
        
        // Setup mock behaviors
        setupMockBehaviors();
    }

    @Test
    void shouldSuccessfullyRunCompleteDemo() {
        // When
        runner.run();

        // Then - Verify all major operations were called in the correct order
        // 1. Setup dependencies
        verify(companyService).create(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(performanceCycleService).create(anyString(), anyString(), anyString(), anyString(), eq(true), eq(true), any(LocalDate.class), any(LocalDate.class));
        
        // 2. Create operations - 1 basic matrix, 1 with custom config
        verify(assessmentMatrixServiceV2).create(anyString(), anyString(), anyString(), anyString(), any(Map.class));
        verify(assessmentMatrixServiceV2).create(anyString(), anyString(), anyString(), anyString(), any(Map.class), any(AssessmentConfiguration.class));
        
        // 3. Create test questions (happens before updates now)
        verify(questionService).create(anyString(), any(QuestionType.class), anyString(), any(Double.class), anyString(), anyString(), anyString(), anyString());
        
        // 4. Update operations - 1 update with new pillars, optionally 1 with new config
        verify(assessmentMatrixServiceV2).update(anyString(), anyString(), anyString(), anyString(), anyString(), any(Map.class));
        // Note: Second update with config is optional based on number of matrices created
        
        // 5. Calculate points operations
        verify(assessmentMatrixServiceV2).incrementQuestionCount(anyString());
        verify(assessmentMatrixServiceV2).decrementQuestionCount(anyString());
        verify(assessmentMatrixServiceV2).updateCurrentPotentialScore(anyString(), anyString());
        
        // 6. Query operations (findById is called twice total - once in calculate, once in query)
        verify(assessmentMatrixServiceV2, times(2)).findById(anyString());
        verify(assessmentMatrixServiceV2).findAllByTenantId(anyString());
        verify(assessmentMatrixServiceV2).getAssessmentDashboard(anyString(), anyString());
        
        // 7. Delete operations - deletes one matrix as test
        // 8. Cleanup - deletes remaining matrix
        verify(assessmentMatrixServiceV2, times(2)).deleteById(anyString()); // One in delete test, one in cleanup
    }

    @Test
    void shouldSetupTestDependenciesCorrectly() throws Exception {
        // When - Call the private setupTestDependencies method
        Method setupMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("setupTestDependencies");
        setupMethod.setAccessible(true);
        setupMethod.invoke(runner);

        // Then
        verify(companyService).create(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(performanceCycleService).create(anyString(), anyString(), anyString(), anyString(), eq(true), eq(true), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void shouldTestCreateOperationsWithBothConfigurations() throws Exception {
        // Given - Setup dependencies first
        Method setupMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("setupTestDependencies");
        setupMethod.setAccessible(true);
        setupMethod.invoke(runner);

        // When - Call the private testCreateOperations method
        Method createMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("testCreateOperations");
        createMethod.setAccessible(true);
        createMethod.invoke(runner);

        // Then - Verify 1 basic create and 1 with custom config
        verify(assessmentMatrixServiceV2).create(anyString(), anyString(), anyString(), anyString(), any(Map.class));
        verify(assessmentMatrixServiceV2).create(anyString(), anyString(), anyString(), anyString(), any(Map.class), any(AssessmentConfiguration.class));
    }

    @Test
    void shouldTestUpdateOperationsWithPillarAndConfigChanges() throws Exception {
        // Given - Setup dependencies and create matrices
        setupDependenciesAndCreateMatrices();

        // When - Call the private testUpdateOperations method
        Method updateMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("testUpdateOperations");
        updateMethod.setAccessible(true);
        updateMethod.invoke(runner);

        // Then - Verify 1 update with new pillars, and 1 update with new config (if 2 matrices exist)
        verify(assessmentMatrixServiceV2).update(anyString(), anyString(), anyString(), anyString(), anyString(), any(Map.class));
        verify(assessmentMatrixServiceV2).update(anyString(), anyString(), anyString(), anyString(), anyString(), any(Map.class), any(AssessmentConfiguration.class));
    }

    @Test
    void shouldTestCalculatePointsOperations() throws Exception {
        // Given - Setup dependencies and create matrices
        setupDependenciesAndCreateMatrices();
        
        // Create questions first (as per new flow)
        Method createQuestionsMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("createTestQuestionsForCalculateOperations");
        createQuestionsMethod.setAccessible(true);
        createQuestionsMethod.invoke(runner);

        // When - Call the private testCalculatePointsOperations method
        Method calculateMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("testCalculatePointsOperations");
        calculateMethod.setAccessible(true);
        calculateMethod.invoke(runner);

        // Then - Question creation happens in createTestQuestionsForCalculateOperations
        verify(questionService).create(anyString(), any(QuestionType.class), anyString(), any(Double.class), anyString(), anyString(), anyString(), anyString());
        verify(assessmentMatrixServiceV2).findById(anyString()); // Used to get current matrix state
        verify(assessmentMatrixServiceV2).incrementQuestionCount(anyString());
        verify(assessmentMatrixServiceV2).decrementQuestionCount(anyString());
        verify(assessmentMatrixServiceV2).updateCurrentPotentialScore(anyString(), anyString());
    }

    @Test
    void shouldTestQueryOperations() throws Exception {
        // Given - Setup dependencies and create matrices
        setupDependenciesAndCreateMatrices();

        // When - Call the private testQueryOperations method
        Method queryMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("testQueryOperations");
        queryMethod.setAccessible(true);
        queryMethod.invoke(runner);

        // Then
        verify(assessmentMatrixServiceV2).findById(anyString());
        verify(assessmentMatrixServiceV2).findAllByTenantId(anyString());
        verify(assessmentMatrixServiceV2).getAssessmentDashboard(anyString(), anyString());
    }

    @Test
    void shouldTestDeleteOperations() throws Exception {
        // Given - Setup dependencies and create matrices
        setupDependenciesAndCreateMatrices();

        // When - Call the private testDeleteOperations method
        Method deleteMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("testDeleteOperations");
        deleteMethod.setAccessible(true);
        deleteMethod.invoke(runner);

        // Then
        verify(assessmentMatrixServiceV2).deleteById(anyString());
    }

    @Test
    void shouldCleanupAllTestDataWhenEnabled() throws Exception {
        // Given - Runner with cleanup enabled and some test data
        AssessmentMatrixTableRunnerV2 cleanupRunner = new AssessmentMatrixTableRunnerV2(true);
        injectMockServicesIntoRunner(cleanupRunner);
        setupDependenciesAndCreateMatricesForRunner(cleanupRunner);
        
        // Create questions to ensure cleanup is tested
        Method createQuestionsMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("createTestQuestionsForCalculateOperations");
        createQuestionsMethod.setAccessible(true);
        createQuestionsMethod.invoke(cleanupRunner);

        // When - Call the private cleanupAllTestData method
        Method cleanupMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("cleanupAllTestData");
        cleanupMethod.setAccessible(true);
        cleanupMethod.invoke(cleanupRunner);

        // Then - Should delete both matrices created (2 calls)
        verify(assessmentMatrixServiceV2, times(2)).deleteById(anyString());
        verify(questionService).delete(any(Question.class));
        verify(performanceCycleService).deleteById(anyString());
        verify(companyService).deleteById(anyString());
    }

    @Test
    void shouldHandleEmptyMatricesGracefully() throws Exception {
        // Given - No matrices created
        Method updateMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("testUpdateOperations");
        updateMethod.setAccessible(true);

        // When - This should not throw exception
        updateMethod.invoke(runner);

        // Then - No service calls should be made for updates
        verify(assessmentMatrixServiceV2, times(0)).update(anyString(), anyString(), anyString(), anyString(), anyString(), any(Map.class));
    }

    @Test
    void shouldCreateEnhancedPillarStructureForUpdates() throws Exception {
        // Given
        when(tableRunnerHelper.createPillarsWithCategoriesMapV2()).thenReturn(testPillarMap);

        // When - Call the private createEnhancedPillarStructure method
        Method enhancedMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("createEnhancedPillarStructure");
        enhancedMethod.setAccessible(true);
        Map<String, PillarV2> result = (Map<String, PillarV2>) enhancedMethod.invoke(runner);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(testPillarMap.size() + 1); // Original + 1 enhanced pillar
        verify(tableRunnerHelper).createPillarsWithCategoriesMapV2();
    }

    private void injectMockServices() throws Exception {
        injectMockServicesIntoRunner(runner);
    }

    private void injectMockServicesIntoRunner(AssessmentMatrixTableRunnerV2 targetRunner) throws Exception {
        Field assessmentServiceField = AssessmentMatrixTableRunnerV2.class.getDeclaredField("assessmentMatrixServiceV2");
        assessmentServiceField.setAccessible(true);
        assessmentServiceField.set(targetRunner, assessmentMatrixServiceV2);

        Field performanceServiceField = AssessmentMatrixTableRunnerV2.class.getDeclaredField("performanceCycleService");
        performanceServiceField.setAccessible(true);
        performanceServiceField.set(targetRunner, performanceCycleService);

        Field companyServiceField = AssessmentMatrixTableRunnerV2.class.getDeclaredField("companyService");
        companyServiceField.setAccessible(true);
        companyServiceField.set(targetRunner, companyService);

        Field questionServiceField = AssessmentMatrixTableRunnerV2.class.getDeclaredField("questionService");
        questionServiceField.setAccessible(true);
        questionServiceField.set(targetRunner, questionService);

        Field helperField = AssessmentMatrixTableRunnerV2.class.getDeclaredField("tableRunnerHelper");
        helperField.setAccessible(true);
        helperField.set(targetRunner, tableRunnerHelper);
    }

    private void setupTestData() {
        // Test company
        testCompany = CompanyV2.builder()
            .id("test-company-id")
            .name("Test Company V2")
            .email("test@company.com")
            .description("Test company for runner")
            .tenantId("test-tenant-123")
            .build();

        // Test performance cycle
        testPerformanceCycle = PerformanceCycleV2.builder()
            .id("test-cycle-id")
            .name("Test Cycle V2")
            .description("Test performance cycle")
            .tenantId("test-tenant-123")
            .companyId("test-company-id")
            .isActive(true)
            .isTimeSensitive(true)
            .startDate(LocalDate.of(2024, 1, 1))
            .endDate(LocalDate.of(2024, 12, 31))
            .build();

        // Test pillar map
        testPillarMap = new HashMap<>();
        CategoryV2 category = CategoryV2.builder()
            .id("test-category-id")
            .name("Test Category")
            .description("Test category description")
            .build();
        
        Map<String, CategoryV2> categoryMap = new HashMap<>();
        categoryMap.put(category.getId(), category);
        
        PillarV2 pillar = PillarV2.builder()
            .id("test-pillar-id")
            .name("Test Pillar")
            .description("Test pillar description")
            .categoryMap(categoryMap)
            .build();
        
        testPillarMap.put(pillar.getId(), pillar);

        // Test matrix
        AssessmentConfiguration testConfig = AssessmentConfiguration.builder()
            .allowQuestionReview(true)
            .requireAllQuestions(true)
            .autoSave(true)
            .navigationMode(QuestionNavigationType.RANDOM)
            .build();
            
        testMatrix = AssessmentMatrixV2.builder()
            .id("test-matrix-id")
            .name("Test Matrix V2")
            .description("Test assessment matrix")
            .tenantId("test-tenant-123")
            .performanceCycleId("test-cycle-id")
            .pillarMap(testPillarMap)
            .questionCount(0)
            .configuration(testConfig)
            .build();
    }

    private void setupMockBehaviors() {
        // Company service mocks
        lenient().doReturn(Optional.of(testCompany)).when(companyService).create(anyString(), anyString(), anyString(), anyString(), anyString());
        lenient().doReturn(true).when(companyService).deleteById(anyString());

        // Performance cycle service mocks
        lenient().doReturn(Optional.of(testPerformanceCycle)).when(performanceCycleService).create(anyString(), anyString(), anyString(), anyString(), eq(true), eq(true), any(LocalDate.class), any(LocalDate.class));
        lenient().doReturn(true).when(performanceCycleService).deleteById(anyString());

        // Assessment matrix service mocks
        lenient().doReturn(Optional.of(testMatrix)).when(assessmentMatrixServiceV2).create(anyString(), anyString(), anyString(), anyString(), any(Map.class));
        lenient().doReturn(Optional.of(testMatrix)).when(assessmentMatrixServiceV2).create(anyString(), anyString(), anyString(), anyString(), any(Map.class), any(AssessmentConfiguration.class));
        lenient().doReturn(Optional.of(testMatrix)).when(assessmentMatrixServiceV2).update(anyString(), anyString(), anyString(), anyString(), anyString(), any(Map.class));
        lenient().doReturn(Optional.of(testMatrix)).when(assessmentMatrixServiceV2).update(anyString(), anyString(), anyString(), anyString(), anyString(), any(Map.class), any(AssessmentConfiguration.class));
        lenient().doReturn(Optional.of(testMatrix)).when(assessmentMatrixServiceV2).findById(anyString());
        lenient().doReturn(List.of(testMatrix)).when(assessmentMatrixServiceV2).findAllByTenantId(anyString());
        lenient().doReturn(testMatrix).when(assessmentMatrixServiceV2).incrementQuestionCount(anyString());
        lenient().doReturn(testMatrix).when(assessmentMatrixServiceV2).decrementQuestionCount(anyString());
        lenient().doReturn(testMatrix).when(assessmentMatrixServiceV2).updateCurrentPotentialScore(anyString(), anyString());
        lenient().doReturn(true).when(assessmentMatrixServiceV2).deleteById(anyString());

        // Dashboard mock
        AssessmentDashboardData dashboardData = AssessmentDashboardData.builder()
            .assessmentMatrixId(testMatrix.getId())
            .matrixName(testMatrix.getName())
            .totalEmployees(0)
            .completedAssessments(0)
            .build();
        lenient().doReturn(Optional.of(dashboardData)).when(assessmentMatrixServiceV2).getAssessmentDashboard(anyString(), anyString());

        // Question service mock
        Question testQuestion = Question.builder()
            .id("test-question-id")
            .question("Test Question")
            .questionType(QuestionType.STAR_FIVE)
            .tenantId("test-tenant-123")
            .assessmentMatrixId("test-matrix-id")
            .pillarId("test-pillar-id")
            .pillarName("Test Pillar")
            .categoryId("test-category-id")
            .categoryName("Test Category")
            .build();
        lenient().doReturn(Optional.of(testQuestion)).when(questionService).create(anyString(), any(QuestionType.class), anyString(), any(Double.class), anyString(), anyString(), anyString(), anyString());

        // Table runner helper mock
        lenient().doReturn(testPillarMap).when(tableRunnerHelper).createPillarsWithCategoriesMapV2();
        lenient().doReturn(questionService).when(tableRunnerHelper).getQuestionService();
    }

    private void setupDependenciesAndCreateMatrices() throws Exception {
        // Setup dependencies
        Method setupMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("setupTestDependencies");
        setupMethod.setAccessible(true);
        setupMethod.invoke(runner);

        // Create matrices
        Method createMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("testCreateOperations");
        createMethod.setAccessible(true);
        createMethod.invoke(runner);
    }

    private void setupDependenciesAndCreateMatricesForRunner(AssessmentMatrixTableRunnerV2 targetRunner) throws Exception {
        // Setup dependencies
        Method setupMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("setupTestDependencies");
        setupMethod.setAccessible(true);
        setupMethod.invoke(targetRunner);

        // Create matrices
        Method createMethod = AssessmentMatrixTableRunnerV2.class.getDeclaredMethod("testCreateOperations");
        createMethod.setAccessible(true);
        createMethod.invoke(targetRunner);
    }
}
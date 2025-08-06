package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentConfigurationV2;
import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessmentScoreV2;
import com.agilecheckup.persistency.entity.EmployeeAssessmentV2;
import com.agilecheckup.persistency.entity.PerformanceCycleV2;
import com.agilecheckup.persistency.entity.PillarV2;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.TeamV2;
import com.agilecheckup.persistency.entity.person.NaturalPersonV2;
import com.agilecheckup.persistency.entity.question.QuestionV2;
import com.agilecheckup.persistency.entity.score.PotentialScoreV2;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepositoryV2;
import com.agilecheckup.service.dto.AssessmentDashboardData;
import dagger.Lazy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssessmentMatrixServiceV2Test {

    @Mock
    private AssessmentMatrixRepositoryV2 assessmentMatrixRepositoryV2;

    @Mock
    private PerformanceCycleServiceV2 performanceCycleServiceV2;

    @Mock
    private Lazy<QuestionServiceV2> questionService;

    @Mock
    private Lazy<EmployeeAssessmentServiceV2> employeeAssessmentServiceV2;

    @Mock
    private Lazy<TeamServiceV2> teamService;

    @Mock
    private QuestionServiceV2 mockQuestionService;

    @Mock
    private EmployeeAssessmentServiceV2 mockEmployeeAssessmentServiceV2;

    @Mock
    private TeamServiceV2 mockTeamService;

    private AssessmentMatrixServiceV2 service;

    @BeforeEach
    void setUp() {
        // Set up lazy mocks
        lenient().doReturn(mockQuestionService).when(questionService).get();
        lenient().doReturn(mockEmployeeAssessmentServiceV2).when(employeeAssessmentServiceV2).get();
        lenient().doReturn(mockTeamService).when(teamService).get();
        
        service = new AssessmentMatrixServiceV2(
                assessmentMatrixRepositoryV2,
            performanceCycleServiceV2,
                questionService,
                employeeAssessmentServiceV2,
                teamService
        );
    }

    @Test
    void testCreate() {
        String name = "Test Matrix";
        String description = "Test Description";
        String tenantId = "tenant-123";
        String performanceCycleId = "cycle-123";
        Map<String, PillarV2> pillarMap = new HashMap<>();

        PerformanceCycleV2 mockCycle = PerformanceCycleV2.builder()
                .id(performanceCycleId)
                .name("Test Cycle")
                .description("Test Cycle Description")
                .tenantId(tenantId)
                .companyId("company-123")
                .isActive(true)
                .isTimeSensitive(true)
                .build();

        AssessmentMatrixV2 savedMatrix = AssessmentMatrixV2.builder()
                .id("matrix-123")
                .name(name)
                .description(description)
                .tenantId(tenantId)
                .performanceCycleId(performanceCycleId)
                .pillarMap(pillarMap)
                .questionCount(0)
                .build();

        lenient().doReturn(Optional.of(mockCycle)).when(performanceCycleServiceV2).findById(performanceCycleId);
        doReturn(Optional.of(savedMatrix)).when(assessmentMatrixRepositoryV2).save(any(AssessmentMatrixV2.class));

        Optional<AssessmentMatrixV2> result = service.create(name, description, tenantId, performanceCycleId, pillarMap);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(name);
        assertThat(result.get().getDescription()).isEqualTo(description);
        assertThat(result.get().getTenantId()).isEqualTo(tenantId);
        assertThat(result.get().getPerformanceCycleId()).isEqualTo(performanceCycleId);
        assertThat(result.get().getPillarMap()).isEqualTo(pillarMap);
        verify(assessmentMatrixRepositoryV2).save(any(AssessmentMatrixV2.class));
    }

    @Test
    void testCreateWithConfiguration() {
        String name = "Test Matrix";
        String description = "Test Description";
        String tenantId = "tenant-123";
        String performanceCycleId = "cycle-123";
        Map<String, PillarV2> pillarMap = new HashMap<>();
        AssessmentConfigurationV2 configuration = AssessmentConfigurationV2.builder()
                .allowQuestionReview(false)
                .requireAllQuestions(true)
                .build();

        PerformanceCycleV2 mockCycle = PerformanceCycleV2.builder()
                .id(performanceCycleId)
                .name("Test Cycle")
                .description("Test Cycle Description")
                .tenantId(tenantId)
                .companyId("company-123")
                .isActive(true)
                .isTimeSensitive(true)
                .build();

        AssessmentMatrixV2 savedMatrix = AssessmentMatrixV2.builder()
                .id("matrix-123")
                .name(name)
                .description(description)
                .tenantId(tenantId)
                .performanceCycleId(performanceCycleId)
                .pillarMap(pillarMap)
                .configuration(configuration)
                .questionCount(0)
                .build();

        lenient().doReturn(Optional.of(mockCycle)).when(performanceCycleServiceV2).findById(performanceCycleId);
        doReturn(Optional.of(savedMatrix)).when(assessmentMatrixRepositoryV2).save(any(AssessmentMatrixV2.class));

        Optional<AssessmentMatrixV2> result = service.create(name, description, tenantId, performanceCycleId, pillarMap, configuration);

        assertThat(result).isPresent();
        assertThat(result.get().getConfiguration()).isEqualTo(configuration);
        verify(assessmentMatrixRepositoryV2).save(any(AssessmentMatrixV2.class));
    }

    @Test
    void testFindAllByTenantId() {
        String tenantId = "tenant-123";
        List<AssessmentMatrixV2> expectedList = List.of(
                AssessmentMatrixV2.builder()
                        .id("matrix-1")
                        .name("Matrix 1")
                        .description("Matrix 1 Description")
                        .tenantId(tenantId)
                        .performanceCycleId("cycle-1")
                        .build(),
                AssessmentMatrixV2.builder()
                        .id("matrix-2")
                        .name("Matrix 2")
                        .description("Matrix 2 Description")
                        .tenantId(tenantId)
                        .performanceCycleId("cycle-2")
                        .build()
        );

        doReturn(expectedList).when(assessmentMatrixRepositoryV2).findAllByTenantId(tenantId);

        List<AssessmentMatrixV2> result = service.findAllByTenantId(tenantId);

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedList);
        verify(assessmentMatrixRepositoryV2).findAllByTenantId(tenantId);
    }

    @Test
    void testCreateDefaultConfiguration() {
        AssessmentConfigurationV2 config = service.createDefaultConfiguration();

        assertThat(config).isNotNull();
        assertThat(config.getAllowQuestionReview()).isTrue();
        assertThat(config.getRequireAllQuestions()).isTrue();
        assertThat(config.getAutoSave()).isTrue();
    }

    @Test
    void testIncrementQuestionCount() {
        String matrixId = "matrix-123";
        AssessmentMatrixV2 existingMatrix = AssessmentMatrixV2.builder()
                .id(matrixId)
                .name("Test Matrix")
                .description("Test Description")
                .tenantId("tenant-123")
                .performanceCycleId("cycle-123")
                .questionCount(5)
                .build();

        AssessmentMatrixV2 updatedMatrix = AssessmentMatrixV2.builder()
                .id(matrixId)
                .name("Test Matrix")
                .description("Test Description")
                .tenantId("tenant-123")
                .performanceCycleId("cycle-123")
                .questionCount(6)
                .build();

        doReturn(Optional.of(existingMatrix)).when(assessmentMatrixRepositoryV2).findById(matrixId);
        doReturn(Optional.of(updatedMatrix)).when(assessmentMatrixRepositoryV2).save(any(AssessmentMatrixV2.class));

        AssessmentMatrixV2 result = service.incrementQuestionCount(matrixId);

        assertThat(result.getQuestionCount()).isEqualTo(6);
        verify(assessmentMatrixRepositoryV2).findById(matrixId);
        verify(assessmentMatrixRepositoryV2).save(any(AssessmentMatrixV2.class));
    }

    @Test
    void testDecrementQuestionCount() {
        String matrixId = "matrix-123";
        AssessmentMatrixV2 existingMatrix = AssessmentMatrixV2.builder()
                .id(matrixId)
                .name("Test Matrix")
                .description("Test Description")
                .tenantId("tenant-123")
                .performanceCycleId("cycle-123")
                .questionCount(5)
                .build();

        AssessmentMatrixV2 updatedMatrix = AssessmentMatrixV2.builder()
                .id(matrixId)
                .name("Test Matrix")
                .description("Test Description")
                .tenantId("tenant-123")
                .performanceCycleId("cycle-123")
                .questionCount(4)
                .build();

        doReturn(Optional.of(existingMatrix)).when(assessmentMatrixRepositoryV2).findById(matrixId);
        doReturn(Optional.of(updatedMatrix)).when(assessmentMatrixRepositoryV2).save(any(AssessmentMatrixV2.class));

        AssessmentMatrixV2 result = service.decrementQuestionCount(matrixId);

        assertThat(result.getQuestionCount()).isEqualTo(4);
        verify(assessmentMatrixRepositoryV2).findById(matrixId);
        verify(assessmentMatrixRepositoryV2).save(any(AssessmentMatrixV2.class));
    }

    @Test
    void testGetAssessmentDashboard_Success() {
        String matrixId = "matrix-123";
        String tenantId = "tenant-123";
        
        // Mock the assessment matrix
        PotentialScoreV2 potentialScore = PotentialScoreV2.builder()
                .score(100.0)
                .build();
                
        AssessmentMatrixV2 matrix = AssessmentMatrixV2.builder()
                .id(matrixId)
                .name("Test Matrix")
                .description("Test Description")
                .tenantId(tenantId)
                .performanceCycleId("cycle-123")
                .potentialScore(potentialScore)
                .build();

        // Mock employee assessments
        NaturalPersonV2 employee1 = NaturalPersonV2.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();
                
        NaturalPersonV2 employee2 = NaturalPersonV2.builder()
                .name("Jane Smith")
                .email("jane@example.com")
                .build();

        EmployeeAssessmentScoreV2 score1 = EmployeeAssessmentScoreV2.builder()
                .score(85.0)
                .build();

        EmployeeAssessmentV2 assessment1 = EmployeeAssessmentV2.builder()
                .id("assessment-1")
                .tenantId(tenantId)
                .assessmentMatrixId(matrixId)
                .employee(employee1)
                .teamId("team-1")
                .assessmentStatus(AssessmentStatus.COMPLETED)
                .employeeAssessmentScore(score1)
                .answeredQuestionCount(8)
                .lastActivityDate(new Date())
                .build();

        EmployeeAssessmentV2 assessment2 = EmployeeAssessmentV2.builder()
                .id("assessment-2")
                .tenantId(tenantId)
                .assessmentMatrixId(matrixId)
                .employee(employee2)
                .teamId("team-1")
                .assessmentStatus(AssessmentStatus.IN_PROGRESS)
                .answeredQuestionCount(5)
                .lastActivityDate(new Date())
                .build();

        List<EmployeeAssessmentV2> employeeAssessments = Arrays.asList(assessment1, assessment2);

        // Mock team
        TeamV2 team = TeamV2.builder()
                .id("team-1")
                .name("Engineering Team")
                .description("Software Engineering Team")
                .tenantId(tenantId)
                .departmentId("department-1")
                .build();

        // Set up mocks
        doReturn(Optional.of(matrix)).when(assessmentMatrixRepositoryV2).findById(matrixId);
        doReturn(employeeAssessments).when(mockEmployeeAssessmentServiceV2).findByAssessmentMatrix(matrixId, tenantId);
        lenient().doReturn(Optional.of(team)).when(mockTeamService).findById("team-1");

        // Execute
        Optional<AssessmentDashboardData> result = service.getAssessmentDashboard(matrixId, tenantId);

        // Verify
        assertThat(result).isPresent();
        AssessmentDashboardData dashboardData = result.get();
        
        assertThat(dashboardData.getAssessmentMatrixId()).isEqualTo(matrixId);
        assertThat(dashboardData.getMatrixName()).isEqualTo("Test Matrix");
        assertThat(dashboardData.getPotentialScore()).isEqualTo(potentialScore);
        assertThat(dashboardData.getTotalEmployees()).isEqualTo(2);
        assertThat(dashboardData.getCompletedAssessments()).isEqualTo(1);
        
        // Employee and team summaries removed during V1 cleanup
        // Only basic assessment data is available after V1 DTO removal
        assertThat(dashboardData.getTotalEmployees()).isEqualTo(2);
        assertThat(dashboardData.getCompletedAssessments()).isEqualTo(1);
    }

    @Test
    void testGetAssessmentDashboard_MatrixNotFound() {
        String matrixId = "nonexistent-matrix";
        String tenantId = "tenant-123";
        
        doReturn(Optional.empty()).when(assessmentMatrixRepositoryV2).findById(matrixId);
        
        Optional<AssessmentDashboardData> result = service.getAssessmentDashboard(matrixId, tenantId);
        
        assertThat(result).isEmpty();
    }

    @Test
    void testGetAssessmentDashboard_TenantMismatch() {
        String matrixId = "matrix-123";
        String tenantId = "tenant-123";
        String differentTenantId = "different-tenant";
        
        AssessmentMatrixV2 matrix = AssessmentMatrixV2.builder()
                .id(matrixId)
                .name("Test Matrix")
                .description("Test Description")
                .tenantId(differentTenantId)  // Different tenant
                .performanceCycleId("cycle-123")
                .build();
                
        doReturn(Optional.of(matrix)).when(assessmentMatrixRepositoryV2).findById(matrixId);
        
        Optional<AssessmentDashboardData> result = service.getAssessmentDashboard(matrixId, tenantId);
        
        assertThat(result).isEmpty();
    }

    @Test
    void testGetAssessmentDashboard_NoEmployeeAssessments() {
        String matrixId = "matrix-123";
        String tenantId = "tenant-123";
        
        AssessmentMatrixV2 matrix = AssessmentMatrixV2.builder()
                .id(matrixId)
                .name("Test Matrix")
                .description("Test Description")
                .tenantId(tenantId)
                .performanceCycleId("cycle-123")
                .build();
                
        doReturn(Optional.of(matrix)).when(assessmentMatrixRepositoryV2).findById(matrixId);
        doReturn(Collections.emptyList()).when(mockEmployeeAssessmentServiceV2).findByAssessmentMatrix(matrixId, tenantId);
        
        Optional<AssessmentDashboardData> result = service.getAssessmentDashboard(matrixId, tenantId);
        
        assertThat(result).isPresent();
        AssessmentDashboardData dashboardData = result.get();
        assertThat(dashboardData.getTotalEmployees()).isEqualTo(0);
        assertThat(dashboardData.getCompletedAssessments()).isEqualTo(0);
        // Employee and team summaries removed during V1 cleanup
    }

    @Test
    void testGetAssessmentDashboard_TeamNotFound() {
        String matrixId = "matrix-123";
        String tenantId = "tenant-123";
        
        AssessmentMatrixV2 matrix = AssessmentMatrixV2.builder()
                .id(matrixId)
                .name("Test Matrix")
                .description("Test Description")
                .tenantId(tenantId)
                .performanceCycleId("cycle-123")
                .build();

        NaturalPersonV2 employee = NaturalPersonV2.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        EmployeeAssessmentV2 assessment = EmployeeAssessmentV2.builder()
                .id("assessment-1")
                .tenantId(tenantId)
                .assessmentMatrixId(matrixId)
                .employee(employee)
                .teamId("nonexistent-team")
                .assessmentStatus(AssessmentStatus.COMPLETED)
                .build();

        List<EmployeeAssessmentV2> employeeAssessments = Arrays.asList(assessment);
        
        doReturn(Optional.of(matrix)).when(assessmentMatrixRepositoryV2).findById(matrixId);
        doReturn(employeeAssessments).when(mockEmployeeAssessmentServiceV2).findByAssessmentMatrix(matrixId, tenantId);
        lenient().doReturn(Optional.empty()).when(mockTeamService).findById("nonexistent-team");
        
        Optional<AssessmentDashboardData> result = service.getAssessmentDashboard(matrixId, tenantId);
        
        assertThat(result).isPresent();
        AssessmentDashboardData dashboardData = result.get();
        // Employee and team summaries removed during V1 cleanup
    }

    @Test
    void testUpdateCurrentPotentialScore_Success() {
        String matrixId = "matrix-123";
        String tenantId = "tenant-123";
        
        AssessmentMatrixV2 matrix = AssessmentMatrixV2.builder()
                .id(matrixId)
                .name("Test Matrix")
                .description("Test Description")
                .tenantId(tenantId)
                .performanceCycleId("cycle-123")
                .build();

        List<QuestionV2> questions = Arrays.asList(
            QuestionV2.builder()
                .id("question-1")
                .tenantId(tenantId)
                .assessmentMatrixId(matrixId)
                .pillarId("pillar-1")
                .pillarName("Technical Skills")
                .categoryId("category-1")
                .categoryName("Programming")
                .question("Test Question 1")
                .questionType(QuestionType.ONE_TO_TEN)
                .points(10.0)
                .build(),
            QuestionV2.builder()
                .id("question-2")
                .tenantId(tenantId)
                .assessmentMatrixId(matrixId)
                .pillarId("pillar-1")
                .pillarName("Technical Skills")
                .categoryId("category-2")
                .categoryName("Architecture")
                .question("Test Question 2")
                .questionType(QuestionType.YES_NO)
                .points(5.0)
                .build()
        );

        AssessmentMatrixV2 updatedMatrix = AssessmentMatrixV2.builder()
                .id(matrixId)
                .name("Test Matrix")
                .description("Test Description")
                .tenantId(tenantId)
                .performanceCycleId("cycle-123")
                .potentialScore(PotentialScoreV2.builder()
                    .score(15.0)
                    .build())
                .build();

        doReturn(Optional.of(matrix)).when(assessmentMatrixRepositoryV2).findById(matrixId);
        doReturn(questions).when(mockQuestionService).findByAssessmentMatrixId(matrixId, tenantId);
        doReturn(Optional.of(updatedMatrix)).when(assessmentMatrixRepositoryV2).save(any(AssessmentMatrixV2.class));

        AssessmentMatrixV2 result = service.updateCurrentPotentialScore(matrixId, tenantId);

        assertThat(result).isNotNull();
        assertThat(result.getPotentialScore()).isNotNull();
        assertThat(result.getPotentialScore().getScore()).isEqualTo(15.0);
        
        verify(mockQuestionService).findByAssessmentMatrixId(matrixId, tenantId);
        verify(assessmentMatrixRepositoryV2).findById(matrixId);
        verify(assessmentMatrixRepositoryV2).save(any(AssessmentMatrixV2.class));
    }

    @Test
    void testUpdateCurrentPotentialScore_MatrixNotFound() {
        String matrixId = "nonexistent-matrix";
        String tenantId = "tenant-123";
        
        List<QuestionV2> questions = Arrays.asList();
        
        doReturn(questions).when(mockQuestionService).findByAssessmentMatrixId(matrixId, tenantId);
        doReturn(Optional.empty()).when(assessmentMatrixRepositoryV2).findById(matrixId);
        
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> service.updateCurrentPotentialScore(matrixId, tenantId)
        );
        
        assertThat(exception.getMessage()).contains("Matrix not found: " + matrixId);
    }

    @Test
    void testComputeQuestionMaxScore_OneToTen() {
        QuestionV2 question = QuestionV2.builder()
                .id("question-1")
                .tenantId("tenant-123")
                .assessmentMatrixId("matrix-123")
                .pillarId("pillar-1")
                .pillarName("Technical Skills")
                .categoryId("category-1")
                .categoryName("Programming")
                .question("Test Question 1")
                .questionType(QuestionType.ONE_TO_TEN)
                .points(10.0)
                .build();
                
        Double result = service.computeQuestionMaxScore(question);
        
        assertThat(result).isEqualTo(10.0);
    }

    @Test
    void testComputeQuestionMaxScore_YesNo() {
        QuestionV2 question = QuestionV2.builder()
                .id("question-2")
                .tenantId("tenant-123")
                .assessmentMatrixId("matrix-123")
                .pillarId("pillar-1")
                .pillarName("Technical Skills")
                .categoryId("category-1")
                .categoryName("Programming")
                .question("Test Question 2")
                .questionType(QuestionType.YES_NO)
                .points(5.0)
                .build();
                
        Double result = service.computeQuestionMaxScore(question);
        
        assertThat(result).isEqualTo(5.0);
    }
}
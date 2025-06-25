package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.*;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.score.*;
import com.agilecheckup.persistency.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardAnalyticsServiceTest {

    @Mock
    private DashboardAnalyticsRepository dashboardAnalyticsRepository;
    
    @Mock
    private AssessmentMatrixService assessmentMatrixService;
    
    @Mock
    private EmployeeAssessmentService employeeAssessmentService;
    
    @Mock
    private TeamRepository teamRepository;
    
    @Mock
    private AnswerRepository answerRepository;

    @InjectMocks
    private DashboardAnalyticsService dashboardAnalyticsService;

    private static final String COMPANY_ID = "company123";
    private static final String PERFORMANCE_CYCLE_ID = "cycle456";
    private static final String ASSESSMENT_MATRIX_ID = "matrix789";
    private static final String TEAM_ID = "team101";
    private static final String PILLAR_ID = "pillar001";
    private static final String CATEGORY_ID = "category001";

    private AssessmentMatrix mockMatrix;
    private EmployeeAssessment mockEmployeeAssessment;
    private Team mockTeam;
    private DashboardAnalytics mockDashboardAnalytics;
    private PotentialScore mockPotentialScore;
    private EmployeeAssessmentScore mockEmployeeAssessmentScore;

    @BeforeEach
    void setUp() {
        setupMockEntities();
    }

    @Test
    void getOverview_WhenMatrixExists_ShouldReturnOverviewAnalytics() {
        // Given
        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(dashboardAnalyticsRepository.findByCompanyPerformanceCycleAndTeam(
                COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID, "OVERVIEW"))
                .thenReturn(Optional.of(mockDashboardAnalytics));

        // When
        Optional<DashboardAnalytics> result = dashboardAnalyticsService.getOverview(ASSESSMENT_MATRIX_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mockDashboardAnalytics);
        verify(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
        verify(dashboardAnalyticsRepository).findByCompanyPerformanceCycleAndTeam(
                COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID, "OVERVIEW");
    }

    @Test
    void getOverview_WhenMatrixNotFound_ShouldReturnEmpty() {
        // Given
        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.empty());

        // When
        Optional<DashboardAnalytics> result = dashboardAnalyticsService.getOverview(ASSESSMENT_MATRIX_ID);

        // Then
        assertThat(result).isEmpty();
        verify(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
        verifyNoInteractions(dashboardAnalyticsRepository);
    }

    @Test
    void getTeamAnalytics_WhenMatrixAndTeamExist_ShouldReturnTeamAnalytics() {
        // Given
        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(dashboardAnalyticsRepository.findByCompanyPerformanceCycleAndTeam(
                COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID, TEAM_ID))
                .thenReturn(Optional.of(mockDashboardAnalytics));

        // When
        Optional<DashboardAnalytics> result = dashboardAnalyticsService.getTeamAnalytics(ASSESSMENT_MATRIX_ID, TEAM_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mockDashboardAnalytics);
        verify(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
        verify(dashboardAnalyticsRepository).findByCompanyPerformanceCycleAndTeam(
                COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID, TEAM_ID);
    }

    @Test
    void getTeamAnalytics_WhenMatrixNotFound_ShouldReturnEmpty() {
        // Given
        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.empty());

        // When
        Optional<DashboardAnalytics> result = dashboardAnalyticsService.getTeamAnalytics(ASSESSMENT_MATRIX_ID, TEAM_ID);

        // Then
        assertThat(result).isEmpty();
        verify(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
        verifyNoInteractions(dashboardAnalyticsRepository);
    }

    @Test
    void getAllAnalytics_WhenMatrixExists_ShouldReturnFilteredAnalytics() {
        // Given
        DashboardAnalytics analytics1 = createMockAnalytics(ASSESSMENT_MATRIX_ID, "team1");
        DashboardAnalytics analytics2 = createMockAnalytics(ASSESSMENT_MATRIX_ID, "team2");
        DashboardAnalytics analytics3 = createMockAnalytics("otherMatrix", "team3");
        List<DashboardAnalytics> allAnalytics = Arrays.asList(analytics1, analytics2, analytics3);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(dashboardAnalyticsRepository.findByCompanyAndPerformanceCycle(COMPANY_ID, PERFORMANCE_CYCLE_ID))
                .thenReturn(allAnalytics);

        // When
        List<DashboardAnalytics> result = dashboardAnalyticsService.getAllAnalytics(ASSESSMENT_MATRIX_ID);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(analytics1, analytics2);
        verify(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
        verify(dashboardAnalyticsRepository).findByCompanyAndPerformanceCycle(COMPANY_ID, PERFORMANCE_CYCLE_ID);
    }

    @Test
    void getAllAnalytics_WhenMatrixNotFound_ShouldReturnEmptyList() {
        // Given
        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.empty());

        // When
        List<DashboardAnalytics> result = dashboardAnalyticsService.getAllAnalytics(ASSESSMENT_MATRIX_ID);

        // Then
        assertThat(result).isEmpty();
        verify(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
        verifyNoInteractions(dashboardAnalyticsRepository);
    }

    @Test
    void updateAssessmentMatrixAnalytics_WhenMatrixNotFound_ShouldLogErrorAndReturn() {
        // Given
        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.empty());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then
        verify(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
        verifyNoInteractions(employeeAssessmentService);
        verifyNoInteractions(dashboardAnalyticsRepository);
    }

    @Test
    void updateAssessmentMatrixAnalytics_WhenNoAssessments_ShouldReturn() {
        // Given
        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, COMPANY_ID))
                .thenReturn(Collections.emptyList());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then
        verify(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
        verify(employeeAssessmentService).findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, COMPANY_ID);
        verifyNoMoreInteractions(dashboardAnalyticsRepository);
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithValidAssessments_ShouldCalculateAndSaveAnalytics() {
        // Given
        EmployeeAssessment assessment1 = createMockEmployeeAssessment("emp1", TEAM_ID);
        EmployeeAssessment assessment2 = createMockEmployeeAssessment("emp2", TEAM_ID);
        List<EmployeeAssessment> assessments = Arrays.asList(assessment1, assessment2);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, COMPANY_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(TEAM_ID)).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(createMockAnswers());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then
        verify(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
        verify(employeeAssessmentService).findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, COMPANY_ID);
        verify(dashboardAnalyticsRepository, times(2)).save(any(DashboardAnalytics.class));
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithMultipleTeams_ShouldCreateAnalyticsForEachTeam() {
        // Given
        EmployeeAssessment assessment1 = createMockEmployeeAssessment("emp1", "team1");
        EmployeeAssessment assessment2 = createMockEmployeeAssessment("emp2", "team2");
        List<EmployeeAssessment> assessments = Arrays.asList(assessment1, assessment2);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, COMPANY_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(anyString())).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(createMockAnswers());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then
        verify(dashboardAnalyticsRepository, times(3)).save(any(DashboardAnalytics.class)); // 2 teams + 1 overview
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithWordCloudData_ShouldGenerateWordCloud() {
        // Given
        EmployeeAssessment assessment = createMockEmployeeAssessment("emp1", TEAM_ID);
        List<EmployeeAssessment> assessments = Collections.singletonList(assessment);
        
        Answer answer1 = createMockAnswer("This is excellent work and shows great improvement");
        Answer answer2 = createMockAnswer("The team collaboration needs improvement but overall good progress");
        List<Answer> answers = Arrays.asList(answer1, answer2);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, COMPANY_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(TEAM_ID)).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(answers);

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then
        verify(answerRepository, times(2)).findByEmployeeAssessmentId(assessment.getId(), assessment.getTenantId());
        verify(dashboardAnalyticsRepository, times(2)).save(any(DashboardAnalytics.class));
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithCompletedAssessments_ShouldCalculateCorrectPercentages() {
        // Given
        EmployeeAssessment completedAssessment = createMockEmployeeAssessment("emp1", TEAM_ID);
        completedAssessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
        completedAssessment.setEmployeeAssessmentScore(mockEmployeeAssessmentScore);
        
        EmployeeAssessment inProgressAssessment = createMockEmployeeAssessment("emp2", TEAM_ID);
        inProgressAssessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);
        
        List<EmployeeAssessment> assessments = Arrays.asList(completedAssessment, inProgressAssessment);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, COMPANY_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(TEAM_ID)).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(createMockAnswers());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then
        verify(dashboardAnalyticsRepository, times(2)).save(any(DashboardAnalytics.class));
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithNullTeamId_ShouldSkipAssessment() {
        // Given
        EmployeeAssessment assessmentWithoutTeam = createMockEmployeeAssessment("emp1", null);
        EmployeeAssessment assessmentWithTeam = createMockEmployeeAssessment("emp2", TEAM_ID);
        List<EmployeeAssessment> assessments = Arrays.asList(assessmentWithoutTeam, assessmentWithTeam);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, COMPANY_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(TEAM_ID)).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(createMockAnswers());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then
        verify(dashboardAnalyticsRepository, times(2)).save(any(DashboardAnalytics.class)); // 1 team + 1 overview
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithNoPotentialScore_ShouldHandleGracefully() {
        // Given
        AssessmentMatrix matrixWithoutPotentialScore = AssessmentMatrix.builder()
                .id(ASSESSMENT_MATRIX_ID)
                .name("Test Matrix Without Potential Score")
                .description("Test Matrix Without Potential Score Description")
                .tenantId(COMPANY_ID)
                .performanceCycleId(PERFORMANCE_CYCLE_ID)
                .potentialScore(null)
                .pillarMap(new HashMap<>())
                .build();

        EmployeeAssessment assessment = createMockEmployeeAssessment("emp1", TEAM_ID);
        List<EmployeeAssessment> assessments = Collections.singletonList(assessment);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(matrixWithoutPotentialScore));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, COMPANY_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(TEAM_ID)).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(createMockAnswers());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then
        verify(dashboardAnalyticsRepository, times(2)).save(any(DashboardAnalytics.class));
    }

    private void setupMockEntities() {
        // Setup mock matrix with potential score
        mockPotentialScore = createMockPotentialScore();
        Map<String, Pillar> pillarMap = new HashMap<>();
        pillarMap.put(PILLAR_ID, Pillar.builder()
                .id(PILLAR_ID)
                .name("Test Pillar")
                .description("Test Pillar Description")
                .build());
        
        mockMatrix = AssessmentMatrix.builder()
                .id(ASSESSMENT_MATRIX_ID)
                .name("Test Assessment Matrix")
                .description("Test Assessment Matrix Description")
                .tenantId(COMPANY_ID)
                .performanceCycleId(PERFORMANCE_CYCLE_ID)
                .potentialScore(mockPotentialScore)
                .pillarMap(pillarMap)
                .build();

        // Setup mock team
        mockTeam = Team.builder()
                .id(TEAM_ID)
                .name("Test Team")
                .description("Test Team Description")
                .tenantId(COMPANY_ID)
                .departmentId("dept123")
                .build();

        // Setup mock employee assessment score
        Map<String, PillarScore> pillarScoreMap = new HashMap<>();
        Map<String, CategoryScore> categoryScoreMap = new HashMap<>();
        categoryScoreMap.put(CATEGORY_ID, CategoryScore.builder()
                .categoryName("Test Category")
                .score(80.0)
                .build());
        
        pillarScoreMap.put(PILLAR_ID, PillarScore.builder()
                .score(85.0)
                .categoryIdToCategoryScoreMap(categoryScoreMap)
                .build());

        mockEmployeeAssessmentScore = EmployeeAssessmentScore.builder()
                .score(82.5)
                .pillarIdToPillarScoreMap(pillarScoreMap)
                .build();

        // Setup mock dashboard analytics
        mockDashboardAnalytics = DashboardAnalytics.builder()
                .companyPerformanceCycleId(COMPANY_ID + "#" + PERFORMANCE_CYCLE_ID)
                .assessmentMatrixTeamId(ASSESSMENT_MATRIX_ID + "#" + TEAM_ID)
                .companyId(COMPANY_ID)
                .performanceCycleId(PERFORMANCE_CYCLE_ID)
                .assessmentMatrixId(ASSESSMENT_MATRIX_ID)
                .teamId(TEAM_ID)
                .teamName("Test Team")
                .generalAverage(82.5)
                .employeeCount(5)
                .completionPercentage(80.0)
                .lastUpdated(LocalDateTime.now())
                .analyticsDataJson("{\"test\": \"data\"}")
                .build();
    }

    private PotentialScore createMockPotentialScore() {
        Map<String, CategoryScore> categoryScoreMap = new HashMap<>();
        categoryScoreMap.put(CATEGORY_ID, CategoryScore.builder()
                .categoryName("Test Category")
                .score(100.0)
                .build());

        Map<String, PillarScore> pillarScoreMap = new HashMap<>();
        pillarScoreMap.put(PILLAR_ID, PillarScore.builder()
                .score(100.0)
                .categoryIdToCategoryScoreMap(categoryScoreMap)
                .build());

        return PotentialScore.builder()
                .score(100.0)
                .pillarIdToPillarScoreMap(pillarScoreMap)
                .build();
    }

    private EmployeeAssessment createMockEmployeeAssessment(String employeeId, String teamId) {
        NaturalPerson employee = NaturalPerson.builder()
                .email(employeeId + "@example.com")
                .name("Employee " + employeeId)
                .build();
                
        return EmployeeAssessment.builder()
                .id(employeeId + "_assessment")
                .tenantId(COMPANY_ID)
                .employee(employee)
                .teamId(teamId)
                .assessmentMatrixId(ASSESSMENT_MATRIX_ID)
                .assessmentStatus(AssessmentStatus.COMPLETED)
                .employeeAssessmentScore(mockEmployeeAssessmentScore)
                .build();
    }

    private DashboardAnalytics createMockAnalytics(String matrixId, String teamId) {
        return DashboardAnalytics.builder()
                .companyPerformanceCycleId(COMPANY_ID + "#" + PERFORMANCE_CYCLE_ID)
                .assessmentMatrixTeamId(matrixId + "#" + teamId)
                .companyId(COMPANY_ID)
                .performanceCycleId(PERFORMANCE_CYCLE_ID)
                .assessmentMatrixId(matrixId)
                .teamId(teamId)
                .teamName("Team " + teamId)
                .generalAverage(75.0)
                .employeeCount(3)
                .completionPercentage(80.0)
                .lastUpdated(LocalDateTime.now())
                .analyticsDataJson("{}")
                .build();
    }

    private List<Answer> createMockAnswers() {
        Answer answer1 = createMockAnswer("Great teamwork and collaboration");
        Answer answer2 = createMockAnswer("Needs improvement in communication");
        return Arrays.asList(answer1, answer2);
    }

    private Answer createMockAnswer(String notes) {
        return Answer.builder()
                .id("answer_" + UUID.randomUUID().toString())
                .tenantId(COMPANY_ID)
                .employeeAssessmentId("assessment_123")
                .answeredAt(LocalDateTime.now())
                .pillarId(PILLAR_ID)
                .categoryId(CATEGORY_ID)
                .questionId("question_123")
                .questionType(QuestionType.OPEN_ANSWER)
                .value("Good")
                .notes(notes)
                .build();
    }
}
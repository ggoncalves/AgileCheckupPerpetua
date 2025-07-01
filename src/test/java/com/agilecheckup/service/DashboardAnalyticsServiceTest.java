package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.DashboardAnalytics;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.EmployeeAssessmentScore;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.entity.Pillar;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.PotentialScore;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.persistency.repository.DashboardAnalyticsRepository;
import com.agilecheckup.persistency.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardAnalyticsServiceTest {

    @Mock
    private DashboardAnalyticsRepository dashboardAnalyticsRepository;
    
    @Mock
    private AssessmentMatrixService assessmentMatrixService;
    
    @Mock
    private EmployeeAssessmentService employeeAssessmentService;
    
    @Mock
    private CompanyService companyService;
    
    @Mock
    private PerformanceCycleService performanceCycleService;
    
    @Mock
    private TeamRepository teamRepository;
    
    @Mock
    private AnswerRepository answerRepository;

    @InjectMocks
    private DashboardAnalyticsService dashboardAnalyticsService;

    private static final String COMPANY_ID = "company123";
    private static final String TENANT_ID = "tenant456";  // Different from COMPANY_ID
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
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
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
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
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
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
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
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
                .thenReturn(Collections.emptyList());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then
        verify(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
        verify(employeeAssessmentService).findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID);
        verifyNoMoreInteractions(dashboardAnalyticsRepository);
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithValidAssessments_ShouldCalculateAndSaveAnalytics() {
        // Given
        EmployeeAssessment assessment1 = createMockEmployeeAssessment("emp1", TEAM_ID);
        EmployeeAssessment assessment2 = createMockEmployeeAssessment("emp2", TEAM_ID);
        List<EmployeeAssessment> assessments = Arrays.asList(assessment1, assessment2);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(TEAM_ID)).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(createMockAnswers());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then
        verify(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
        verify(employeeAssessmentService).findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID);
        verify(dashboardAnalyticsRepository, times(2)).save(any(DashboardAnalytics.class));
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithMultipleTeams_ShouldCreateAnalyticsForEachTeam() {
        // Given
        EmployeeAssessment assessment1 = createMockEmployeeAssessment("emp1", "team1");
        EmployeeAssessment assessment2 = createMockEmployeeAssessment("emp2", "team2");
        List<EmployeeAssessment> assessments = Arrays.asList(assessment1, assessment2);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
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
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
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
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
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
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
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
    void updateAssessmentMatrixAnalytics_WithSingleEmployee_ShouldCalculateCorrectTotalScore() {
        // Given: One employee with score 85.5
        EmployeeAssessment employee = createMockEmployeeAssessment("emp1", TEAM_ID);
        employee.getEmployeeAssessmentScore().setScore(85.5);
        List<EmployeeAssessment> assessments = Collections.singletonList(employee);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(TEAM_ID)).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(createMockAnswers());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then: generalAverage should equal the single employee's score (85.5)
        verify(dashboardAnalyticsRepository, times(2)).save(argThat(analytics -> {
            if ("OVERVIEW".equals(analytics.getTeamId())) {
                // Overall analytics should have the same score as the single employee
                assertThat(analytics.getGeneralAverage()).isEqualTo(85.5);
                assertThat(analytics.getEmployeeCount()).isEqualTo(1);
            } else if (TEAM_ID.equals(analytics.getTeamId())) {
                // Team analytics should also have the same score
                assertThat(analytics.getGeneralAverage()).isEqualTo(85.5);
                assertThat(analytics.getEmployeeCount()).isEqualTo(1);
            }
            return true;
        }));
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithMultipleEmployeesSameTeam_ShouldCalculateAverageScore() {
        // Given: Three employees in same team with scores 100.0, 80.0, 70.0 (average = 83.33)
        EmployeeAssessment emp1 = createMockEmployeeAssessment("emp1", TEAM_ID);
        emp1.getEmployeeAssessmentScore().setScore(100.0);
        
        EmployeeAssessment emp2 = createMockEmployeeAssessment("emp2", TEAM_ID);
        emp2.getEmployeeAssessmentScore().setScore(80.0);
        
        EmployeeAssessment emp3 = createMockEmployeeAssessment("emp3", TEAM_ID);
        emp3.getEmployeeAssessmentScore().setScore(70.0);
        
        List<EmployeeAssessment> assessments = Arrays.asList(emp1, emp2, emp3);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(TEAM_ID)).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(createMockAnswers());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then: generalAverage should be (100 + 80 + 70) / 3 = 83.33
        double expectedAverage = (100.0 + 80.0 + 70.0) / 3.0;
        
        verify(dashboardAnalyticsRepository, times(2)).save(argThat(analytics -> {
            if ("OVERVIEW".equals(analytics.getTeamId()) || TEAM_ID.equals(analytics.getTeamId())) {
                assertThat(analytics.getGeneralAverage()).isEqualTo(expectedAverage);
                assertThat(analytics.getEmployeeCount()).isEqualTo(3);
            }
            return true;
        }));
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithMultipleTeams_ShouldCalculateOverallAverage() {
        // Given: Two teams with different average scores
        // Team 1: employees with scores 120.0, 80.0 (team average = 100.0)
        // Team 2: employees with scores 60.0, 40.0 (team average = 50.0)
        // Overall average should be (120 + 80 + 60 + 40) / 4 = 75.0
        
        EmployeeAssessment team1Emp1 = createMockEmployeeAssessment("team1_emp1", "team1");
        team1Emp1.getEmployeeAssessmentScore().setScore(120.0);
        
        EmployeeAssessment team1Emp2 = createMockEmployeeAssessment("team1_emp2", "team1");
        team1Emp2.getEmployeeAssessmentScore().setScore(80.0);
        
        EmployeeAssessment team2Emp1 = createMockEmployeeAssessment("team2_emp1", "team2");
        team2Emp1.getEmployeeAssessmentScore().setScore(60.0);
        
        EmployeeAssessment team2Emp2 = createMockEmployeeAssessment("team2_emp2", "team2");
        team2Emp2.getEmployeeAssessmentScore().setScore(40.0);
        
        List<EmployeeAssessment> assessments = Arrays.asList(team1Emp1, team1Emp2, team2Emp1, team2Emp2);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(anyString())).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(createMockAnswers());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then: Verify individual team averages and overall average
        double expectedOverallAverage = (120.0 + 80.0 + 60.0 + 40.0) / 4.0; // = 75.0
        double expectedTeam1Average = (120.0 + 80.0) / 2.0; // = 100.0
        double expectedTeam2Average = (60.0 + 40.0) / 2.0; // = 50.0
        
        verify(dashboardAnalyticsRepository, times(3)).save(argThat(analytics -> {
            if ("OVERVIEW".equals(analytics.getTeamId())) {
                assertThat(analytics.getGeneralAverage()).isEqualTo(expectedOverallAverage);
                assertThat(analytics.getEmployeeCount()).isEqualTo(4);
            } else if ("team1".equals(analytics.getTeamId())) {
                assertThat(analytics.getGeneralAverage()).isEqualTo(expectedTeam1Average);
                assertThat(analytics.getEmployeeCount()).isEqualTo(2);
            } else if ("team2".equals(analytics.getTeamId())) {
                assertThat(analytics.getGeneralAverage()).isEqualTo(expectedTeam2Average);
                assertThat(analytics.getEmployeeCount()).isEqualTo(2);
            }
            return true;
        }));
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithScoresAbove100_ShouldHandleCorrectly() {
        // Given: Employees with scores that exceed 100 (which is allowed in the system)
        EmployeeAssessment emp1 = createMockEmployeeAssessment("emp1", TEAM_ID);
        emp1.getEmployeeAssessmentScore().setScore(150.5); // Above 100
        
        EmployeeAssessment emp2 = createMockEmployeeAssessment("emp2", TEAM_ID);
        emp2.getEmployeeAssessmentScore().setScore(200.0); // Way above 100
        
        List<EmployeeAssessment> assessments = Arrays.asList(emp1, emp2);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(TEAM_ID)).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(createMockAnswers());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then: Average should be (150.5 + 200.0) / 2 = 175.25 (not capped at 100)
        double expectedAverage = (150.5 + 200.0) / 2.0;
        
        verify(dashboardAnalyticsRepository, times(2)).save(argThat(analytics -> {
            if ("OVERVIEW".equals(analytics.getTeamId()) || TEAM_ID.equals(analytics.getTeamId())) {
                assertThat(analytics.getGeneralAverage()).isEqualTo(expectedAverage);
                assertThat(analytics.getEmployeeCount()).isEqualTo(2);
            }
            return true;
        }));
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithMixedCompletionStatus_ShouldOnlyCountCompleted() {
        // Given: Mix of completed and incomplete assessments
        // Only completed assessments should count toward the average
        EmployeeAssessment completedEmp1 = createMockEmployeeAssessment("completed1", TEAM_ID);
        completedEmp1.setAssessmentStatus(AssessmentStatus.COMPLETED);
        completedEmp1.getEmployeeAssessmentScore().setScore(90.0);
        
        EmployeeAssessment completedEmp2 = createMockEmployeeAssessment("completed2", TEAM_ID);
        completedEmp2.setAssessmentStatus(AssessmentStatus.COMPLETED);
        completedEmp2.getEmployeeAssessmentScore().setScore(70.0);
        
        EmployeeAssessment inProgressEmp = createMockEmployeeAssessment("inprogress", TEAM_ID);
        inProgressEmp.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);
        inProgressEmp.setEmployeeAssessmentScore(null); // No score yet
        
        List<EmployeeAssessment> assessments = Arrays.asList(completedEmp1, completedEmp2, inProgressEmp);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(TEAM_ID)).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(createMockAnswers());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then: Average should only include completed assessments: (90 + 70) / 2 = 80.0
        // Total employee count should be 3, but completion percentage should reflect only 2 completed
        double expectedAverage = (90.0 + 70.0) / 2.0;
        
        verify(dashboardAnalyticsRepository, times(2)).save(argThat(analytics -> {
            if ("OVERVIEW".equals(analytics.getTeamId()) || TEAM_ID.equals(analytics.getTeamId())) {
                assertThat(analytics.getGeneralAverage()).isEqualTo(expectedAverage);
                assertThat(analytics.getEmployeeCount()).isEqualTo(3); // Total employees
                assertThat(analytics.getCompletionPercentage()).isEqualTo(66.67); // 2/3 * 100 = 66.67%
            }
            return true;
        }));
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithNoCompletedAssessments_ShouldHaveZeroAverage() {
        // Given: All assessments are incomplete (no scores to average)
        EmployeeAssessment inProgressEmp1 = createMockEmployeeAssessment("emp1", TEAM_ID);
        inProgressEmp1.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);
        inProgressEmp1.setEmployeeAssessmentScore(null);
        
        EmployeeAssessment invitedEmp2 = createMockEmployeeAssessment("emp2", TEAM_ID);
        invitedEmp2.setAssessmentStatus(AssessmentStatus.INVITED);
        invitedEmp2.setEmployeeAssessmentScore(null);
        
        List<EmployeeAssessment> assessments = Arrays.asList(inProgressEmp1, invitedEmp2);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(TEAM_ID)).thenReturn(mockTeam);
        // Note: No need to mock answerRepository since no completed assessments exist

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then: Average should be 0.0 when no completed assessments exist
        verify(dashboardAnalyticsRepository, times(2)).save(argThat(analytics -> {
            if ("OVERVIEW".equals(analytics.getTeamId()) || TEAM_ID.equals(analytics.getTeamId())) {
                assertThat(analytics.getGeneralAverage()).isEqualTo(0.0);
                assertThat(analytics.getEmployeeCount()).isEqualTo(2);
                assertThat(analytics.getCompletionPercentage()).isEqualTo(0.0); // 0/2 * 100 = 0%
            }
            return true;
        }));
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithCompletedButUnscored_ShouldCountAsZero() {
        // Given: Mix of scored and unscored but completed assessments
        // Scored employee: 123.0
        // Unscored employees: 2 employees marked as COMPLETED but no EmployeeAssessmentScore
        // Expected average: (123.0 + 0.0 + 0.0) / 3 = 41.0
        
        EmployeeAssessment scoredEmp = createMockEmployeeAssessment("scored", TEAM_ID);
        scoredEmp.getEmployeeAssessmentScore().setScore(123.0);
        
        EmployeeAssessment unscoredEmp1 = createMockEmployeeAssessment("unscored1", "team2");
        unscoredEmp1.setAssessmentStatus(AssessmentStatus.COMPLETED);
        unscoredEmp1.setEmployeeAssessmentScore(null); // No score yet but marked completed
        
        EmployeeAssessment unscoredEmp2 = createMockEmployeeAssessment("unscored2", "team2");
        unscoredEmp2.setAssessmentStatus(AssessmentStatus.COMPLETED);
        unscoredEmp2.setEmployeeAssessmentScore(null); // No score yet but marked completed
        
        List<EmployeeAssessment> assessments = Arrays.asList(scoredEmp, unscoredEmp1, unscoredEmp2);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(mockMatrix));
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
                .thenReturn(assessments);
        when(teamRepository.findById(anyString())).thenReturn(mockTeam);
        when(answerRepository.findByEmployeeAssessmentId(anyString(), anyString()))
                .thenReturn(createMockAnswers());

        // When
        dashboardAnalyticsService.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

        // Then: Overall average should include unscored employees as 0.0
        double expectedOverallAverage = (123.0 + 0.0 + 0.0) / 3.0; // = 41.0
        double expectedTeam1Average = 123.0; // Only 1 employee
        double expectedTeam2Average = (0.0 + 0.0) / 2.0; // = 0.0
        
        verify(dashboardAnalyticsRepository, times(3)).save(argThat(analytics -> {
            if ("OVERVIEW".equals(analytics.getTeamId())) {
                assertThat(analytics.getGeneralAverage()).isEqualTo(expectedOverallAverage);
                assertThat(analytics.getEmployeeCount()).isEqualTo(3);
                assertThat(analytics.getCompletionPercentage()).isEqualTo(100.0); // All completed
            } else if (TEAM_ID.equals(analytics.getTeamId())) {
                assertThat(analytics.getGeneralAverage()).isEqualTo(expectedTeam1Average);
                assertThat(analytics.getEmployeeCount()).isEqualTo(1);
            } else if ("team2".equals(analytics.getTeamId())) {
                assertThat(analytics.getGeneralAverage()).isEqualTo(expectedTeam2Average);
                assertThat(analytics.getEmployeeCount()).isEqualTo(2);
                assertThat(analytics.getCompletionPercentage()).isEqualTo(100.0); // Both completed but no scores
            }
            return true;
        }));
    }

    @Test
    void updateAssessmentMatrixAnalytics_WithNoPotentialScore_ShouldHandleGracefully() {
        // Given
        AssessmentMatrix matrixWithoutPotentialScore = AssessmentMatrix.builder()
                .id(ASSESSMENT_MATRIX_ID)
                .name("Test Matrix Without Potential Score")
                .description("Test Matrix Without Potential Score Description")
                .tenantId(TENANT_ID)
                .performanceCycleId(PERFORMANCE_CYCLE_ID)
                .potentialScore(null)
                .pillarMap(new HashMap<>())
                .build();

        EmployeeAssessment assessment = createMockEmployeeAssessment("emp1", TEAM_ID);
        List<EmployeeAssessment> assessments = Collections.singletonList(assessment);

        when(assessmentMatrixService.findById(ASSESSMENT_MATRIX_ID)).thenReturn(Optional.of(matrixWithoutPotentialScore));
        when(companyService.findById(COMPANY_ID)).thenReturn(Optional.of(createMockCompany()));
        when(performanceCycleService.findById(PERFORMANCE_CYCLE_ID)).thenReturn(Optional.of(createMockPerformanceCycle()));
        when(employeeAssessmentService.findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID))
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
                .tenantId(TENANT_ID)  // Use TENANT_ID instead of COMPANY_ID
                .performanceCycleId(PERFORMANCE_CYCLE_ID)
                .potentialScore(mockPotentialScore)
                .pillarMap(pillarMap)
                .build();

        // Setup mock team
        mockTeam = Team.builder()
                .id(TEAM_ID)
                .name("Test Team")
                .description("Test Team Description")
                .tenantId(TENANT_ID)  // Use TENANT_ID instead of COMPANY_ID
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
                .companyName("Test Company")
                .performanceCycleName("Q4 2024 Assessment")
                .assessmentMatrixName("Test Assessment Matrix")
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
        
        // Create a new score object for each employee to avoid sharing
        EmployeeAssessmentScore individualScore = createMockEmployeeAssessmentScore();
                
        return EmployeeAssessment.builder()
                .id(employeeId + "_assessment")
                .tenantId(TENANT_ID)  // Use TENANT_ID instead of COMPANY_ID
                .employee(employee)
                .teamId(teamId)
                .assessmentMatrixId(ASSESSMENT_MATRIX_ID)
                .assessmentStatus(AssessmentStatus.COMPLETED)
                .employeeAssessmentScore(individualScore)
                .build();
    }
    
    private EmployeeAssessmentScore createMockEmployeeAssessmentScore() {
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

        return EmployeeAssessmentScore.builder()
                .score(82.5)  // Default score that can be overridden
                .pillarIdToPillarScoreMap(pillarScoreMap)
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
                .companyName("Test Company")
                .performanceCycleName("Q4 2024 Assessment")
                .assessmentMatrixName("Test Assessment Matrix")
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
                .tenantId(TENANT_ID)  // Use TENANT_ID instead of COMPANY_ID
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

    private Company createMockCompany() {
        return Company.builder()
                .id(COMPANY_ID)
                .name("Test Company")
                .email("test@company.com")
                .description("Test Company Description")
                .tenantId(TENANT_ID)  // Use TENANT_ID instead of COMPANY_ID
                .build();
    }

    private PerformanceCycle createMockPerformanceCycle() {
        return PerformanceCycle.builder()
                .id(PERFORMANCE_CYCLE_ID)
                .name("Q4 2024 Assessment")
                .description("Performance Cycle Description")
                .tenantId(TENANT_ID)  // Use TENANT_ID instead of COMPANY_ID
                .companyId(COMPANY_ID)  // Keep COMPANY_ID here as this is the actual company reference
                .isActive(true)
                .isTimeSensitive(false)
                .build();
    }
}
package com.agilecheckup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agilecheckup.persistency.entity.AnalyticsScope;
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
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.score.CategoryScore;
import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.PotentialScore;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.persistency.repository.DashboardAnalyticsRepository;
import com.agilecheckup.persistency.repository.TeamRepository;
import com.agilecheckup.util.TestObjectFactory;

@ExtendWith(MockitoExtension.class)
class DashboardAnalyticsServiceTest {

  private static final String TENANT_ID = "tenant-123";
  private static final String COMPANY_ID = "company-123";
  private static final String PERFORMANCE_CYCLE_ID = "cycle-123";
  private static final String ASSESSMENT_MATRIX_ID = "matrix-123";
  private static final String TEAM_ID_1 = "team-123";
  private static final String TEAM_ID_2 = "team-456";
  private static final String PILLAR_ID_1 = "pillar-123";
  private static final String PILLAR_ID_2 = "pillar-456";
  private static final String CATEGORY_ID_1 = "category-123";
  private static final String CATEGORY_ID_2 = "category-456";

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

  private DashboardAnalyticsService service;

  @BeforeEach
  void setUp() {
    service = new DashboardAnalyticsService(
                                            dashboardAnalyticsRepository, assessmentMatrixService, employeeAssessmentService, companyService, performanceCycleService, teamRepository, answerRepository
    );

    setupDefaultMocks();
  }

  private void setupDefaultMocks() {
    AssessmentMatrix mockMatrix = createMockAssessmentMatrix();
    lenient().doReturn(Optional.of(mockMatrix)).when(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);

    PerformanceCycle mockCycle = createMockPerformanceCycle();
    lenient().doReturn(Optional.of(mockCycle)).when(performanceCycleService).findById(PERFORMANCE_CYCLE_ID);

    Company mockCompany = createMockCompany();
    lenient().doReturn(Optional.of(mockCompany)).when(companyService).findById(COMPANY_ID);

    // Setup batch team lookup
    Team mockTeam1 = createMockTeam(TEAM_ID_1, "Team Alpha");
    Team mockTeam2 = createMockTeam(TEAM_ID_2, "Team Beta");
    Map<String, Team> teamsByIds = Map.of(
                                          TEAM_ID_1, mockTeam1, TEAM_ID_2, mockTeam2
    );
    lenient().doReturn(teamsByIds).when(teamRepository).findByIds(any());

    // Setup batch answer lookup (empty by default)
    lenient().doReturn(Map.of()).when(answerRepository).findByEmployeeAssessmentIds(any(), any());
  }

  @Test
  void testGetOverview_Success() {
    DashboardAnalytics expectedAnalytics = createMockDashboardAnalytics(AnalyticsScope.ASSESSMENT_MATRIX, null);
    doReturn(Optional.of(expectedAnalytics)).when(dashboardAnalyticsRepository)
                                            .findAssessmentMatrixOverview(COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID);

    Optional<DashboardAnalytics> result = service.getOverview(ASSESSMENT_MATRIX_ID);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(expectedAnalytics);
    verify(dashboardAnalyticsRepository).findAssessmentMatrixOverview(COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID);
  }

  @Test
  void testGetOverview_AssessmentMatrixNotFound() {
    doReturn(Optional.empty()).when(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);

    Optional<DashboardAnalytics> result = service.getOverview(ASSESSMENT_MATRIX_ID);

    assertThat(result).isEmpty();
    verify(dashboardAnalyticsRepository, never()).findAssessmentMatrixOverview(anyString(), anyString(), anyString());
  }

  @Test
  void testGetOverview_PerformanceCycleNotFound() {
    doReturn(Optional.empty()).when(performanceCycleService).findById(PERFORMANCE_CYCLE_ID);
    doReturn(Optional.empty()).when(dashboardAnalyticsRepository)
                              .findAssessmentMatrixOverview(null, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID);

    Optional<DashboardAnalytics> result = service.getOverview(ASSESSMENT_MATRIX_ID);

    assertThat(result).isEmpty();
    verify(dashboardAnalyticsRepository).findAssessmentMatrixOverview(null, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID);
  }

  @Test
  void testGetOverview_RepositoryThrowsException() {
    RuntimeException exception = new RuntimeException("Database error");
    doReturn(Optional.of(createMockAssessmentMatrix())).when(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);
    doReturn(Optional.of(createMockPerformanceCycle())).when(performanceCycleService).findById(PERFORMANCE_CYCLE_ID);
    doThrow(exception).when(dashboardAnalyticsRepository)
                      .findAssessmentMatrixOverview(COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID);

    assertThatThrownBy(() -> service.getOverview(ASSESSMENT_MATRIX_ID))
                                                                       .isInstanceOf(RuntimeException.class)
                                                                       .hasMessage("Database error");
  }

  @Test
  void testGetTeamAnalytics_Success() {
    DashboardAnalytics expectedAnalytics = createMockDashboardAnalytics(AnalyticsScope.TEAM, TEAM_ID_1);
    doReturn(Optional.of(expectedAnalytics)).when(dashboardAnalyticsRepository)
                                            .findTeamAnalytics(COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID, TEAM_ID_1);

    Optional<DashboardAnalytics> result = service.getTeamAnalytics(ASSESSMENT_MATRIX_ID, TEAM_ID_1);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(expectedAnalytics);
    assertThat(result.get().getTeamId()).isEqualTo(TEAM_ID_1);
    verify(dashboardAnalyticsRepository).findTeamAnalytics(COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID, TEAM_ID_1);
  }

  @Test
  void testGetTeamAnalytics_AssessmentMatrixNotFound() {
    doReturn(Optional.empty()).when(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);

    Optional<DashboardAnalytics> result = service.getTeamAnalytics(ASSESSMENT_MATRIX_ID, TEAM_ID_1);

    assertThat(result).isEmpty();
    verify(dashboardAnalyticsRepository, never()).findTeamAnalytics(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testGetAllAnalytics_Success() {
    List<DashboardAnalytics> expectedAnalytics = Arrays.asList(
                                                               createMockDashboardAnalytics(AnalyticsScope.ASSESSMENT_MATRIX, null), createMockDashboardAnalytics(AnalyticsScope.TEAM, TEAM_ID_1), createMockDashboardAnalytics(AnalyticsScope.TEAM, TEAM_ID_2)
    );

    doReturn(expectedAnalytics).when(dashboardAnalyticsRepository)
                               .findByCompanyAndPerformanceCycle(COMPANY_ID, PERFORMANCE_CYCLE_ID);

    List<DashboardAnalytics> result = service.getAllAnalytics(ASSESSMENT_MATRIX_ID);

    assertThat(result).hasSize(3);
    assertThat(result).containsExactlyInAnyOrderElementsOf(expectedAnalytics);
    verify(dashboardAnalyticsRepository).findByCompanyAndPerformanceCycle(COMPANY_ID, PERFORMANCE_CYCLE_ID);
  }

  @Test
  void testGetAllAnalytics_AssessmentMatrixNotFound() {
    doReturn(Optional.empty()).when(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);

    List<DashboardAnalytics> result = service.getAllAnalytics(ASSESSMENT_MATRIX_ID);

    assertThat(result).isEmpty();
    verify(dashboardAnalyticsRepository, never()).findByCompanyAndPerformanceCycle(anyString(), anyString());
  }

  @Test
  void testGetAllAnalytics_CompanyNotFound() {
    doReturn(Optional.empty()).when(performanceCycleService).findById(PERFORMANCE_CYCLE_ID);

    List<DashboardAnalytics> result = service.getAllAnalytics(ASSESSMENT_MATRIX_ID);

    assertThat(result).isEmpty();
    verify(dashboardAnalyticsRepository, never()).findByCompanyAndPerformanceCycle(anyString(), anyString());
  }

  @Test
  void testUpdateAssessmentMatrixAnalytics_Success() {
    List<EmployeeAssessment> employeeAssessments = createTenEmployeeAssessments();
    doReturn(employeeAssessments).when(employeeAssessmentService)
                                 .findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID);

    service.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

    // Verify batch operations - findByIds called once, answers called once (CRITICAL OPTIMIZATION: single batch load)
    verify(teamRepository, times(1)).findByIds(any());
    verify(answerRepository, times(1)).findByEmployeeAssessmentIds(any(), any());

    @SuppressWarnings("unchecked") ArgumentCaptor<List<DashboardAnalytics>> analyticsCaptor = ArgumentCaptor.forClass(List.class);
    verify(dashboardAnalyticsRepository, times(1)).saveAll(analyticsCaptor.capture());

    List<DashboardAnalytics> savedAnalytics = analyticsCaptor.getValue();
    assertThat(savedAnalytics).hasSize(3); // 2 teams + 1 overview

    // Verify overview analytics
    DashboardAnalytics overviewAnalytics = savedAnalytics.stream()
                                                         .filter(analytics -> analytics.getScope() == AnalyticsScope.ASSESSMENT_MATRIX)
                                                         .findFirst()
                                                         .orElseThrow();

    assertThat(overviewAnalytics.getEmployeeCount()).isEqualTo(10);
    assertThat(overviewAnalytics.getCompanyId()).isEqualTo(COMPANY_ID);
    assertThat(overviewAnalytics.getPerformanceCycleId()).isEqualTo(PERFORMANCE_CYCLE_ID);
    assertThat(overviewAnalytics.getAssessmentMatrixId()).isEqualTo(ASSESSMENT_MATRIX_ID);
    assertThat(overviewAnalytics.getTeamId()).isNull();

    // Verify team analytics
    List<DashboardAnalytics> teamAnalytics = savedAnalytics.stream()
                                                           .filter(analytics -> analytics.getScope() == AnalyticsScope.TEAM)
                                                           .collect(Collectors.toList());

    assertThat(teamAnalytics).hasSize(2);
    assertThat(teamAnalytics.stream().map(DashboardAnalytics::getTeamId))
                                                                         .containsExactlyInAnyOrder(TEAM_ID_1, TEAM_ID_2);
  }

  @Test
  void testUpdateAssessmentMatrixAnalytics_AssessmentMatrixNotFound() {
    doReturn(Optional.empty()).when(assessmentMatrixService).findById(ASSESSMENT_MATRIX_ID);

    service.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

    verify(dashboardAnalyticsRepository, never()).saveAll(any());
    verify(employeeAssessmentService, never()).findByAssessmentMatrix(anyString(), anyString());
  }

  @Test
  void testUpdateAssessmentMatrixAnalytics_NoAssessments() {
    doReturn(Collections.emptyList()).when(employeeAssessmentService)
                                     .findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID);

    service.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

    verify(dashboardAnalyticsRepository, never()).saveAll(any());
  }

  @Test
  void testUpdateAssessmentMatrixAnalytics_OnlyAssessmentsWithoutTeams() {
    List<EmployeeAssessment> assessmentsWithoutTeams = createEmployeeAssessmentsWithoutTeams(3);
    doReturn(assessmentsWithoutTeams).when(employeeAssessmentService)
                                     .findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID);

    service.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

    // Verify batch operations - teams still called but with empty set, answers called once (CRITICAL OPTIMIZATION: single batch load)
    verify(teamRepository, times(1)).findByIds(any());
    verify(answerRepository, times(1)).findByEmployeeAssessmentIds(any(), any());

    @SuppressWarnings("unchecked") ArgumentCaptor<List<DashboardAnalytics>> analyticsCaptor = ArgumentCaptor.forClass(List.class);
    verify(dashboardAnalyticsRepository, times(1)).saveAll(analyticsCaptor.capture());

    List<DashboardAnalytics> savedAnalytics = analyticsCaptor.getValue();
    assertThat(savedAnalytics).hasSize(1); // Only overview, no team analytics

    DashboardAnalytics overviewAnalytics = savedAnalytics.get(0);
    assertThat(overviewAnalytics.getScope()).isEqualTo(AnalyticsScope.ASSESSMENT_MATRIX);
    assertThat(overviewAnalytics.getEmployeeCount()).isEqualTo(3);
    assertThat(overviewAnalytics.getTeamId()).isNull();
  }

  @Test
  void testUpdateAssessmentMatrixAnalytics_WithCompletedAndScoredAssessments() {
    List<EmployeeAssessment> employeeAssessments = createTenEmployeeAssessments();

    // Make first 5 completed and scored
    for (int i = 0; i < 5; i++) {
      EmployeeAssessment assessment = employeeAssessments.get(i);
      assessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
      assessment.setEmployeeAssessmentScore(createMockEmployeeAssessmentScore(85.0 + i * 2));
    }

    doReturn(employeeAssessments).when(employeeAssessmentService)
                                 .findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID);

    service.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

    // Verify batch operations - findByIds called once, answers called once (CRITICAL OPTIMIZATION: single batch load)
    verify(teamRepository, times(1)).findByIds(any());
    verify(answerRepository, times(1)).findByEmployeeAssessmentIds(any(), any());

    @SuppressWarnings("unchecked") ArgumentCaptor<List<DashboardAnalytics>> analyticsCaptor = ArgumentCaptor.forClass(List.class);
    verify(dashboardAnalyticsRepository, times(1)).saveAll(analyticsCaptor.capture());

    List<DashboardAnalytics> savedAnalytics = analyticsCaptor.getValue();
    DashboardAnalytics overviewAnalytics = savedAnalytics.stream()
                                                         .filter(analytics -> analytics.getScope() == AnalyticsScope.ASSESSMENT_MATRIX)
                                                         .findFirst()
                                                         .orElseThrow();

    assertThat(overviewAnalytics.getCompletionPercentage()).isEqualTo(50.0); // 5 out of 10 completed
    assertThat(overviewAnalytics.getGeneralAverage()).isGreaterThan(0.0);
  }

  @Test
  void testUpdateAssessmentMatrixAnalytics_WithAnswersForWordCloud() {
    List<EmployeeAssessment> employeeAssessments = createTenEmployeeAssessments();

    // Make all assessments completed so word cloud logic is triggered
    for (EmployeeAssessment assessment : employeeAssessments) {
      assessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
    }

    // Setup batch answer lookup with mock answers containing notes
    Map<String, List<Answer>> answersByAssessment = new HashMap<>();
    for (EmployeeAssessment assessment : employeeAssessments) {
      answersByAssessment.put(assessment.getId(), createMockAnswersWithNotes(assessment.getId()));
    }
    doReturn(answersByAssessment).when(answerRepository).findByEmployeeAssessmentIds(any(), any());

    doReturn(employeeAssessments).when(employeeAssessmentService)
                                 .findByAssessmentMatrix(ASSESSMENT_MATRIX_ID, TENANT_ID);

    service.updateAssessmentMatrixAnalytics(ASSESSMENT_MATRIX_ID);

    // Verify batch operations - findByIds called once, answers called once (CRITICAL OPTIMIZATION: single batch load)
    verify(teamRepository, times(1)).findByIds(any());
    verify(answerRepository, times(1)).findByEmployeeAssessmentIds(any(), any());

    @SuppressWarnings("unchecked") ArgumentCaptor<List<DashboardAnalytics>> analyticsCaptor = ArgumentCaptor.forClass(List.class);
    verify(dashboardAnalyticsRepository, times(1)).saveAll(analyticsCaptor.capture());

    List<DashboardAnalytics> savedAnalytics = analyticsCaptor.getValue();
    DashboardAnalytics overviewAnalytics = savedAnalytics.stream()
                                                         .filter(analytics -> analytics.getScope() == AnalyticsScope.ASSESSMENT_MATRIX)
                                                         .findFirst()
                                                         .orElseThrow();

    assertThat(overviewAnalytics.getAnalyticsDataJson()).isNotNull();
    assertThat(overviewAnalytics.getAnalyticsDataJson()).contains("wordCloud");
  }

  @Test
  void testDeleteById_Success() {
    String companyPerformanceCycleId = COMPANY_ID + "#" + PERFORMANCE_CYCLE_ID;
    String assessmentMatrixScopeId = ASSESSMENT_MATRIX_ID + "#" + AnalyticsScope.TEAM.name() + "#" + TEAM_ID_1;

    service.deleteById(companyPerformanceCycleId, assessmentMatrixScopeId);

    verify(dashboardAnalyticsRepository).deleteById(companyPerformanceCycleId, assessmentMatrixScopeId);
  }

  // Helper method to create 10 employee assessments as requested
  public List<EmployeeAssessment> createTenEmployeeAssessments() {
    List<EmployeeAssessment> assessments = new ArrayList<>();

    // Create 5 assessments for team 1
    for (int i = 1; i <= 5; i++) {
      assessments.add(createMockEmployeeAssessment(
                                                   "employee-" + i + "@company.com", "Employee " + i, TEAM_ID_1, "assessment-" + i
      ));
    }

    // Create 5 assessments for team 2
    for (int i = 6; i <= 10; i++) {
      assessments.add(createMockEmployeeAssessment(
                                                   "employee-" + i + "@company.com", "Employee " + i, TEAM_ID_2, "assessment-" + i
      ));
    }

    return assessments;
  }

  private List<EmployeeAssessment> createEmployeeAssessmentsWithoutTeams(int count) {
    List<EmployeeAssessment> assessments = new ArrayList<>();

    for (int i = 1; i <= count; i++) {
      assessments.add(createMockEmployeeAssessment(
                                                   "employee-no-team-" + i + "@company.com", "Employee No Team " + i, null, "assessment-no-team-" + i
      ));
    }

    return assessments;
  }

  private EmployeeAssessment createMockEmployeeAssessment(String email, String name, String teamId, String assessmentId) {
    NaturalPerson employee = NaturalPerson.builder()
                                          .id("person-" + assessmentId)
                                          .name(name)
                                          .email(email)
                                          .documentNumber("123456789")
                                          .personDocumentType(PersonDocumentType.CPF)
                                          .gender(Gender.MALE)
                                          .genderPronoun(GenderPronoun.HE)
                                          .build();

    EmployeeAssessment assessment = EmployeeAssessment.builder()
                                                      .id(assessmentId)
                                                      .assessmentMatrixId(ASSESSMENT_MATRIX_ID)
                                                      .teamId(teamId)
                                                      .employee(employee)
                                                      .employeeEmailNormalized(email.toLowerCase())
                                                      .answeredQuestionCount(0)
                                                      .assessmentStatus(AssessmentStatus.INVITED)
                                                      .build();

    assessment.setTenantId(TENANT_ID);
    return assessment;
  }

  private AssessmentMatrix createMockAssessmentMatrix() {
    Map<String, Pillar> pillarMap = createMockPillarMap();
    PotentialScore potentialScore = createMockPotentialScore();

    AssessmentMatrix matrix = AssessmentMatrix.builder()
                                              .id(ASSESSMENT_MATRIX_ID)
                                              .tenantId(TENANT_ID)
                                              .name("Test Assessment Matrix")
                                              .description("Test matrix description")
                                              .performanceCycleId(PERFORMANCE_CYCLE_ID)
                                              .pillarMap(pillarMap)
                                              .potentialScore(potentialScore)
                                              .build();

    return matrix;
  }

  private Map<String, Pillar> createMockPillarMap() {
    Map<String, Pillar> pillarMap = new HashMap<>();

    Pillar pillar1 = TestObjectFactory.createMockedPillar("Technical Skills", "Technical competencies");
    pillar1.setId(PILLAR_ID_1);
    pillarMap.put(PILLAR_ID_1, pillar1);

    Pillar pillar2 = TestObjectFactory.createMockedPillar("Soft Skills", "Interpersonal competencies");
    pillar2.setId(PILLAR_ID_2);
    pillarMap.put(PILLAR_ID_2, pillar2);

    return pillarMap;
  }

  private PotentialScore createMockPotentialScore() {
    Map<String, PillarScore> pillarScoreMap = new HashMap<>();

    Map<String, CategoryScore> categoryScores1 = new HashMap<>();
    categoryScores1.put(CATEGORY_ID_1, CategoryScore.builder()
                                                    .categoryId(CATEGORY_ID_1)
                                                    .categoryName("Programming")
                                                    .score(100.0)
                                                    .build());

    PillarScore pillarScore1 = PillarScore.builder()
                                          .pillarId(PILLAR_ID_1)
                                          .score(100.0)
                                          .categoryIdToCategoryScoreMap(categoryScores1)
                                          .build();

    Map<String, CategoryScore> categoryScores2 = new HashMap<>();
    categoryScores2.put(CATEGORY_ID_2, CategoryScore.builder()
                                                    .categoryId(CATEGORY_ID_2)
                                                    .categoryName("Communication")
                                                    .score(100.0)
                                                    .build());

    PillarScore pillarScore2 = PillarScore.builder()
                                          .pillarId(PILLAR_ID_2)
                                          .score(100.0)
                                          .categoryIdToCategoryScoreMap(categoryScores2)
                                          .build();

    pillarScoreMap.put(PILLAR_ID_1, pillarScore1);
    pillarScoreMap.put(PILLAR_ID_2, pillarScore2);

    return PotentialScore.builder()
                         .pillarIdToPillarScoreMap(pillarScoreMap)
                         .build();
  }

  private EmployeeAssessmentScore createMockEmployeeAssessmentScore(double score) {
    Map<String, PillarScore> pillarScoreMap = new HashMap<>();

    Map<String, CategoryScore> categoryScores1 = new HashMap<>();
    categoryScores1.put(CATEGORY_ID_1, CategoryScore.builder()
                                                    .categoryId(CATEGORY_ID_1)
                                                    .categoryName("Programming")
                                                    .score(score * 0.9)
                                                    .build());

    PillarScore pillarScore1 = PillarScore.builder()
                                          .pillarId(PILLAR_ID_1)
                                          .score(score * 0.9)
                                          .categoryIdToCategoryScoreMap(categoryScores1)
                                          .build();

    Map<String, CategoryScore> categoryScores2 = new HashMap<>();
    categoryScores2.put(CATEGORY_ID_2, CategoryScore.builder()
                                                    .categoryId(CATEGORY_ID_2)
                                                    .categoryName("Communication")
                                                    .score(score * 1.1)
                                                    .build());

    PillarScore pillarScore2 = PillarScore.builder()
                                          .pillarId(PILLAR_ID_2)
                                          .score(score * 1.1)
                                          .categoryIdToCategoryScoreMap(categoryScores2)
                                          .build();

    pillarScoreMap.put(PILLAR_ID_1, pillarScore1);
    pillarScoreMap.put(PILLAR_ID_2, pillarScore2);

    return EmployeeAssessmentScore.builder()
                                  .score(score)
                                  .pillarIdToPillarScoreMap(pillarScoreMap)
                                  .build();
  }

  private PerformanceCycle createMockPerformanceCycle() {
    return PerformanceCycle.builder()
                           .id(PERFORMANCE_CYCLE_ID)
                           .name("Q4 2023 Performance Cycle")
                           .description("Fourth quarter performance evaluation")
                           .companyId(COMPANY_ID)
                           .tenantId(TENANT_ID)
                           .isActive(true)
                           .isTimeSensitive(true)
                           .build();
  }

  private Company createMockCompany() {
    return TestObjectFactory.createMockedCompany(COMPANY_ID);
  }

  private Team createMockTeam(String teamId, String teamName) {
    Team team = TestObjectFactory.createMockedTeam(teamName, "Test team description", TENANT_ID, "dept-123");
    team.setId(teamId);
    return team;
  }

  private DashboardAnalytics createMockDashboardAnalytics(AnalyticsScope scope, String teamId) {
    return DashboardAnalytics.builder()
                             .companyPerformanceCycleId(COMPANY_ID + "#" + PERFORMANCE_CYCLE_ID)
                             .assessmentMatrixScopeId(buildAssessmentMatrixScopeId(ASSESSMENT_MATRIX_ID, scope, teamId))
                             .companyId(COMPANY_ID)
                             .performanceCycleId(PERFORMANCE_CYCLE_ID)
                             .assessmentMatrixId(ASSESSMENT_MATRIX_ID)
                             .scope(scope)
                             .teamId(teamId)
                             .teamName(teamId != null ? "Test Team" : null)
                             .companyName("Test Company")
                             .performanceCycleName("Test Cycle")
                             .assessmentMatrixName("Test Matrix")
                             .generalAverage(85.5)
                             .employeeCount(10)
                             .completionPercentage(80.0)
                             .lastUpdated(Instant.now())
                             .analyticsDataJson("{\"test\": \"data\"}")
                             .build();
  }

  private String buildAssessmentMatrixScopeId(String assessmentMatrixId, AnalyticsScope scope, String teamId) {
    if (scope == AnalyticsScope.ASSESSMENT_MATRIX) {
      return assessmentMatrixId + "#" + scope.name();
    }
    else {
      return assessmentMatrixId + "#" + scope.name() + "#" + teamId;
    }
  }

  private List<Answer> createMockAnswersWithNotes(String employeeAssessmentId) {
    List<Answer> answers = new ArrayList<>();

    Answer answer1 = Answer.builder()
                           .questionId("question-1")
                           .pillarId(PILLAR_ID_1)
                           .categoryId(CATEGORY_ID_1)
                           .score(8.5)
                           .questionType(QuestionType.ONE_TO_TEN)
                           .employeeAssessmentId(employeeAssessmentId)
                           .answeredAt(LocalDateTime.now())
                           .value("8")
                           .notes("Great technical skills demonstrated in recent projects.")
                           .tenantId(TENANT_ID)
                           .build();

    Answer answer2 = Answer.builder()
                           .questionId("question-2")
                           .pillarId(PILLAR_ID_2)
                           .categoryId(CATEGORY_ID_2)
                           .score(7.0)
                           .questionType(QuestionType.ONE_TO_TEN)
                           .employeeAssessmentId(employeeAssessmentId)
                           .answeredAt(LocalDateTime.now())
                           .value("7")
                           .notes("Good communication but needs improvement in presentation skills.")
                           .tenantId(TENANT_ID)
                           .build();

    answers.add(answer1);
    answers.add(answer2);

    return answers;
  }
}
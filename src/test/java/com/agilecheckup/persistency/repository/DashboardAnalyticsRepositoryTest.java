package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.AnalyticsScope;
import com.agilecheckup.persistency.entity.DashboardAnalytics;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardAnalyticsRepositoryTest extends AbstractRepositoryTest<DashboardAnalytics> {

    @Mock
    private PaginatedQueryList<DashboardAnalytics> paginatedQueryList;

    @InjectMocks
    @Spy
    private DashboardAnalyticsRepository dashboardAnalyticsRepository;

    private static final String COMPANY_ID = "company123";
    private static final String PERFORMANCE_CYCLE_ID = "cycle456";
    private static final String ASSESSMENT_MATRIX_ID = "matrix789";
    private static final String TEAM_ID = "team101";
    private static final String COMPANY_PERFORMANCE_CYCLE_ID = COMPANY_ID + "#" + PERFORMANCE_CYCLE_ID;
  private static final String ASSESSMENT_MATRIX_SCOPE_ID_TEAM = ASSESSMENT_MATRIX_ID + "#" + AnalyticsScope.TEAM.name() + "#" + TEAM_ID;
  private static final String ASSESSMENT_MATRIX_SCOPE_ID_OVERVIEW = ASSESSMENT_MATRIX_ID + "#" + AnalyticsScope.ASSESSMENT_MATRIX.name();

    @Override
    AbstractCrudRepository getRepository() {
        return dashboardAnalyticsRepository;
    }

    @Override
    DashboardAnalytics createMockedT() {
      return createTestAnalytics(TEAM_ID, AnalyticsScope.TEAM);
    }

    @Override
    Class getMockedClass() {
        return DashboardAnalytics.class;
    }

    @Test
    void findByCompanyAndPerformanceCycle_ShouldQueryGSI() {
        // Given
      DashboardAnalytics analytics1 = createTestAnalytics("team1", AnalyticsScope.TEAM);
      DashboardAnalytics analytics2 = createTestAnalytics("team2", AnalyticsScope.TEAM);
        List<DashboardAnalytics> expectedResults = Arrays.asList(analytics1, analytics2);

        when(dynamoDBMapperMock.query(eq(DashboardAnalytics.class), any(DynamoDBQueryExpression.class)))
                .thenReturn(paginatedQueryList);

        // When
        List<DashboardAnalytics> results = dashboardAnalyticsRepository.findByCompanyAndPerformanceCycle(COMPANY_ID, PERFORMANCE_CYCLE_ID);

        // Then
        assertEquals(paginatedQueryList, results);
        
        ArgumentCaptor<DynamoDBQueryExpression<DashboardAnalytics>> queryCaptor = 
                ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
        verify(dynamoDBMapperMock).query(eq(DashboardAnalytics.class), queryCaptor.capture());
        
        DynamoDBQueryExpression<DashboardAnalytics> query = queryCaptor.getValue();
        assertEquals("company-cycle-index", query.getIndexName());
        assertFalse(query.isConsistentRead());
      assertEquals("companyId = :companyId AND performanceCycleId = :performanceCycleId",
                query.getKeyConditionExpression());
    }

    @Test
    void findTeamAnalytics_ShouldLoadByCompositeKey() {
        // Given
      DashboardAnalytics expectedAnalytics = createTestAnalytics(TEAM_ID, AnalyticsScope.TEAM);
      when(dynamoDBMapperMock.load(DashboardAnalytics.class, COMPANY_PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_SCOPE_ID_TEAM))
                .thenReturn(expectedAnalytics);

        // When
      Optional<DashboardAnalytics> result = dashboardAnalyticsRepository.findTeamAnalytics(
                COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID, TEAM_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedAnalytics, result.get());
      verify(dynamoDBMapperMock).load(DashboardAnalytics.class, COMPANY_PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_SCOPE_ID_TEAM);
    }

    @Test
    void findTeamAnalytics_WhenNotFound_ShouldReturnEmpty() {
        // Given
      when(dynamoDBMapperMock.load(DashboardAnalytics.class, COMPANY_PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_SCOPE_ID_TEAM))
                .thenReturn(null);

        // When
      Optional<DashboardAnalytics> result = dashboardAnalyticsRepository.findTeamAnalytics(
                COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID, TEAM_ID);

        // Then
        assertFalse(result.isPresent());
      verify(dynamoDBMapperMock).load(DashboardAnalytics.class, COMPANY_PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_SCOPE_ID_TEAM);
    }

  @Test
  void findAssessmentMatrixOverview_ShouldLoadByCompositeKey() {
    // Given
    DashboardAnalytics expectedAnalytics = createTestAnalytics(null, AnalyticsScope.ASSESSMENT_MATRIX);
    when(dynamoDBMapperMock.load(DashboardAnalytics.class, COMPANY_PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_SCOPE_ID_OVERVIEW))
        .thenReturn(expectedAnalytics);

    // When
    Optional<DashboardAnalytics> result = dashboardAnalyticsRepository.findAssessmentMatrixOverview(
        COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID);

    // Then
    assertTrue(result.isPresent());
    assertEquals(expectedAnalytics, result.get());
    verify(dynamoDBMapperMock).load(DashboardAnalytics.class, COMPANY_PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_SCOPE_ID_OVERVIEW);
  }

  @Test
  void findAssessmentMatrixOverview_WhenNotFound_ShouldReturnEmpty() {
    // Given
    when(dynamoDBMapperMock.load(DashboardAnalytics.class, COMPANY_PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_SCOPE_ID_OVERVIEW))
        .thenReturn(null);

    // When
    Optional<DashboardAnalytics> result = dashboardAnalyticsRepository.findAssessmentMatrixOverview(
        COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID);

    // Then
    assertFalse(result.isPresent());
    verify(dynamoDBMapperMock).load(DashboardAnalytics.class, COMPANY_PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_SCOPE_ID_OVERVIEW);
    }

    @Test
    void findByCompany_ShouldQueryGSIWithCompanyId() {
        // Given
      DashboardAnalytics analytics1 = createTestAnalytics("team1", AnalyticsScope.TEAM);
      DashboardAnalytics analytics2 = createTestAnalytics("team2", AnalyticsScope.TEAM);
        List<DashboardAnalytics> expectedResults = Arrays.asList(analytics1, analytics2);

        when(dynamoDBMapperMock.query(eq(DashboardAnalytics.class), any(DynamoDBQueryExpression.class)))
                .thenReturn(paginatedQueryList);

        // When
        List<DashboardAnalytics> results = dashboardAnalyticsRepository.findByCompany(COMPANY_ID);

        // Then
        assertEquals(paginatedQueryList, results);
        
        ArgumentCaptor<DynamoDBQueryExpression<DashboardAnalytics>> queryCaptor = 
                ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
        verify(dynamoDBMapperMock).query(eq(DashboardAnalytics.class), queryCaptor.capture());
        
        DynamoDBQueryExpression<DashboardAnalytics> query = queryCaptor.getValue();
        assertEquals("company-cycle-index", query.getIndexName());
      assertEquals("companyId = :companyId", query.getKeyConditionExpression());
    }

    @Test
    void save_ShouldPersistAnalytics() {
        // Given
      DashboardAnalytics analytics = createTestAnalytics(TEAM_ID, AnalyticsScope.TEAM);

        // When
        dashboardAnalyticsRepository.save(analytics);

        // Then
        verify(dynamoDBMapperMock).save(analytics);
    }

  private DashboardAnalytics createTestAnalytics(String teamId, AnalyticsScope scope) {
    String assessmentMatrixScopeId;
    String teamName = null;

    if (scope == AnalyticsScope.TEAM) {
      assessmentMatrixScopeId = ASSESSMENT_MATRIX_ID + "#" + scope.name() + "#" + teamId;
      teamName = "Team " + teamId;
    }
    else {
      assessmentMatrixScopeId = ASSESSMENT_MATRIX_ID + "#" + scope.name();
      teamId = null; // Assessment matrix scope doesn't have a team ID
    }

    return DashboardAnalytics.builder()
                .companyPerformanceCycleId(COMPANY_PERFORMANCE_CYCLE_ID)
        .assessmentMatrixScopeId(assessmentMatrixScopeId)
                .companyId(COMPANY_ID)
                .performanceCycleId(PERFORMANCE_CYCLE_ID)
                .assessmentMatrixId(ASSESSMENT_MATRIX_ID)
        .scope(scope)
                .teamId(teamId)
        .teamName(teamName)
                .generalAverage(75.5)
                .employeeCount(10)
                .completionPercentage(80.0)
                .lastUpdated(LocalDateTime.now())
                .analyticsDataJson("{\"test\": \"data\"}")
                .build();
    }
}
package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.AnalyticsScope;
import com.agilecheckup.persistency.entity.DashboardAnalyticsV2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@ExtendWith(MockitoExtension.class)
class DashboardAnalyticsRepositoryTest {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;
    
    @Mock
    private DynamoDbTable<DashboardAnalyticsV2> mockTable;
    
    @Mock
    private DynamoDbIndex<DashboardAnalyticsV2> mockIndex;
    
    @Mock
    private PageIterable<DashboardAnalyticsV2> pageIterable;
    
    @Mock
    private Page<DashboardAnalyticsV2> page;

    @InjectMocks
    @Spy
    private DashboardAnalyticsRepositoryV2 dashboardAnalyticsRepository;

    private static final String COMPANY_ID = "company123";
    private static final String PERFORMANCE_CYCLE_ID = "cycle456";
    private static final String ASSESSMENT_MATRIX_ID = "matrix789";
    private static final String TEAM_ID = "team101";
    private static final String COMPANY_PERFORMANCE_CYCLE_ID = COMPANY_ID + "#" + PERFORMANCE_CYCLE_ID;
  private static final String ASSESSMENT_MATRIX_SCOPE_ID_TEAM = ASSESSMENT_MATRIX_ID + "#" + AnalyticsScope.TEAM.name() + "#" + TEAM_ID;
  private static final String ASSESSMENT_MATRIX_SCOPE_ID_OVERVIEW = ASSESSMENT_MATRIX_ID + "#" + AnalyticsScope.ASSESSMENT_MATRIX.name();


    @Test
    void findByCompanyAndPerformanceCycle_ShouldQueryGSI() {
        // Given
        DashboardAnalyticsV2 analytics1 = createTestAnalytics("team1", AnalyticsScope.TEAM);
        DashboardAnalyticsV2 analytics2 = createTestAnalytics("team2", AnalyticsScope.TEAM);
        List<DashboardAnalyticsV2> expectedResults = Arrays.asList(analytics1, analytics2);

        when(dashboardAnalyticsRepository.getTable()).thenReturn(mockTable);
        when(mockTable.index("company-cycle-index")).thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(expectedResults);

        // When
        List<DashboardAnalyticsV2> results = dashboardAnalyticsRepository.findByCompanyAndPerformanceCycle(COMPANY_ID, PERFORMANCE_CYCLE_ID);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).containsExactly(analytics1, analytics2);
        verify(mockTable).index("company-cycle-index");
        verify(mockIndex).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void findTeamAnalytics_ShouldLoadByCompositeKey() {
        // Given
        DashboardAnalyticsV2 expectedAnalytics = createTestAnalytics(TEAM_ID, AnalyticsScope.TEAM);
        when(dashboardAnalyticsRepository.getTable()).thenReturn(mockTable);
        when(mockTable.getItem(any(Key.class))).thenReturn(expectedAnalytics);

        // When
        Optional<DashboardAnalyticsV2> result = dashboardAnalyticsRepository.findTeamAnalytics(
                COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID, TEAM_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedAnalytics);
        verify(mockTable).getItem(any(Key.class));
    }

    @Test
    void findTeamAnalytics_WhenNotFound_ShouldReturnEmpty() {
        // Given
        when(dashboardAnalyticsRepository.getTable()).thenReturn(mockTable);
        when(mockTable.getItem(any(Key.class))).thenReturn(null);

        // When
        Optional<DashboardAnalyticsV2> result = dashboardAnalyticsRepository.findTeamAnalytics(
                COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID, TEAM_ID);

        // Then
        assertThat(result).isEmpty();
        verify(mockTable).getItem(any(Key.class));
    }

    @Test
    void findAssessmentMatrixOverview_ShouldLoadByCompositeKey() {
        // Given
        DashboardAnalyticsV2 expectedAnalytics = createTestAnalytics(null, AnalyticsScope.ASSESSMENT_MATRIX);
        when(dashboardAnalyticsRepository.getTable()).thenReturn(mockTable);
        when(mockTable.getItem(any(Key.class))).thenReturn(expectedAnalytics);

        // When
        Optional<DashboardAnalyticsV2> result = dashboardAnalyticsRepository.findAssessmentMatrixOverview(
                COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedAnalytics);
        verify(mockTable).getItem(any(Key.class));
    }

    @Test
    void findAssessmentMatrixOverview_WhenNotFound_ShouldReturnEmpty() {
        // Given
        when(dashboardAnalyticsRepository.getTable()).thenReturn(mockTable);
        when(mockTable.getItem(any(Key.class))).thenReturn(null);

        // When
        Optional<DashboardAnalyticsV2> result = dashboardAnalyticsRepository.findAssessmentMatrixOverview(
                COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID);

        // Then
        assertThat(result).isEmpty();
        verify(mockTable).getItem(any(Key.class));
    }

    @Test
    void findByCompany_ShouldQueryGSIWithCompanyId() {
        // Given
        DashboardAnalyticsV2 analytics1 = createTestAnalytics("team1", AnalyticsScope.TEAM);
        DashboardAnalyticsV2 analytics2 = createTestAnalytics("team2", AnalyticsScope.TEAM);
        List<DashboardAnalyticsV2> expectedResults = Arrays.asList(analytics1, analytics2);

        when(dashboardAnalyticsRepository.getTable()).thenReturn(mockTable);
        when(mockTable.index("company-cycle-index")).thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(expectedResults);

        // When
        List<DashboardAnalyticsV2> results = dashboardAnalyticsRepository.findByCompany(COMPANY_ID);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).containsExactly(analytics1, analytics2);
        verify(mockTable).index("company-cycle-index");
        verify(mockIndex).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void save_ShouldPersistAnalytics() {
        // Given
        DashboardAnalyticsV2 analytics = createTestAnalytics(TEAM_ID, AnalyticsScope.TEAM);
        when(dashboardAnalyticsRepository.getTable()).thenReturn(mockTable);

        // When
        dashboardAnalyticsRepository.save(analytics);

        // Then
        verify(mockTable).putItem(analytics);
    }

    private DashboardAnalyticsV2 createTestAnalytics(String teamId, AnalyticsScope scope) {
        String assessmentMatrixScopeId;
        String teamName = null;

        if (scope == AnalyticsScope.TEAM) {
            assessmentMatrixScopeId = ASSESSMENT_MATRIX_ID + "#" + scope.name() + "#" + teamId;
            teamName = "Team " + teamId;
        } else {
            assessmentMatrixScopeId = ASSESSMENT_MATRIX_ID + "#" + scope.name();
            teamId = null; // Assessment matrix scope doesn't have a team ID
        }

        return DashboardAnalyticsV2.builder()
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
                .lastUpdated(Instant.now())
                .analyticsDataJson("{\"test\": \"data\"}")
                .build();
    }
}
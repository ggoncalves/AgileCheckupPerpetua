package com.agilecheckup.persistency.repository;

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
    private static final String ASSESSMENT_MATRIX_TEAM_ID = ASSESSMENT_MATRIX_ID + "#" + TEAM_ID;

    @Override
    AbstractCrudRepository getRepository() {
        return dashboardAnalyticsRepository;
    }

    @Override
    DashboardAnalytics createMockedT() {
        return createTestAnalytics(TEAM_ID);
    }

    @Override
    Class getMockedClass() {
        return DashboardAnalytics.class;
    }

    @Test
    void findByCompanyAndPerformanceCycle_ShouldQueryGSI() {
        // Given
        DashboardAnalytics analytics1 = createTestAnalytics("team1");
        DashboardAnalytics analytics2 = createTestAnalytics("team2");
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
    void findByCompanyPerformanceCycleAndTeam_ShouldLoadByCompositeKey() {
        // Given
        DashboardAnalytics expectedAnalytics = createTestAnalytics(TEAM_ID);
        when(dynamoDBMapperMock.load(DashboardAnalytics.class, COMPANY_PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_TEAM_ID))
                .thenReturn(expectedAnalytics);

        // When
        Optional<DashboardAnalytics> result = dashboardAnalyticsRepository.findByCompanyPerformanceCycleAndTeam(
                COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID, TEAM_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedAnalytics, result.get());
        verify(dynamoDBMapperMock).load(DashboardAnalytics.class, COMPANY_PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_TEAM_ID);
    }

    @Test
    void findByCompanyPerformanceCycleAndTeam_WhenNotFound_ShouldReturnEmpty() {
        // Given
        when(dynamoDBMapperMock.load(DashboardAnalytics.class, COMPANY_PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_TEAM_ID))
                .thenReturn(null);

        // When
        Optional<DashboardAnalytics> result = dashboardAnalyticsRepository.findByCompanyPerformanceCycleAndTeam(
                COMPANY_ID, PERFORMANCE_CYCLE_ID, ASSESSMENT_MATRIX_ID, TEAM_ID);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByCompany_ShouldQueryGSIWithCompanyId() {
        // Given
        DashboardAnalytics analytics1 = createTestAnalytics("team1");
        DashboardAnalytics analytics2 = createTestAnalytics("team2");
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
        DashboardAnalytics analytics = createTestAnalytics(TEAM_ID);

        // When
        dashboardAnalyticsRepository.save(analytics);

        // Then
        verify(dynamoDBMapperMock).save(analytics);
    }

    private DashboardAnalytics createTestAnalytics(String teamId) {
        return DashboardAnalytics.builder()
                .companyPerformanceCycleId(COMPANY_PERFORMANCE_CYCLE_ID)
                .assessmentMatrixTeamId(ASSESSMENT_MATRIX_ID + "#" + teamId)
                .companyId(COMPANY_ID)
                .performanceCycleId(PERFORMANCE_CYCLE_ID)
                .assessmentMatrixId(ASSESSMENT_MATRIX_ID)
                .teamId(teamId)
                .teamName("Team " + teamId)
                .generalAverage(75.5)
                .employeeCount(10)
                .completionPercentage(80.0)
                .lastUpdated(LocalDateTime.now())
                .analyticsDataJson("{\"test\": \"data\"}")
                .build();
    }
}
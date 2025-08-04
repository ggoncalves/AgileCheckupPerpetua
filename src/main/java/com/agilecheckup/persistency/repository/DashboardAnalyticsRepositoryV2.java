package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.AnalyticsScope;
import com.agilecheckup.persistency.entity.DashboardAnalyticsV2;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * V2 Repository for managing dashboard analytics data.
 */
@Slf4j
public class DashboardAnalyticsRepositoryV2 {

    private final DynamoDbEnhancedClient enhancedClient;
    private final Class<DashboardAnalyticsV2> entityClass;
    private final String tableName;

    @Inject
    public DashboardAnalyticsRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
        this.entityClass = DashboardAnalyticsV2.class;
        this.tableName = "DashboardAnalytics";
    }

    protected DynamoDbTable<DashboardAnalyticsV2> getTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(entityClass));
    }

    public void save(DashboardAnalyticsV2 entity) {
        getTable().putItem(entity);
    }

    /**
     * Find analytics by company and performance cycle using GSI
     */
    public List<DashboardAnalyticsV2> findByCompanyAndPerformanceCycle(String companyId, String performanceCycleId) {
        log.info("DashboardAnalyticsRepositoryV2.findByCompanyAndPerformanceCycle called with companyId: {}, performanceCycleId: {}", 
                 companyId, performanceCycleId);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(companyId)
                        .sortValue(performanceCycleId)
                        .build()
        );

        QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();

        return getTable().index("company-cycle-index")
                .query(queryRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Find analytics overview (assessment matrix scope) by composite key
     */
    public Optional<DashboardAnalyticsV2> findAssessmentMatrixOverview(String companyId, String performanceCycleId, String assessmentMatrixId) {
        log.info("DashboardAnalyticsRepositoryV2.findAssessmentMatrixOverview called with companyId: {}, performanceCycleId: {}, assessmentMatrixId: {}", 
                 companyId, performanceCycleId, assessmentMatrixId);

        String companyPerformanceCycleId = companyId + "#" + performanceCycleId;
        String assessmentMatrixScopeId = assessmentMatrixId + "#" + AnalyticsScope.ASSESSMENT_MATRIX.name();
        
        return findById(companyPerformanceCycleId, assessmentMatrixScopeId);
    }

    /**
     * Find team-specific analytics by composite key
     */
    public Optional<DashboardAnalyticsV2> findTeamAnalytics(String companyId, String performanceCycleId, String assessmentMatrixId, String teamId) {
        log.info("DashboardAnalyticsRepositoryV2.findTeamAnalytics called with companyId: {}, performanceCycleId: {}, assessmentMatrixId: {}, teamId: {}", 
                 companyId, performanceCycleId, assessmentMatrixId, teamId);

        String companyPerformanceCycleId = companyId + "#" + performanceCycleId;
        String assessmentMatrixScopeId = assessmentMatrixId + "#" + AnalyticsScope.TEAM.name() + "#" + teamId;
        
        return findById(companyPerformanceCycleId, assessmentMatrixScopeId);
    }

    /**
     * Find all analytics for a company using GSI
     */
    public List<DashboardAnalyticsV2> findByCompany(String companyId) {
        log.info("DashboardAnalyticsRepositoryV2.findByCompany called with companyId: {}", companyId);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(companyId)
                        .build()
        );

        QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();

        return getTable().index("company-cycle-index")
                .query(queryRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    /**
     * Find by composite primary key
     */
    public Optional<DashboardAnalyticsV2> findById(String companyPerformanceCycleId, String assessmentMatrixScopeId) {
        Key key = Key.builder()
                .partitionValue(companyPerformanceCycleId)
                .sortValue(assessmentMatrixScopeId)
                .build();

        DashboardAnalyticsV2 item = getTable().getItem(key);
        return Optional.ofNullable(item);
    }
}
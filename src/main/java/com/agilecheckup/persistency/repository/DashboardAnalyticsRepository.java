package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.DashboardAnalytics;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for managing dashboard analytics data.
 */
public class DashboardAnalyticsRepository extends AbstractCrudRepository<DashboardAnalytics> {

    @Inject
    public DashboardAnalyticsRepository() {
        super(DashboardAnalytics.class);
    }

    @VisibleForTesting
    public DashboardAnalyticsRepository(DynamoDBMapper dynamoDBMapper) {
        super(DashboardAnalytics.class, dynamoDBMapper);
    }

    /**
     * Find analytics by company and performance cycle using GSI
     */
    public List<DashboardAnalytics> findByCompanyAndPerformanceCycle(String companyId, String performanceCycleId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":companyId", new AttributeValue().withS(companyId));
        eav.put(":performanceCycleId", new AttributeValue().withS(performanceCycleId));

        DynamoDBQueryExpression<DashboardAnalytics> queryExpression = new DynamoDBQueryExpression<DashboardAnalytics>()
                .withIndexName("company-cycle-index")
                .withConsistentRead(false)
            .withKeyConditionExpression("companyId = :companyId AND performanceCycleId = :performanceCycleId")
                .withExpressionAttributeValues(eav);

        return dynamoDBMapper.query(DashboardAnalytics.class, queryExpression);
    }

    /**
     * Find specific analytics by composite key
     */
    public Optional<DashboardAnalytics> findByCompanyPerformanceCycleAndTeam(String companyId, String performanceCycleId, String assessmentMatrixId, String teamId) {
        String companyPerformanceCycleId = companyId + "#" + performanceCycleId;
        String assessmentMatrixTeamId = assessmentMatrixId + "#" + teamId;
        
        DashboardAnalytics analytics = dynamoDBMapper.load(DashboardAnalytics.class, companyPerformanceCycleId, assessmentMatrixTeamId);
        return Optional.ofNullable(analytics);
    }

    /**
     * Find all analytics for a company using GSI
     */
    public List<DashboardAnalytics> findByCompany(String companyId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":companyId", new AttributeValue().withS(companyId));

        DynamoDBQueryExpression<DashboardAnalytics> queryExpression = new DynamoDBQueryExpression<DashboardAnalytics>()
                .withIndexName("company-cycle-index")
                .withConsistentRead(false)
            .withKeyConditionExpression("companyId = :companyId")
                .withExpressionAttributeValues(eav);

        return dynamoDBMapper.query(DashboardAnalytics.class, queryExpression);
    }
}
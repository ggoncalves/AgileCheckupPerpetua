package com.agilecheckup.persistency.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.agilecheckup.persistency.entity.AnalyticsScope;
import com.agilecheckup.persistency.entity.DashboardAnalytics;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

/**
 * Repository for managing dashboard analytics data.
 */
@Slf4j
public class DashboardAnalyticsRepository {

  private final DynamoDbEnhancedClient enhancedClient;
  private final Class<DashboardAnalytics> entityClass;
  private final String tableName;

  @Inject
  public DashboardAnalyticsRepository(DynamoDbEnhancedClient enhancedClient) {
    this.enhancedClient = enhancedClient;
    this.entityClass = DashboardAnalytics.class;
    this.tableName = "DashboardAnalytics";
  }

  protected DynamoDbTable<DashboardAnalytics> getTable() {
    return enhancedClient.table(tableName, TableSchema.fromBean(entityClass));
  }

  public void save(DashboardAnalytics entity) {
    getTable().putItem(entity);
  }

  /**
   * Find analytics by company and performance cycle using GSI
   */
  public List<DashboardAnalytics> findByCompanyAndPerformanceCycle(String companyId, String performanceCycleId) {
    log.info("DashboardAnalyticsRepository.findByCompanyAndPerformanceCycle called with companyId: {}, performanceCycleId: {}", companyId, performanceCycleId);

    QueryConditional queryConditional = QueryConditional.keyEqualTo(
                                                                    Key.builder()
                                                                       .partitionValue(companyId)
                                                                       .sortValue(performanceCycleId)
                                                                       .build()
    );

    QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder().queryConditional(queryConditional).build();

    return getTable().index("company-cycle-index")
                     .query(queryRequest)
                     .stream()
                     .flatMap(page -> page.items().stream())
                     .collect(java.util.stream.Collectors.toList());
  }

  /**
   * Find analytics overview (assessment matrix scope) by composite key
   */
  public Optional<DashboardAnalytics> findAssessmentMatrixOverview(String companyId, String performanceCycleId, String assessmentMatrixId) {
    log.info("DashboardAnalyticsRepository.findAssessmentMatrixOverview called with companyId: {}, performanceCycleId: {}, assessmentMatrixId: {}", companyId, performanceCycleId, assessmentMatrixId);

    String companyPerformanceCycleId = companyId + "#" + performanceCycleId;
    String assessmentMatrixScopeId = assessmentMatrixId + "#" + AnalyticsScope.ASSESSMENT_MATRIX.name();

    return findById(companyPerformanceCycleId, assessmentMatrixScopeId);
  }

  /**
   * Find team-specific analytics by composite key
   */
  public Optional<DashboardAnalytics> findTeamAnalytics(String companyId, String performanceCycleId, String assessmentMatrixId, String teamId) {
    log.info("DashboardAnalyticsRepository.findTeamAnalytics called with companyId: {}, performanceCycleId: {}, assessmentMatrixId: {}, teamId: {}", companyId, performanceCycleId, assessmentMatrixId, teamId);

    String companyPerformanceCycleId = companyId + "#" + performanceCycleId;
    String assessmentMatrixScopeId = assessmentMatrixId + "#" + AnalyticsScope.TEAM.name() + "#" + teamId;

    return findById(companyPerformanceCycleId, assessmentMatrixScopeId);
  }

  /**
   * Find all analytics for a company using GSI
   */
  public List<DashboardAnalytics> findByCompany(String companyId) {
    log.info("DashboardAnalyticsRepository.findByCompany called with companyId: {}", companyId);

    QueryConditional queryConditional = QueryConditional.keyEqualTo(
                                                                    Key.builder().partitionValue(companyId).build()
    );

    QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder().queryConditional(queryConditional).build();

    return getTable().index("company-cycle-index")
                     .query(queryRequest)
                     .stream()
                     .flatMap(page -> page.items().stream())
                     .collect(Collectors.toList());
  }

  /**
   * Find by composite primary key
   */
  public Optional<DashboardAnalytics> findById(String companyPerformanceCycleId, String assessmentMatrixScopeId) {
    Key key = Key.builder().partitionValue(companyPerformanceCycleId).sortValue(assessmentMatrixScopeId).build();

    DashboardAnalytics item = getTable().getItem(key);
    return Optional.ofNullable(item);
  }

  public void deleteById(String companyPerformanceCycleId, String assessmentMatrixScopeId) {
    Key key = Key.builder().partitionValue(companyPerformanceCycleId).sortValue(assessmentMatrixScopeId).build();

    getTable().deleteItem(key);
    log.info("Deleted analytics: {}/{}", companyPerformanceCycleId, assessmentMatrixScopeId);
  }
}
package com.agilecheckup.persistency.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.google.common.annotations.VisibleForTesting;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class EmployeeAssessmentRepository extends AbstractCrudRepository<EmployeeAssessment> {

  @Inject
  public EmployeeAssessmentRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
    super(dynamoDbEnhancedClient, EmployeeAssessment.class, "EmployeeAssessment");
  }

  @VisibleForTesting
  public EmployeeAssessmentRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient, String tableName) {
    super(dynamoDbEnhancedClient, EmployeeAssessment.class, tableName);
  }

  /**
   * Check if employee assessment already exists for the given assessment matrix and employee email.
   * Uses GSI for efficient querying instead of scanning the entire table.
   * 
   * @param assessmentMatrixId The assessment matrix ID
   * @param employeeEmail      The employee email (will be normalized to lowercase)
   * @return true if employee assessment exists, false otherwise
   */
  public boolean existsByAssessmentMatrixAndEmployeeEmail(String assessmentMatrixId, String employeeEmail) {
    String normalizedEmail = employeeEmail.toLowerCase().trim();

    try {
      DynamoDbIndex<EmployeeAssessment> gsi = getTable().index("assessmentMatrixId-employeeEmail-index");

      QueryConditional queryConditional = QueryConditional.keyEqualTo(
                                                                      Key.builder()
                                                                         .partitionValue(assessmentMatrixId)
                                                                         .sortValue(normalizedEmail)
                                                                         .build()
      );

      QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                                                              .queryConditional(queryConditional)
                                                              .limit(1) // We only need to know if any exists
                                                              .build();

      List<EmployeeAssessment> results = gsi.query(queryRequest)
                                            .stream()
                                            .flatMap(page -> page.items().stream())
                                            .limit(1)
                                            .collect(java.util.stream.Collectors.toList());

      return !results.isEmpty();

    }
    catch (Exception e) {
      // If GSI doesn't exist or there's a configuration error, fail fast
      throw new RuntimeException("GSI query failed. Please check if GSI 'assessmentMatrixId-employeeEmail-index' exists and is Active.", e);
    }
  }

  /**
   * Find all employee assessments by tenant ID using GSI.
   * 
   * @param tenantId The tenant ID
   * @return List of employee assessments for the tenant
   */
  public List<EmployeeAssessment> findAllByTenantId(String tenantId) {
    return queryBySecondaryIndex("tenantId-index", "tenantId", tenantId);
  }

  /**
   * Find employee assessments by assessment matrix ID and tenant ID using GSI.
   * This method provides efficient querying for dashboard and analytics features.
   * 
   * @param assessmentMatrixId The assessment matrix ID
   * @param tenantId           The tenant ID for additional filtering
   * @return List of employee assessments for the matrix
   */
  public List<EmployeeAssessment> findByAssessmentMatrixId(String assessmentMatrixId, String tenantId) {
    try {
      DynamoDbIndex<EmployeeAssessment> gsi = getTable().index("assessmentMatrixId-employeeEmail-index");

      QueryConditional queryConditional = QueryConditional.keyEqualTo(
                                                                      Key.builder()
                                                                         .partitionValue(assessmentMatrixId)
                                                                         .build()
      );

      // Add tenant filter for security
      Map<String, AttributeValue> expressionValues = new HashMap<>();
      expressionValues.put(":tenantId", AttributeValue.builder().s(tenantId).build());

      Expression filterExpression = Expression.builder()
                                              .expression("tenantId = :tenantId")
                                              .expressionValues(expressionValues)
                                              .build();

      QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                                                              .queryConditional(queryConditional)
                                                              .filterExpression(filterExpression)
                                                              .build();

      return gsi.query(queryRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(java.util.stream.Collectors.toList());
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to query employee assessments by matrix ID: " + assessmentMatrixId, e);
    }
  }

  /**
   * Check if all employee assessments for a given assessment matrix and tenant are completed.
   * This is a low-latency, low-cost method that stops as soon as it finds any non-completed assessment.
   * 
   * @param assessmentMatrixId The assessment matrix ID
   * @param tenantId           The tenant ID for additional filtering
   * @return true if all assessments are completed, false if any are not completed or if no assessments exist
   */
  public boolean areAllAssessmentsCompleted(String assessmentMatrixId, String tenantId) {
    try {
      DynamoDbIndex<EmployeeAssessment> gsi = getTable().index("assessmentMatrixId-employeeEmail-index");

      QueryConditional queryConditional = QueryConditional.keyEqualTo(
                                                                      Key.builder()
                                                                         .partitionValue(assessmentMatrixId)
                                                                         .build()
      );

      // Filter for tenant and non-completed assessments
      Map<String, AttributeValue> expressionValues = new HashMap<>();
      expressionValues.put(":tenantId", AttributeValue.builder().s(tenantId).build());
      expressionValues.put(":completedStatus", AttributeValue.builder().s("COMPLETED").build());

      Expression filterExpression = Expression.builder()
                                              .expression("tenantId = :tenantId AND #status <> :completedStatus")
                                              .expressionValues(expressionValues)
                                              .putExpressionName("#status", "status")
                                              .build();

      QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                                                              .queryConditional(queryConditional)
                                                              .filterExpression(filterExpression)
                                                              .limit(1) // Stop as soon as we find one non-completed
                                                              .build();

      // If we find any non-completed assessment, return false
      boolean hasNonCompleted = gsi.query(queryRequest)
                                   .stream()
                                   .flatMap(page -> page.items().stream())
                                   .findFirst()
                                   .isPresent();

      if (hasNonCompleted) {
        return false;
      }

      // Now check if there are any assessments at all for this matrix/tenant
      return hasAnyAssessments(assessmentMatrixId, tenantId);

    }
    catch (Exception e) {
      throw new RuntimeException("Failed to check completion status for matrix ID: " + assessmentMatrixId, e);
    }
  }

  /**
   * Check if there are any assessments for the given matrix and tenant.
   * Used internally to distinguish between "all completed" and "no assessments exist".
   * 
   * @param assessmentMatrixId The assessment matrix ID
   * @param tenantId           The tenant ID
   * @return true if at least one assessment exists, false otherwise
   */
  private boolean hasAnyAssessments(String assessmentMatrixId, String tenantId) {
    try {
      DynamoDbIndex<EmployeeAssessment> gsi = getTable().index("assessmentMatrixId-employeeEmail-index");

      QueryConditional queryConditional = QueryConditional.keyEqualTo(
                                                                      Key.builder()
                                                                         .partitionValue(assessmentMatrixId)
                                                                         .build()
      );

      Map<String, AttributeValue> expressionValues = new HashMap<>();
      expressionValues.put(":tenantId", AttributeValue.builder().s(tenantId).build());

      Expression filterExpression = Expression.builder()
                                              .expression("tenantId = :tenantId")
                                              .expressionValues(expressionValues)
                                              .build();

      QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                                                              .queryConditional(queryConditional)
                                                              .filterExpression(filterExpression)
                                                              .limit(1)
                                                              .build();

      return gsi.query(queryRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst()
                .isPresent();
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to check if assessments exist for matrix ID: " + assessmentMatrixId, e);
    }
  }

  /**
   * Count the number of non-completed assessments for a given matrix and tenant.
   * This method can be used for progress tracking and dashboard metrics.
   * 
   * @param assessmentMatrixId The assessment matrix ID
   * @param tenantId           The tenant ID
   * @return count of non-completed assessments
   */
  public long countNonCompletedAssessments(String assessmentMatrixId, String tenantId) {
    try {
      DynamoDbIndex<EmployeeAssessment> gsi = getTable().index("assessmentMatrixId-employeeEmail-index");

      QueryConditional queryConditional = QueryConditional.keyEqualTo(
                                                                      Key.builder()
                                                                         .partitionValue(assessmentMatrixId)
                                                                         .build()
      );

      Map<String, AttributeValue> expressionValues = new HashMap<>();
      expressionValues.put(":tenantId", AttributeValue.builder().s(tenantId).build());
      expressionValues.put(":completedStatus", AttributeValue.builder().s("COMPLETED").build());

      Expression filterExpression = Expression.builder()
                                              .expression("tenantId = :tenantId AND #status <> :completedStatus")
                                              .expressionValues(expressionValues)
                                              .putExpressionName("#status", "status")
                                              .build();

      QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                                                              .queryConditional(queryConditional)
                                                              .filterExpression(filterExpression)
                                                              .build();

      return gsi.query(queryRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .count();
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to count non-completed assessments for matrix ID: " + assessmentMatrixId, e);
    }
  }
}
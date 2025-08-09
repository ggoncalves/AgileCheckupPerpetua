package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param employeeEmail The employee email (will be normalized to lowercase)
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
            
        } catch (Exception e) {
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
     * @param tenantId The tenant ID for additional filtering
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to query employee assessments by matrix ID: " + assessmentMatrixId, e);
        }
    }
}
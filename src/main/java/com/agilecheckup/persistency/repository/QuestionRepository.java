package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.question.Question;
import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuestionRepository extends AbstractCrudRepository<Question> {

    @Inject
    public QuestionRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        super(dynamoDbEnhancedClient, Question.class, "Question");
    }

    @VisibleForTesting
    public QuestionRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient, String tableName) {
        super(dynamoDbEnhancedClient, Question.class, tableName);
    }

    /**
     * Find questions by assessment matrix ID and tenant ID.
     * Uses scan with filter expression to maintain compatibility with V1 behavior.
     * 
     * @param matrixId The assessment matrix ID
     * @param tenantId The tenant ID
     * @return List of questions matching the criteria
     */
    public List<Question> findByAssessmentMatrixId(String matrixId, String tenantId) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":val1", AttributeValue.builder().s(matrixId).build());
        expressionValues.put(":val2", AttributeValue.builder().s(tenantId).build());

        Expression filterExpression = Expression.builder()
                .expression("assessmentMatrixId = :val1 AND tenantId = :val2")
                .expressionValues(expressionValues)
                .build();

        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(filterExpression)
                .build();

        return getTable().scan(scanRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    /**
     * Check if questions exist for the given assessment matrix, category, and tenant.
     * Uses scan with filter expression and limit for efficient existence check.
     * 
     * @param matrixId The assessment matrix ID
     * @param categoryId The category ID
     * @param tenantId The tenant ID
     * @return true if at least one question exists, false otherwise
     */
    public boolean existsByCategoryId(String matrixId, String categoryId, String tenantId) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":val1", AttributeValue.builder().s(matrixId).build());
        expressionValues.put(":val2", AttributeValue.builder().s(categoryId).build());
        expressionValues.put(":val3", AttributeValue.builder().s(tenantId).build());

        Expression filterExpression = Expression.builder()
                .expression("assessmentMatrixId = :val1 AND categoryId = :val2 AND tenantId = :val3")
                .expressionValues(expressionValues)
                .build();

        // Scan with limit 1 - we only need to know if at least one exists
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(filterExpression)
                .limit(1)
                .build();

        List<Question> results = getTable().scan(scanRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .limit(1)
                .collect(Collectors.toList());
        
        return !results.isEmpty();
    }
}
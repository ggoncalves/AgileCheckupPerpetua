package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.question.Question;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionRepository extends AbstractCrudRepository<Question> {

  @Inject
  public QuestionRepository() {
    super(Question.class);
  }

  @VisibleForTesting
  public QuestionRepository(DynamoDBMapper dynamoDBMapper) {
    super(Question.class, dynamoDBMapper);
  }

  public List<Question> findByAssessmentMatrixId(String matrixId, String tenantId) {
    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":val1", new AttributeValue().withS(matrixId));
    eav.put(":val2", new AttributeValue().withS(tenantId));

    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
        .withFilterExpression("assessmentMatrixId = :val1 and tenantId = :val2")
        .withExpressionAttributeValues(eav);

    return getDynamoDBMapper().scan(Question.class, scanExpression);
  }

  public boolean existsByCategoryId(String matrixId, String categoryId, String tenantId) {
    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":val1", new AttributeValue().withS(matrixId));
    eav.put(":val2", new AttributeValue().withS(categoryId));
    eav.put(":val3", new AttributeValue().withS(tenantId));

    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
        .withFilterExpression("assessmentMatrixId = :val1 and categoryId = :val2 and tenantId = :val3")
        .withExpressionAttributeValues(eav)
        .withLimit(1); // We only need to know if at least one exists

    List<Question> results = getDynamoDBMapper().scan(Question.class, scanExpression);
    return !results.isEmpty();
  }
}
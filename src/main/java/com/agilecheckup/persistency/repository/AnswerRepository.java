package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnswerRepository extends AbstractCrudRepository<Answer> {

  @Inject
  public AnswerRepository() {
    super(Answer.class);
  }

  @VisibleForTesting
  public AnswerRepository(DynamoDBMapper dynamoDBMapper) {
    super(Answer.class, dynamoDBMapper);
  }

  public List<Answer> findByEmployeeAssessmentId(String employeeAssessmentId, String tenantId) {
    // TODO: Generify in super abstract class. scanByEntityIdAndTenant
    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":val1", new AttributeValue().withS(employeeAssessmentId));
    eav.put(":val2", new AttributeValue().withS(tenantId));

    DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
        .withFilterExpression("employeeAssessmentId = :val1 and tenantId = :val2")
        .withExpressionAttributeValues(eav);

    List<Answer> answers = getDynamoDBMapper().scan(Answer.class, scanExpression);
    answers.stream()
        .filter(answer -> answer.getQuestion() == null)
        .forEach(answer -> {
          answer.setQuestion(getDynamoDBMapper().load(Question.class, answer.getQuestionId()));
          save(answer);
        });

    return answers;
  }
}
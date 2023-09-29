package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.question.Answer;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;

public class AnswerRepository extends AbstractCrudRepository<Answer> {

  @Inject
  public AnswerRepository() {
    super(Answer.class);
  }

  @VisibleForTesting
  public AnswerRepository(DynamoDBMapper dynamoDBMapper) {
    super(Answer.class, dynamoDBMapper);
  }
}
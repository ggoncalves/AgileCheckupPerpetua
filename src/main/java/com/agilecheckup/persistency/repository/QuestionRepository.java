package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.Question;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;

public class QuestionRepository extends AbstractCrudRepository<Question> {

  @Inject
  public QuestionRepository() {
    super(Question.class);
  }

  @VisibleForTesting
  public QuestionRepository(DynamoDBMapper dynamoDBMapper) {
    super(Question.class, dynamoDBMapper);
  }

}

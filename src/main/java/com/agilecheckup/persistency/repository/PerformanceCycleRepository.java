package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;

public class PerformanceCycleRepository extends AbstractCrudRepository<PerformanceCycle> {

  @Inject
  public PerformanceCycleRepository() {
    super(PerformanceCycle.class);
  }

  @VisibleForTesting
  public PerformanceCycleRepository(DynamoDBMapper dynamoDBMapper) {
    super(PerformanceCycle.class, dynamoDBMapper);
  }

}

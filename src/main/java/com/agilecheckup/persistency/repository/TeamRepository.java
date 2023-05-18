package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.Team;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;

public class TeamRepository extends AbstractCrudRepository<Team> {

  @Inject
  public TeamRepository() {
    super(Team.class);
  }

  @VisibleForTesting
  public TeamRepository(DynamoDBMapper dynamoDBMapper) {
    super(Team.class, dynamoDBMapper);
  }

}

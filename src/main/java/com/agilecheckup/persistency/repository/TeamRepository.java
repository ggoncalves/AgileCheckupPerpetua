package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.Team;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamRepository extends AbstractCrudRepository<Team> {

  @Inject
  public TeamRepository() {
    super(Team.class);
  }

  @VisibleForTesting
  public TeamRepository(DynamoDBMapper dynamoDBMapper) {
    super(Team.class, dynamoDBMapper);
  }

  public List<Team> findByDepartmentId(String departmentId, String tenantId) {
    // Create expression attribute values
    Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
    expressionAttributeValues.put(":departmentId", new AttributeValue().withS(departmentId));
    expressionAttributeValues.put(":tenantId", new AttributeValue().withS(tenantId));
    
    // Create DynamoDB query expression for GSI
    DynamoDBQueryExpression<Team> queryExpression = new DynamoDBQueryExpression<Team>()
        .withIndexName("departmentId-index")
        .withConsistentRead(false) // GSI queries cannot use consistent read
        .withKeyConditionExpression("departmentId = :departmentId")
        .withFilterExpression("tenantId = :tenantId") // Filter by tenant for multi-tenancy
        .withExpressionAttributeValues(expressionAttributeValues);
    
    // Execute query on GSI
    PaginatedQueryList<Team> result = getDynamoDBMapper().query(Team.class, queryExpression);
    return result;
  }

}

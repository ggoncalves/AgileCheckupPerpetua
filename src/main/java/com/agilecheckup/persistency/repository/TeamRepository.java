package com.agilecheckup.persistency.repository;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.agilecheckup.persistency.entity.Team;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@Singleton
public class TeamRepository extends AbstractCrudRepository<Team> {

  @Inject
  public TeamRepository(DynamoDbEnhancedClient enhancedClient) {
    super(enhancedClient, Team.class, "Team");
  }

  public List<Team> findByDepartmentId(String departmentId) {
    QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder().queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(departmentId))).build();

    return getTable().index("departmentId-index").query(queryRequest).stream().flatMap(page -> page.items().stream()).collect(Collectors.toList());
  }

  public List<Team> findAllByTenantId(String tenantId) {
    return queryBySecondaryIndex("tenantId-index", "tenantId", tenantId);
  }
}
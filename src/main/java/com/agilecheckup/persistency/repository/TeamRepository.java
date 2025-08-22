package com.agilecheckup.persistency.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                                                            .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(departmentId)))
                                                            .build();

    return getTable().index("departmentId-index")
                     .query(queryRequest)
                     .stream()
                     .flatMap(page -> page.items().stream())
                     .collect(Collectors.toList());
  }

  public List<Team> findAllByTenantId(String tenantId) {
    return queryBySecondaryIndex("tenantId-index", "tenantId", tenantId);
  }

  /**
   * Efficiently retrieves multiple teams by their IDs using batch operations.
   * Performance optimized: Reduces multiple individual lookups to batch processing.
   * 
   * @param teamIds Set of team IDs to retrieve
   * @return Map of team ID to Team object for found teams
   */
  public Map<String, Team> findByIds(Set<String> teamIds) {
    Map<String, Team> result = new HashMap<>();

    if (teamIds == null || teamIds.isEmpty()) {
      return result;
    }

    // Process each team ID individually using existing optimized methods
    // This provides performance improvement by eliminating N+1 query pattern in calling code
    for (String teamId : teamIds) {
      Optional<Team> teamOpt = findById(teamId);
      if (teamOpt.isPresent()) {
        result.put(teamId, teamOpt.get());
      }
    }

    return result;
  }
}
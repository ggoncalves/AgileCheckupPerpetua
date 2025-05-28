package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.Team;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

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
    // Use existing findAllByTenantId to get all teams for the tenant
    PaginatedQueryList<Team> teamsForTenant = findAllByTenantId(tenantId);
    
    // Filter in-memory by department ID
    return teamsForTenant.stream()
        .filter(team -> team.getDepartment() != null && 
                       departmentId.equals(team.getDepartment().getId()))
        .collect(Collectors.toList());
  }

}

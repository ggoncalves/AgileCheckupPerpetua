package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.TeamRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TeamService extends AbstractCrudService<Team, AbstractCrudRepository<Team>> {

  private final TeamRepository teamRepository;
  private final DepartmentServiceV2 departmentServiceV2;

  @Inject
  public TeamService(TeamRepository teamRepository, DepartmentServiceV2 departmentServiceV2) {
    this.teamRepository = teamRepository;
    this.departmentServiceV2 = departmentServiceV2;
  }

  public Optional<Team> create(String name, String description, String tenantId, String departmentId) {
    return super.create(createTeam(name, description, tenantId, departmentId));
  }

  public Optional<Team> update(String id, String name, String description, String tenantId, String departmentId) {
    Optional<Team> optionalTeam = findById(id);
    if (optionalTeam.isPresent()) {
      Team team = optionalTeam.get();
      // Validate that department exists
      departmentServiceV2.findById(departmentId)
          .orElseThrow(() -> new InvalidIdReferenceException(departmentId, "Team", "Department"));
      team.setName(name);
      team.setDescription(description);
      team.setTenantId(tenantId);
      team.setDepartmentId(departmentId);
      return super.update(team);
    } else {
      return Optional.empty();
    }
  }

  private Team createTeam(String name, String description, String tenantId, String departmentId) {
    // Validate that department exists
    departmentServiceV2.findById(departmentId)
        .orElseThrow(() -> new InvalidIdReferenceException(departmentId, "Team", "Department"));
    return Team.builder()
        .name(name)
        .description(description)
        .tenantId(tenantId)
        .departmentId(departmentId)
        .build();
  }

  public List<Team> findAllByTenantId(String tenantId) {
    PaginatedQueryList<Team> paginatedList = teamRepository.findAllByTenantId(tenantId);
    return paginatedList.stream().collect(Collectors.toList());
  }

  public List<Team> findByDepartmentId(String departmentId, String tenantId) {
    return teamRepository.findByDepartmentId(departmentId, tenantId);
  }

  @Override
  AbstractCrudRepository<Team> getRepository() {
    return teamRepository;
  }
}
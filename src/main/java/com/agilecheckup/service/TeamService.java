package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.repository.TeamRepository;
import com.agilecheckup.service.exception.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Log4j2
@Singleton
public class TeamService extends AbstractCrudService<Team, TeamRepository> {

    private final TeamRepository teamRepository;

    @Inject
    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    TeamRepository getRepository() {
        return teamRepository;
    }

    public Optional<Team> create(String tenantId, String name, String description, String departmentId) {
        Team team = Team.builder()
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .departmentId(departmentId)
                .build();
        
        return create(team);
    }

    public Optional<Team> update(String id, String tenantId, String name, String description, String departmentId) {
        Team existing = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + id));
        
        existing.setTenantId(tenantId);
        existing.setName(name);
        existing.setDescription(description);
        existing.setDepartmentId(departmentId);
        
        return update(existing);
    }

    public List<Team> findByDepartmentId(String departmentId) {
        return teamRepository.findByDepartmentId(departmentId);
    }

    public List<Team> findAllByTenantId(String tenantId) {
        return teamRepository.findAllByTenantId(tenantId);
    }
}
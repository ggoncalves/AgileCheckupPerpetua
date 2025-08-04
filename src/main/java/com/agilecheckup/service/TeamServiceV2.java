package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.TeamV2;
import com.agilecheckup.persistency.repository.TeamRepositoryV2;
import com.agilecheckup.service.exception.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Log4j2
@Singleton
public class TeamServiceV2 extends AbstractCrudServiceV2<TeamV2, TeamRepositoryV2> {

    private final TeamRepositoryV2 teamRepository;

    @Inject
    public TeamServiceV2(TeamRepositoryV2 teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    TeamRepositoryV2 getRepository() {
        return teamRepository;
    }

    public Optional<TeamV2> create(String tenantId, String name, String description, String departmentId) {
        TeamV2 team = TeamV2.builder()
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .departmentId(departmentId)
                .build();
        
        return create(team);
    }

    public Optional<TeamV2> update(String id, String tenantId, String name, String description, String departmentId) {
        TeamV2 existing = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with id: " + id));
        
        existing.setTenantId(tenantId);
        existing.setName(name);
        existing.setDescription(description);
        existing.setDepartmentId(departmentId);
        
        return update(existing);
    }

    public List<TeamV2> findByDepartmentId(String departmentId) {
        return teamRepository.findByDepartmentId(departmentId);
    }

    public List<TeamV2> findAllByTenantId(String tenantId) {
        return teamRepository.findAllByTenantId(tenantId);
    }
}
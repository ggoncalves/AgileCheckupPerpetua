package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.TeamV2;
import com.agilecheckup.persistency.repository.TeamRepositoryV2;
import com.agilecheckup.service.exception.EntityNotFoundException;
import com.agilecheckup.util.TestObjectFactoryV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceV2Test {

    @Mock
    private TeamRepositoryV2 teamRepository;

    private TeamServiceV2 teamServiceV2;

    @BeforeEach
    void setUp() {
        teamServiceV2 = new TeamServiceV2(teamRepository);
    }

    @Test
    void testCreateTeam() {
        // Given
        String tenantId = "tenant-123";
        String name = "Engineering Team";
        String description = "Core engineering team";
        String departmentId = "dept-456";

        TeamV2 savedTeam = TestObjectFactoryV2.createMockedTeamV2(name, description, tenantId, departmentId);
        savedTeam.setId("team-789");

        when(teamRepository.save(any(TeamV2.class))).thenReturn(Optional.of(savedTeam));

        // When
        Optional<TeamV2> result = teamServiceV2.create(tenantId, name, description, departmentId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(name);
        assertThat(result.get().getDescription()).isEqualTo(description);
        assertThat(result.get().getTenantId()).isEqualTo(tenantId);
        assertThat(result.get().getDepartmentId()).isEqualTo(departmentId);
        verify(teamRepository).save(any(TeamV2.class));
    }

    @Test
    void testUpdateTeamSuccess() {
        // Given
        String teamId = "team-123";
        String tenantId = "tenant-456";
        String name = "Updated Team";
        String description = "Updated description";
        String departmentId = "dept-789";

        TeamV2 existingTeam = TestObjectFactoryV2.createMockedTeamV2(teamId);
        TeamV2 updatedTeam = TestObjectFactoryV2.createMockedTeamV2(name, description, tenantId, departmentId);
        updatedTeam.setId(teamId);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.save(any(TeamV2.class))).thenReturn(Optional.of(updatedTeam));

        // When
        Optional<TeamV2> result = teamServiceV2.update(teamId, tenantId, name, description, departmentId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(name);
        assertThat(result.get().getDescription()).isEqualTo(description);
        assertThat(result.get().getTenantId()).isEqualTo(tenantId);
        assertThat(result.get().getDepartmentId()).isEqualTo(departmentId);
        verify(teamRepository).findById(teamId);
        verify(teamRepository).save(any(TeamV2.class));
    }

    @Test
    void testUpdateTeamNotFound() {
        // Given
        String teamId = "nonexistent-team";
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teamServiceV2.update(teamId, "tenant", "name", "desc", "dept"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Team not found with id: nonexistent-team");

        verify(teamRepository).findById(teamId);
        verify(teamRepository, never()).save(any(TeamV2.class));
    }

    @Test
    void testFindByDepartmentId() {
        // Given
        String departmentId = "dept-123";
        TeamV2 team1 = TestObjectFactoryV2.createMockedTeamV2("team-1");
        TeamV2 team2 = TestObjectFactoryV2.createMockedTeamV2("team-2");
        List<TeamV2> expectedTeams = Arrays.asList(team1, team2);

        when(teamRepository.findByDepartmentId(departmentId)).thenReturn(expectedTeams);

        // When
        List<TeamV2> result = teamServiceV2.findByDepartmentId(departmentId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(team1, team2);
        verify(teamRepository).findByDepartmentId(departmentId);
    }

    @Test
    void testFindAllByTenantId() {
        // Given
        String tenantId = "tenant-123";
        TeamV2 team1 = TestObjectFactoryV2.createMockedTeamV2("team-1");
        TeamV2 team2 = TestObjectFactoryV2.createMockedTeamV2("team-2");
        List<TeamV2> expectedTeams = Arrays.asList(team1, team2);

        when(teamRepository.findAllByTenantId(tenantId)).thenReturn(expectedTeams);

        // When
        List<TeamV2> result = teamServiceV2.findAllByTenantId(tenantId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(team1, team2);
        verify(teamRepository).findAllByTenantId(tenantId);
    }

    @Test
    void testFindById() {
        // Given
        String teamId = "team-123";
        TeamV2 expectedTeam = TestObjectFactoryV2.createMockedTeamV2(teamId);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(expectedTeam));

        // When
        Optional<TeamV2> result = teamServiceV2.findById(teamId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedTeam);
        verify(teamRepository).findById(teamId);
    }

    @Test
    void testDeleteById() {
        // Given
        String teamId = "team-123";
        when(teamRepository.deleteById(teamId)).thenReturn(true);

        // When
        boolean result = teamServiceV2.deleteById(teamId);

        // Then
        assertThat(result).isTrue();
        verify(teamRepository).deleteById(teamId);
    }
}
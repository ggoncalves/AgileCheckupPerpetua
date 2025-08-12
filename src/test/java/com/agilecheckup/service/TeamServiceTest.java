package com.agilecheckup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.repository.TeamRepository;
import com.agilecheckup.service.exception.EntityNotFoundException;
import com.agilecheckup.util.TestObjectFactory;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

  @Mock
  private TeamRepository teamRepository;

  private TeamService teamService;

  @BeforeEach
  void setUp() {
    teamService = new TeamService(teamRepository);
  }

  @Test
  void testCreateTeam() {
    // Given
    String tenantId = "tenant-123";
    String name = "Engineering Team";
    String description = "Core engineering team";
    String departmentId = "dept-456";

    Team savedTeam = TestObjectFactory.createMockedTeam(name, description, tenantId, departmentId);
    savedTeam.setId("team-789");

    when(teamRepository.save(any(Team.class))).thenReturn(Optional.of(savedTeam));

    // When
    Optional<Team> result = teamService.create(tenantId, name, description, departmentId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo(name);
    assertThat(result.get().getDescription()).isEqualTo(description);
    assertThat(result.get().getTenantId()).isEqualTo(tenantId);
    assertThat(result.get().getDepartmentId()).isEqualTo(departmentId);
    verify(teamRepository).save(any(Team.class));
  }

  @Test
  void testUpdateTeamSuccess() {
    // Given
    String teamId = "team-123";
    String tenantId = "tenant-456";
    String name = "Updated Team";
    String description = "Updated description";
    String departmentId = "dept-789";

    Team existingTeam = TestObjectFactory.createMockedTeam(teamId);
    Team updatedTeam = TestObjectFactory.createMockedTeam(name, description, tenantId, departmentId);
    updatedTeam.setId(teamId);

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam));
    when(teamRepository.save(any(Team.class))).thenReturn(Optional.of(updatedTeam));

    // When
    Optional<Team> result = teamService.update(teamId, tenantId, name, description, departmentId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo(name);
    assertThat(result.get().getDescription()).isEqualTo(description);
    assertThat(result.get().getTenantId()).isEqualTo(tenantId);
    assertThat(result.get().getDepartmentId()).isEqualTo(departmentId);
    verify(teamRepository).findById(teamId);
    verify(teamRepository).save(any(Team.class));
  }

  @Test
  void testUpdateTeamNotFound() {
    // Given
    String teamId = "nonexistent-team";
    when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> teamService.update(teamId, "tenant", "name", "desc", "dept")).isInstanceOf(EntityNotFoundException.class).hasMessage("Team not found with id: nonexistent-team");

    verify(teamRepository).findById(teamId);
    verify(teamRepository, never()).save(any(Team.class));
  }

  @Test
  void testFindByDepartmentId() {
    // Given
    String departmentId = "dept-123";
    Team team1 = TestObjectFactory.createMockedTeam("team-1");
    Team team2 = TestObjectFactory.createMockedTeam("team-2");
    List<Team> expectedTeams = Arrays.asList(team1, team2);

    when(teamRepository.findByDepartmentId(departmentId)).thenReturn(expectedTeams);

    // When
    List<Team> result = teamService.findByDepartmentId(departmentId);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(team1, team2);
    verify(teamRepository).findByDepartmentId(departmentId);
  }

  @Test
  void testFindAllByTenantId() {
    // Given
    String tenantId = "tenant-123";
    Team team1 = TestObjectFactory.createMockedTeam("team-1");
    Team team2 = TestObjectFactory.createMockedTeam("team-2");
    List<Team> expectedTeams = Arrays.asList(team1, team2);

    when(teamRepository.findAllByTenantId(tenantId)).thenReturn(expectedTeams);

    // When
    List<Team> result = teamService.findAllByTenantId(tenantId);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(team1, team2);
    verify(teamRepository).findAllByTenantId(tenantId);
  }

  @Test
  void testFindById() {
    // Given
    String teamId = "team-123";
    Team expectedTeam = TestObjectFactory.createMockedTeam(teamId);

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(expectedTeam));

    // When
    Optional<Team> result = teamService.findById(teamId);

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
    boolean result = teamService.deleteById(teamId);

    // Then
    assertThat(result).isTrue();
    verify(teamRepository).deleteById(teamId);
  }
}
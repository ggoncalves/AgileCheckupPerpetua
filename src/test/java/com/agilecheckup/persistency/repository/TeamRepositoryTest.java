package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.Team;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static com.agilecheckup.util.TestObjectFactory.createMockedTeam;
import static com.agilecheckup.util.TestObjectFactory.createMockedTeamWithDependenciesId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamRepositoryTest extends AbstractRepositoryTest<Team> {

  @InjectMocks
  @Spy
  private TeamRepository teamRepository;

  @Test
  void findByDepartmentId_shouldFilterByDepartment() {
    // Given
    String tenantId = "tenant-123";
    String departmentId = "dept-123";
    String otherDepartmentId = "dept-456";
    
    Team team1 = createMockedTeamWithDependenciesId(departmentId);
    team1.setTenantId(tenantId);
    
    Team team2 = createMockedTeamWithDependenciesId(departmentId);
    team2.setTenantId(tenantId);
    
    Team team3 = createMockedTeamWithDependenciesId(otherDepartmentId);
    team3.setTenantId(tenantId);
    
    PaginatedQueryList<Team> mockPaginatedList = mock(PaginatedQueryList.class);
    when(mockPaginatedList.stream()).thenReturn(Arrays.asList(team1, team2, team3).stream());
    
    doReturn(mockPaginatedList).when(teamRepository).findAllByTenantId(tenantId);
    
    // When
    List<Team> result = teamRepository.findByDepartmentId(departmentId, tenantId);
    
    // Then
    assertEquals(2, result.size());
    assertTrue(result.contains(team1));
    assertTrue(result.contains(team2));
  }

  @Test
  void findByDepartmentId_shouldReturnEmptyListWhenNoDepartmentMatches() {
    // Given
    String tenantId = "tenant-123";
    String departmentId = "dept-123";
    String otherDepartmentId = "dept-456";
    
    Team team1 = createMockedTeamWithDependenciesId(otherDepartmentId);
    team1.setTenantId(tenantId);
    
    PaginatedQueryList<Team> mockPaginatedList = mock(PaginatedQueryList.class);
    when(mockPaginatedList.stream()).thenReturn(Arrays.asList(team1).stream());
    
    doReturn(mockPaginatedList).when(teamRepository).findAllByTenantId(tenantId);
    
    // When
    List<Team> result = teamRepository.findByDepartmentId(departmentId, tenantId);
    
    // Then
    assertTrue(result.isEmpty());
  }

  @Override
  AbstractCrudRepository getRepository() {
    return teamRepository;
  }

  @Override
  Team createMockedT() {
    return createMockedTeam();
  }

  @Override
  Class getMockedClass() {
    return Team.class;
  }
}
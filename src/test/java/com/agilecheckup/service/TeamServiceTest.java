package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Department;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.TeamRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.cloneWithId;
import static com.agilecheckup.util.TestObjectFactory.copyTeamAndAddId;
import static com.agilecheckup.util.TestObjectFactory.createMockedDepartment;
import static com.agilecheckup.util.TestObjectFactory.createMockedTeamWithDependenciesId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest extends AbstractCrudServiceTest<Team, AbstractCrudRepository<Team>> {

  @InjectMocks
  @Spy
  private TeamService teamService;

  @Mock
  private TeamRepository mockedTeamRepository;


  @Mock
  private DepartmentServiceV2 mockDepartmentServiceV2;

  private Team originalTeam;

  private final Department department = createMockedDepartment(GENERIC_ID_1234);

  @BeforeEach
  void setUpBefore() {
    originalTeam = createMockedTeamWithDependenciesId(GENERIC_ID_1234);
    originalTeam = cloneWithId(originalTeam, DEFAULT_ID);
  }

  @Test
  void create() {
    Team savedTeam = copyTeamAndAddId(originalTeam, DEFAULT_ID);

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedTeam, mockedTeamRepository);
    doReturn(Optional.of(department)).when(mockDepartmentServiceV2).findById(originalTeam.getDepartmentId());

    // When
    Optional<Team> teamOptional = teamService.create(
        originalTeam.getName(),
        originalTeam.getDescription(),
        originalTeam.getTenantId(),
        originalTeam.getDepartmentId()
    );

    // Then
    assertTrue(teamOptional.isPresent());
    assertEquals(savedTeam, teamOptional.get());
    verify(mockedTeamRepository).save(savedTeam);
    verify(teamService).create(originalTeam.getName(), originalTeam.getDescription(), originalTeam.getTenantId(), originalTeam.getDepartmentId());
  }

  @Test
  void createInvalidCompanyId() {
    // Prevent/Stub
    doReturn(Optional.empty()).when(mockDepartmentServiceV2).findById(originalTeam.getDepartmentId());

    // When
    assertThrows(InvalidIdReferenceException.class, () -> teamService.create(
        originalTeam.getName(),
        originalTeam.getDescription(),
        originalTeam.getTenantId(),
        originalTeam.getDepartmentId()
    ));
  }

  @Test
  void createInvalidDepartmentId() {
    // Prevent/Stub
    doReturn(Optional.empty()).when(mockDepartmentServiceV2).findById(originalTeam.getDepartmentId());

    // When
    assertThrows(InvalidIdReferenceException.class, () -> teamService.create(
        originalTeam.getName(),
        originalTeam.getDescription(),
        originalTeam.getTenantId(),
        originalTeam.getDepartmentId()
    ));
  }

  @Test
  void create_NullTeamName() {
    // Mock department service to return a valid department
    doReturn(Optional.of(department)).when(mockDepartmentServiceV2).findById(originalTeam.getDepartmentId());
    
    // When
    assertThrows(NullPointerException.class, () -> teamService.create(
        null,
        originalTeam.getDescription(),
        originalTeam.getTenantId(),
        originalTeam.getDepartmentId()
    ));
  }

  @Test
  void update_existingTeam_shouldSucceed() {
    // Prepare
    Team existingTeam = createMockedTeamWithDependenciesId(GENERIC_ID_1234);
    existingTeam = cloneWithId(existingTeam, DEFAULT_ID);
    Team updatedTeam = Team.builder()
        .id(DEFAULT_ID)
        .name("Updated Team Name")
        .description("Updated Description")
        .tenantId("Updated Tenant Id")
        .departmentId("updatedDepartmentId")
        .build();

    Department updatedDepartment = createMockedDepartment("updatedDepartmentId");

    // Mock repository calls
    doReturn(existingTeam).when(mockedTeamRepository).findById(DEFAULT_ID);
    doAnswerForUpdate(updatedTeam, mockedTeamRepository);
    doReturn(Optional.of(updatedDepartment)).when(mockDepartmentServiceV2).findById("updatedDepartmentId");

    // When
    Optional<Team> resultOptional = teamService.update(
        DEFAULT_ID,
        updatedTeam.getName(),
        updatedTeam.getDescription(),
        updatedTeam.getTenantId(),
        "updatedDepartmentId"
    );

    // Then
    assertTrue(resultOptional.isPresent());
    assertEquals(updatedTeam, resultOptional.get());
    verify(mockedTeamRepository).findById(DEFAULT_ID);
    verify(mockedTeamRepository).save(updatedTeam);
    verify(mockDepartmentServiceV2).findById("updatedDepartmentId");
    verify(teamService).update(DEFAULT_ID,
        "Updated Team Name",
        "Updated Description",
        "Updated Tenant Id",
        "updatedDepartmentId");
  }

  @Test
  void update_nonExistingTeam_shouldReturnEmpty() {
    // Prepare
    String nonExistingId = "nonExistingId";

    // Mock repository calls
    doReturn(null).when(mockedTeamRepository).findById(nonExistingId);

    // When
    Optional<Team> resultOptional = teamService.update(
        nonExistingId,
        "name",
        "desc",
        "tenant",
        "departmentId"
    );

    // Then
    assertTrue(resultOptional.isEmpty());
    verify(mockedTeamRepository).findById(nonExistingId);
    verify(teamService).update(nonExistingId, "name", "desc", "tenant", "departmentId");
  }

  @Test
  void findAllByTenantId_shouldReturnTeamsForTenant() {
    // Given
    String tenantId = "tenant-123";
    Team team1 = createMockedTeamWithDependenciesId("dept-1");
    team1.setTenantId(tenantId);
    Team team2 = createMockedTeamWithDependenciesId("dept-2");
    team2.setTenantId(tenantId);
    
    PaginatedQueryList mockPaginatedList = mock(PaginatedQueryList.class);
    when(mockPaginatedList.stream()).thenReturn(Stream.of(team1, team2));
    
    doReturn(mockPaginatedList).when(mockedTeamRepository).findAllByTenantId(tenantId);
    
    // When
    List<Team> result = teamService.findAllByTenantId(tenantId);
    
    // Then
    assertEquals(2, result.size());
    assertTrue(result.contains(team1));
    assertTrue(result.contains(team2));
    verify(mockedTeamRepository).findAllByTenantId(tenantId);
  }

  @Test
  void findByDepartmentId_shouldReturnFilteredTeams() {
    // Given
    String tenantId = "tenant-123";
    String departmentId = "dept-123";
    Team team1 = createMockedTeamWithDependenciesId(departmentId);
    team1.setTenantId(tenantId);
    Team team2 = createMockedTeamWithDependenciesId(departmentId);
    team2.setTenantId(tenantId);
    
    List<Team> expectedTeams = Arrays.asList(team1, team2);
    doReturn(expectedTeams).when(mockedTeamRepository).findByDepartmentId(departmentId, tenantId);
    
    // When
    List<Team> result = teamService.findByDepartmentId(departmentId, tenantId);
    
    // Then
    assertEquals(expectedTeams, result);
    verify(mockedTeamRepository).findByDepartmentId(departmentId, tenantId);
  }

  @Test
  void findByDepartmentId_shouldReturnEmptyListWhenNoTeams() {
    // Given
    String tenantId = "tenant-123";
    String departmentId = "dept-123";
    
    doReturn(Collections.emptyList()).when(mockedTeamRepository).findByDepartmentId(departmentId, tenantId);
    
    // When
    List<Team> result = teamService.findByDepartmentId(departmentId, tenantId);
    
    // Then
    assertTrue(result.isEmpty());
    verify(mockedTeamRepository).findByDepartmentId(departmentId, tenantId);
  }
}
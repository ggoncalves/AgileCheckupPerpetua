package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Department;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.TeamRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.cloneWithId;
import static com.agilecheckup.util.TestObjectFactory.copyTeamAndAddId;
import static com.agilecheckup.util.TestObjectFactory.createMockedDepartment;
import static com.agilecheckup.util.TestObjectFactory.createMockedTeamWithDependenciesId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest extends AbstractCrudServiceTest<Team, AbstractCrudRepository<Team>> {

  @InjectMocks
  @Spy
  private TeamService teamService;

  @Mock
  private TeamRepository mockedTeamRepository;


  @Mock
  private DepartmentService mockDepartmentService;

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
    doReturn(Optional.of(department)).when(mockDepartmentService).findById(originalTeam.getDepartment().getId());

    // When
    Optional<Team> teamOptional = teamService.create(
        originalTeam.getName(),
        originalTeam.getDescription(),
        originalTeam.getTenantId(),
        originalTeam.getDepartment().getId()
    );

    // Then
    assertTrue(teamOptional.isPresent());
    assertEquals(savedTeam, teamOptional.get());
    verify(mockedTeamRepository).save(savedTeam);
    verify(teamService).create(originalTeam.getName(), originalTeam.getDescription(), originalTeam.getTenantId(), originalTeam.getDepartment().getId());
  }

  @Test
  void createInvalidCompanyId() {
    // Prevent/Stub
    doReturn(Optional.empty()).when(mockDepartmentService).findById(originalTeam.getDepartment().getId());

    // When
    assertThrows(InvalidIdReferenceException.class, () -> teamService.create(
        originalTeam.getName(),
        originalTeam.getDescription(),
        originalTeam.getTenantId(),
        originalTeam.getDepartment().getId()
    ));
  }

  @Test
  void createInvalidDepartmentId() {
    // Prevent/Stub
    doReturn(Optional.empty()).when(mockDepartmentService).findById(originalTeam.getDepartment().getId());

    // When
    assertThrows(InvalidIdReferenceException.class, () -> teamService.create(
        originalTeam.getName(),
        originalTeam.getDescription(),
        originalTeam.getTenantId(),
        originalTeam.getDepartment().getId()
    ));
  }

  @Test
  void create_NullTeamName() {
    // When
    assertThrows(NullPointerException.class, () -> teamService.create(
        null,
        originalTeam.getDescription(),
        originalTeam.getTenantId(),
        originalTeam.getDepartment().getId()
    ));
  }

  @Test
  void update_existingTeam_shouldSucceed() {
    // Prepare
    Team existingTeam = createMockedTeamWithDependenciesId(GENERIC_ID_1234);
    existingTeam = cloneWithId(existingTeam, DEFAULT_ID);
    Team updatedTeamDetails = createMockedTeamWithDependenciesId("updatedDepartmentId");
    updatedTeamDetails.setName("Updated Team Name");
    updatedTeamDetails.setDescription("Updated Description");
    updatedTeamDetails.setTenantId("Updated Tenant Id");

    Department updatedDepartment = createMockedDepartment("updatedDepartmentId");

    // Mock repository calls
    doReturn(existingTeam).when(mockedTeamRepository).findById(DEFAULT_ID);
    doAnswerForUpdate(updatedTeamDetails, mockedTeamRepository);
    doReturn(Optional.of(updatedDepartment)).when(mockDepartmentService).findById("updatedDepartmentId");

    // When
    Optional<Team> resultOptional = teamService.update(
        DEFAULT_ID,
        updatedTeamDetails.getName(),
        updatedTeamDetails.getDescription(),
        updatedTeamDetails.getTenantId(),
        "updatedDepartmentId"
    );

    // Then
    assertTrue(resultOptional.isPresent());
    assertEquals(updatedTeamDetails, resultOptional.get());
    verify(mockedTeamRepository).findById(DEFAULT_ID);
    verify(mockedTeamRepository).save(updatedTeamDetails);
    verify(mockDepartmentService).findById("updatedDepartmentId");
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
}
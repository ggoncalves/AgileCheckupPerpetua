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

import static com.agilecheckup.util.TestObjectFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    originalTeam = createMockedTeamWithDependenciesId(GENERIC_ID_1234, GENERIC_ID_1234);
    originalTeam = cloneWithId(originalTeam, DEFAULT_ID);
  }

  @Test
  void create() {
    Team savedTeam = copyTeamAndAddId(originalTeam, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedTeam).when(mockedTeamRepository).save(any());
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
    verify(mockedTeamRepository).save(originalTeam);
    verify(teamService).create(originalTeam.getName(), originalTeam.getDescription(), originalTeam.getTenantId(), originalTeam.getDepartment().getId());
  }

  @Test
  void createInvalidCompanyId() {
    Team savedTeam = copyTeamAndAddId(originalTeam, DEFAULT_ID);

    // Prevent/Stub
    doReturn(Optional.empty()).when(mockDepartmentService).findById(originalTeam.getDepartment().getId());

    // When
    assertThrows(InvalidIdReferenceException.class, () -> {
      teamService.create(
          originalTeam.getName(),
          originalTeam.getDescription(),
          originalTeam.getTenantId(),
          originalTeam.getDepartment().getId()
      );
    });
  }

  @Test
  void createInvalidDepartmentId() {
    Team savedTeam = copyTeamAndAddId(originalTeam, DEFAULT_ID);

    // Prevent/Stub
    doReturn(Optional.empty()).when(mockDepartmentService).findById(originalTeam.getDepartment().getId());

    // When
    assertThrows(InvalidIdReferenceException.class, () -> {
      teamService.create(
          originalTeam.getName(),
          originalTeam.getDescription(),
          originalTeam.getTenantId(),
          originalTeam.getDepartment().getId()
      );
    });
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

  @Override
  AbstractCrudService<Team, AbstractCrudRepository<Team>> getCrudServiceSpy() {
    return teamService;
  }
}
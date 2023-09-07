package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.Team;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeAssessmentServiceTest extends AbstractCrudServiceTest<EmployeeAssessment, AbstractCrudRepository<EmployeeAssessment>> {

  @InjectMocks
  @Spy
  private EmployeeAssessmentService employeeAssessmentService;

  @Mock
  private AssessmentMatrixService assessmentMatrixService;

  @Mock
  private TeamService teamService;

  @Mock
  private EmployeeAssessmentRepository employeeAssessmentRepository;

  private EmployeeAssessment originalEmployeeAssessment;

  private final AssessmentMatrix assessmentMatrix = createMockedAssessmentMatrix(GENERIC_ID_1234, DEFAULT_ID, createMockedPillarMap(1,2, "P", "C"));

  private final Team team = createMockedTeam(DEFAULT_ID);

  @BeforeEach
  void setUpBefore() {
    originalEmployeeAssessment = createMockedEmployeeAssessment(DEFAULT_ID, "Fernando", assessmentMatrix.getId());
    originalEmployeeAssessment = cloneWithId(originalEmployeeAssessment, DEFAULT_ID);
  }

  @Test
  void create() {
    EmployeeAssessment savedEmployeeAssessment = cloneWithId(originalEmployeeAssessment, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedEmployeeAssessment).when(employeeAssessmentRepository).save(any());
    doReturn(Optional.of(team)).when(teamService).findById(originalEmployeeAssessment.getTeam().getId());
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalEmployeeAssessment.getAssessmentMatrixId());

    // When
    Optional<EmployeeAssessment> employeeAssessmentOptional = employeeAssessmentService.create(
        originalEmployeeAssessment.getAssessmentMatrixId(),
        originalEmployeeAssessment.getTeam().getId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun());

    // Then
    assertTrue(employeeAssessmentOptional.isPresent());
    assertEquals(savedEmployeeAssessment, employeeAssessmentOptional.get());
    verify(employeeAssessmentRepository).save(savedEmployeeAssessment);
    verify(employeeAssessmentService).create(
        originalEmployeeAssessment.getAssessmentMatrixId(),
        originalEmployeeAssessment.getTeam().getId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun());
    verify(assessmentMatrixService).findById(originalEmployeeAssessment.getAssessmentMatrixId());
  }

  @Override
  AbstractCrudService<EmployeeAssessment, AbstractCrudRepository<EmployeeAssessment>> getCrudServiceSpy() {
    return employeeAssessmentService;
  }

  @Test
  void createNonExistantAssessmentMatrixId() {
    assertThrows(NullPointerException.class, () -> assertCreateEmployeeAssessmentForAssessmentMatrixId("NonExistentAssessmentMatrixId"));
  }

  @Test
  void createNullAssessmentMatrixId() {
    assertThrows(NullPointerException.class, () -> assertCreateEmployeeAssessmentForAssessmentMatrixId(null));
  }

  @Test
  void createNullTeamId() {
    assertThrows(InvalidIdReferenceException.class, () -> assertCreateEmployeeAssessmentForTeamId(null));
  }

  @Test
  void createNonExistantTeamId() {
    assertThrows(InvalidIdReferenceException.class, () -> assertCreateEmployeeAssessmentForTeamId("NonExistentTeamId"));
  }

  void assertCreateEmployeeAssessmentForAssessmentMatrixId(String assessmentMatrixId) {
    originalEmployeeAssessment.setAssessmentMatrixId(null);
    EmployeeAssessment savedEmployeeAssessment = cloneWithId(originalEmployeeAssessment, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedEmployeeAssessment).when(employeeAssessmentRepository).save(originalEmployeeAssessment);
    doReturn(Optional.of(team)).when(teamService).findById(originalEmployeeAssessment.getTeam().getId());
    if (assessmentMatrixId != null) doReturn(Optional.empty()).when(employeeAssessmentService).findById(any());

    // When
    Optional<EmployeeAssessment> employeeAssessmentOptional = employeeAssessmentService.create(
        assessmentMatrixId,
        originalEmployeeAssessment.getTeam().getId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun()
    );

    // Then
    assertTrue(employeeAssessmentOptional.isPresent());
    assertEquals(savedEmployeeAssessment, employeeAssessmentOptional.get());
    verify(employeeAssessmentRepository).save(originalEmployeeAssessment);
    verify(employeeAssessmentService).create(
        assessmentMatrixId,
        originalEmployeeAssessment.getTeam().getId(),
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun()
    );
    if (assessmentMatrixId == null) {
      verify(assessmentMatrixService, never()).findById(assessmentMatrixId);
    }
    else {
      verify(assessmentMatrixService).findById(assessmentMatrixId);
    }
  }

  void assertCreateEmployeeAssessmentForTeamId(String teamId) {
    originalEmployeeAssessment.setTeam(null);
    EmployeeAssessment savedEmployeeAssessment = cloneWithId(originalEmployeeAssessment, DEFAULT_ID);

    // Prevent/Stub
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(originalEmployeeAssessment.getAssessmentMatrixId());
    if (team != null) doReturn(Optional.empty()).when(teamService).findById(any());

    // When
    Optional<EmployeeAssessment> employeeAssessmentOptional = employeeAssessmentService.create(
        originalEmployeeAssessment.getAssessmentMatrixId(),
        teamId,
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun()
    );

    // Then
    assertTrue(employeeAssessmentOptional.isPresent());
    assertEquals(savedEmployeeAssessment, employeeAssessmentOptional.get());
    verify(employeeAssessmentRepository).save(originalEmployeeAssessment);
    verify(employeeAssessmentService).create(
        originalEmployeeAssessment.getAssessmentMatrixId(),
        teamId,
        originalEmployeeAssessment.getEmployee().getName(),
        originalEmployeeAssessment.getEmployee().getEmail(),
        originalEmployeeAssessment.getEmployee().getDocumentNumber(),
        originalEmployeeAssessment.getEmployee().getPersonDocumentType(),
        originalEmployeeAssessment.getEmployee().getGender(),
        originalEmployeeAssessment.getEmployee().getGenderPronoun()
    );
    if (teamId == null) {
      verify(teamService, never()).findById(teamId);
    }
    else {
      verify(teamService).findById(teamId);
    }
  }
}
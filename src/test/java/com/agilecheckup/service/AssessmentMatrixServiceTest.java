package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepository;
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
class AssessmentMatrixServiceTest extends AbstractCrudServiceTest<AssessmentMatrix, AbstractCrudRepository<AssessmentMatrix>> {

  @InjectMocks
  @Spy
  private AssessmentMatrixService assessmentMatrixService;

  @Mock
  private AssessmentMatrixRepository mockAssessmentMatrixRepository;

  @Mock
  private PerformanceCycleService mockPerformanceCycleService;

  private AssessmentMatrix originalAssessmentMatrix;

  private final PerformanceCycle performanceCycle = createMockedPerformanceCycle(GENERIC_ID_1234, GENERIC_ID_1234);

  @BeforeEach
  void setUpBefore() {
    originalAssessmentMatrix = createMockedAssessmentMatrixWithDependenciesId(
        DEFAULT_ID,
        createMockedPillarSet(3, 4, "Pillar", "Category"));
    originalAssessmentMatrix = cloneWithId(originalAssessmentMatrix, DEFAULT_ID);
  }

  @Test
  void create() {
    AssessmentMatrix savedAssessmentMatrix = cloneWithId(originalAssessmentMatrix, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedAssessmentMatrix).when(mockAssessmentMatrixRepository).save(originalAssessmentMatrix);
    doReturn(Optional.of(performanceCycle)).when(mockPerformanceCycleService).findById(originalAssessmentMatrix.getPerformanceCycle().getId());

    // When
    Optional<AssessmentMatrix> assessmentMatrixOptional = assessmentMatrixService.create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        originalAssessmentMatrix.getPerformanceCycle().getId(),
        originalAssessmentMatrix.getPillars()
    );

    // Then
    assertTrue(assessmentMatrixOptional.isPresent());
    assertEquals(savedAssessmentMatrix, assessmentMatrixOptional.get());
    verify(mockAssessmentMatrixRepository).save(originalAssessmentMatrix);
    verify(assessmentMatrixService).create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        originalAssessmentMatrix.getPerformanceCycle().getId(),
        originalAssessmentMatrix.getPillars()
    );
    verify(mockPerformanceCycleService).findById(originalAssessmentMatrix.getPerformanceCycle().getId());
  }

  @Test
  void createNonExistantPerformanceCycleId() {
    assertCreatePerformanceCycleIdFor("NonExistentPerformanceCycleId");
  }

  @Test
  void createNullPerformanceCycleId() {
    assertCreatePerformanceCycleIdFor(null);
  }

  void assertCreatePerformanceCycleIdFor(String performanceCycleId) {
    originalAssessmentMatrix.setPerformanceCycle(null);
    AssessmentMatrix savedAssessmentMatrix = cloneWithId(originalAssessmentMatrix, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedAssessmentMatrix).when(mockAssessmentMatrixRepository).save(originalAssessmentMatrix);
    if (performanceCycleId != null) doReturn(Optional.empty()).when(mockPerformanceCycleService).findById(any());

    // When
    Optional<AssessmentMatrix> assessmentMatrixOptional = assessmentMatrixService.create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        performanceCycleId,
        originalAssessmentMatrix.getPillars()
    );

    // Then
    assertTrue(assessmentMatrixOptional.isPresent());
    assertEquals(savedAssessmentMatrix, assessmentMatrixOptional.get());
    verify(mockAssessmentMatrixRepository).save(originalAssessmentMatrix);
    verify(assessmentMatrixService).create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        performanceCycleId,
        originalAssessmentMatrix.getPillars()
    );
    if (performanceCycleId == null) {
      verify(mockPerformanceCycleService, never()).findById(performanceCycleId);
    }
    else {
      verify(mockPerformanceCycleService).findById(performanceCycleId);
    }
  }

  @Test
  void create_NullAssessmentMatrixName() {
    // When
    assertThrows(NullPointerException.class, () -> {
      assessmentMatrixService.create(
          null,
          originalAssessmentMatrix.getDescription(),
          originalAssessmentMatrix.getTenantId(),
          originalAssessmentMatrix.getPerformanceCycle().getId(),
          originalAssessmentMatrix.getPillars()
      );
    });
  }

  @Override
  AbstractCrudService<AssessmentMatrix, AbstractCrudRepository<AssessmentMatrix>> getCrudServiceSpy() {
    return assessmentMatrixService;
  }
}
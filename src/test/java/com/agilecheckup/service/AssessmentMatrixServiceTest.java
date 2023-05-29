package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepository;
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
class AssessmentMatrixServiceTest {

  private static final String ASSESSMENT_MATRIX_ID = "1234";

  @InjectMocks
  @Spy
  private AssessmentMatrixService assessmentMatrixService;

  @Mock
  private AssessmentMatrixRepository mockAssessmentMatrixRepository;

  @Mock
  private PerformanceCycleService mockPerformanceCycleService;

  private final AssessmentMatrix originalAssessmentMatrix = createMockedAssessmentMatrixWithDependenciesId(
      ASSESSMENT_MATRIX_ID,
      createMockedPillarSet(3, 4, "Pillar", "Category"));

  private final PerformanceCycle performanceCycle = createMockedPerformanceCycle(GENERIC_ID_1234, GENERIC_ID_1234);

  @Test
  void create() {
    AssessmentMatrix savedAssessmentMatrix = cloneWithId(originalAssessmentMatrix, ASSESSMENT_MATRIX_ID);

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
  void createNullPerformanceCycleId() {
    originalAssessmentMatrix.setPerformanceCycle(null);
    AssessmentMatrix savedAssessmentMatrix = cloneWithId(originalAssessmentMatrix, ASSESSMENT_MATRIX_ID);

    // Prevent/Stub
    doReturn(savedAssessmentMatrix).when(mockAssessmentMatrixRepository).save(originalAssessmentMatrix);
    doReturn(Optional.empty()).when(mockPerformanceCycleService).findById(any());

    // When
    Optional<AssessmentMatrix> assessmentMatrixOptional = assessmentMatrixService.create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        "NonExistentPerformanceCycleId",
        originalAssessmentMatrix.getPillars()
    );

    // Then
    assertTrue(assessmentMatrixOptional.isPresent());
//    assertEquals(savedAssessmentMatrix, assessmentMatrixOptional.get());
//    verify(mockAssessmentMatrixRepository).save(originalAssessmentMatrix);
//    verify(assessmentMatrixService).create(
//        originalAssessmentMatrix.getName(),
//        originalAssessmentMatrix.getDescription(),
//        originalAssessmentMatrix.getTenantId(),
//        originalAssessmentMatrix.getPerformanceCycle().getId(),
//        originalAssessmentMatrix.getPillars()
//    );
//    verify(mockPerformanceCycleService).findById(originalAssessmentMatrix.getPerformanceCycle().getId());
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
}
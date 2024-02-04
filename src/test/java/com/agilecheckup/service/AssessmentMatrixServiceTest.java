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
        createMockedPillarMap(3, 4, "Pillar", "Category"));
    originalAssessmentMatrix = cloneWithId(originalAssessmentMatrix, DEFAULT_ID);
  }

  @Test
  void create() {
    AssessmentMatrix savedAssessmentMatrix = cloneWithId(originalAssessmentMatrix, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedAssessmentMatrix).when(mockAssessmentMatrixRepository).save(originalAssessmentMatrix);
    doReturn(Optional.of(performanceCycle)).when(mockPerformanceCycleService).findById(originalAssessmentMatrix.getPerformanceCycleId());

    // When
    Optional<AssessmentMatrix> assessmentMatrixOptional = assessmentMatrixService.create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        originalAssessmentMatrix.getPerformanceCycleId(),
        originalAssessmentMatrix.getPillarMap()
    );

    // Then
    assertTrue(assessmentMatrixOptional.isPresent());
    assertEquals(savedAssessmentMatrix, assessmentMatrixOptional.get());
    verify(mockAssessmentMatrixRepository).save(originalAssessmentMatrix);
    verify(assessmentMatrixService).create(
        originalAssessmentMatrix.getName(),
        originalAssessmentMatrix.getDescription(),
        originalAssessmentMatrix.getTenantId(),
        originalAssessmentMatrix.getPerformanceCycleId(),
        originalAssessmentMatrix.getPillarMap()
    );
    verify(mockPerformanceCycleService).findById(originalAssessmentMatrix.getPerformanceCycleId());
  }

  @Test
  void createNonExistantPerformanceCycleId() {
    assertThrows(NullPointerException.class, () -> assertCreateAssessmentMatrixFor("NonExistentPerformanceCycleId"));
  }

  @Test
  void createNullPerformanceCycleId() {
    assertThrows(NullPointerException.class, () -> assertCreateAssessmentMatrixFor(null));
  }

  void assertCreateAssessmentMatrixFor(String performanceCycleId) {
    originalAssessmentMatrix.setPerformanceCycleId(null);
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
        originalAssessmentMatrix.getPillarMap()
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
        originalAssessmentMatrix.getPillarMap()
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
          originalAssessmentMatrix.getPerformanceCycleId(),
          originalAssessmentMatrix.getPillarMap()
      );
    });
  }

  @Test
  void shouldIncrementQuestionCount() {
    String assessmentMatrixId = "matrixId";
    mockFindById(assessmentMatrixId, originalAssessmentMatrix);
    mockPerformLockedAsSynchronous();

    assessmentMatrixService.incrementQuestionCount(assessmentMatrixId);

    assertQuestionCountIncremented();
    verifyRepositoryInteractions(assessmentMatrixId);
  }

  @Test
  void shouldNotIncrementQuestionCount() {
    String assessmentMatrixId = "matrixId";
    doReturn(null).when(mockAssessmentMatrixRepository).findById(assessmentMatrixId);
    assessmentMatrixService.incrementQuestionCount(assessmentMatrixId);
    verify(mockAssessmentMatrixRepository, never()).save(any());
  }

  @Override
  AbstractCrudService<AssessmentMatrix, AbstractCrudRepository<AssessmentMatrix>> getCrudServiceSpy() {
    return assessmentMatrixService;
  }

  private void mockFindById(String matrixId, AssessmentMatrix assessmentMatrix) {
    when(mockAssessmentMatrixRepository.findById(matrixId)).thenReturn(assessmentMatrix);
  }

  private void mockPerformLockedAsSynchronous() {
    doAnswer(invocation -> {
      Runnable runnable = invocation.getArgument(1);
      runnable.run();
      return null;
    }).when(mockAssessmentMatrixRepository).performLocked(anyString(), any(Runnable.class));
  }

  private void assertQuestionCountIncremented() {
    assertEquals(1, originalAssessmentMatrix.getQuestionCount());
  }

  private void verifyRepositoryInteractions(String matrixId) {
    verify(mockAssessmentMatrixRepository, times(2)).findById(matrixId);
    verify(mockAssessmentMatrixRepository).save(originalAssessmentMatrix);
  }
}
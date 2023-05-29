package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.*;

@ExtendWith(MockitoExtension.class)
class AssessmentMatrixRepositoryTest extends AbstractRepositoryTest<AssessmentMatrix> {

  @InjectMocks
  @Spy
  private AssessmentMatrixRepository assessmentMatrixRepository;

  @Override
  AbstractCrudRepository getRepository() {
    return assessmentMatrixRepository;
  }

  @Override
  AssessmentMatrix createMockedT() {
    return createMockedAssessmentMatrix(GENERIC_ID_1234, GENERIC_ID_1234, createMockedPillarSet(3, 4, "Pillar", "Category"));
  }
}
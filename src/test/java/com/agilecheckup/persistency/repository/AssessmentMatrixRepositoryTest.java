package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.createMockedAssessmentMatrix;
import static com.agilecheckup.util.TestObjectFactory.createMockedPillarMap;

@ExtendWith(MockitoExtension.class)
class AssessmentMatrixRepositoryTest extends AbstractRepositoryTest<AssessmentMatrix> {

  private AssessmentMatrixRepository assessmentMatrixRepository;
  
  @BeforeEach
  void setUpRepository() {
    // Manually create repository with mocked mapper
    assessmentMatrixRepository = new AssessmentMatrixRepository(dynamoDBMapperMock);
  }

  @Override
  AbstractCrudRepository getRepository() {
    return assessmentMatrixRepository;
  }

  @Override
  AssessmentMatrix createMockedT() {
    return createMockedAssessmentMatrix(GENERIC_ID_1234, GENERIC_ID_1234, createMockedPillarMap(3, 4, "Pillar", "Category"));
  }

  @Override
  Class getMockedClass() {
    return AssessmentMatrix.class;
  }
}
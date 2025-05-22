package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.EmployeeAssessment;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.createMockedEmployeeAssessment;

@ExtendWith(MockitoExtension.class)
class EmployeeAssessmentRepositoryTest extends AbstractRepositoryTest<EmployeeAssessment> {

  @InjectMocks
  @Spy
  private EmployeeAssessmentRepository employeeAssessmentRepository;

  @Override
  AbstractCrudRepository getRepository() {
    return employeeAssessmentRepository;
  }

  @Override
  EmployeeAssessment createMockedT() {
    return createMockedEmployeeAssessment(GENERIC_ID_1234, "Josivaldo", GENERIC_ID_1234);
  }

  @Override
  Class getMockedClass() {
    return EmployeeAssessment.class;
  }
}
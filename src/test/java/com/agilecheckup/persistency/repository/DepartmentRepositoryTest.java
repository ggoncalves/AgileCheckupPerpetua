package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.Department;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.createMockedDepartment;

@ExtendWith(MockitoExtension.class)
class DepartmentRepositoryTest extends AbstractRepositoryTest<Department> {

  @InjectMocks
  @Spy
  private DepartmentRepository departmentRepository;

  @Override
  AbstractCrudRepository getRepository() {
    return departmentRepository;
  }

  @Override
  Department createMockedT() {
    return createMockedDepartment();
  }
}
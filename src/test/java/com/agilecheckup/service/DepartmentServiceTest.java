package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.Department;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.DepartmentRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import org.junit.jupiter.api.BeforeAll;
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
class DepartmentServiceTest extends AbstractCrudServiceTest<Department, AbstractCrudRepository<Department>> {

  @InjectMocks
  @Spy
  private DepartmentService departmentService;

  @Mock
  private DepartmentRepository mockDepartmentRepository;

  @Mock
  private CompanyService mockCompanyService;

  private static Department originalDepartment;

  private final Company company = createMockedCompany(DEFAULT_ID);

  @BeforeAll
  static void beforeAll() {
     originalDepartment = createMockedDepartmentWithDependenciesId(DEFAULT_ID);
     originalDepartment = cloneWithId(originalDepartment, DEFAULT_ID);
  }

  @Test
  void create() {
    Department savedDepartment = copyDepartmentAndAddId(originalDepartment, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedDepartment).when(mockDepartmentRepository).save(any());
    doReturn(Optional.of(company)).when(mockCompanyService).findById(originalDepartment.getCompanyId());

    // When
    Optional<Department> departmentOptional = departmentService.create(
        originalDepartment.getName(),
        originalDepartment.getDescription(),
        originalDepartment.getTenantId(),
        originalDepartment.getCompanyId()
    );

    // Then
    assertTrue(departmentOptional.isPresent());
    assertEquals(savedDepartment, departmentOptional.get());
    verify(mockDepartmentRepository).save(originalDepartment);
    verify(departmentService).create(originalDepartment.getName(), originalDepartment.getDescription(), originalDepartment.getTenantId(), originalDepartment.getCompanyId());
    verify(mockCompanyService).findById(originalDepartment.getCompanyId());
  }

  @Test
  void createInvalidCompanyId() {
    Department savedDepartment = copyDepartmentAndAddId(originalDepartment, DEFAULT_ID);

    // Prevent/Stub
    doReturn(Optional.empty()).when(mockCompanyService).findById(originalDepartment.getCompanyId());

    // When
    assertThrows(InvalidIdReferenceException.class, () -> {
      departmentService.create(
          originalDepartment.getName(),
          originalDepartment.getDescription(),
          originalDepartment.getTenantId(),
          originalDepartment.getCompanyId()
      );
    });
  }

  @Test
  void create_NullDepartmentName() {
    // When
    assertThrows(NullPointerException.class, () -> {
      departmentService.create(
          null,
          originalDepartment.getDescription(),
          originalDepartment.getTenantId(),
          originalDepartment.getCompanyId()
      );
    });
  }

  @Override
  AbstractCrudService<Department, AbstractCrudRepository<Department>> getCrudServiceSpy() {
    return departmentService;
  }
}
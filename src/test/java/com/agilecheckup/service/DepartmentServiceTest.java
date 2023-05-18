package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.Department;
import com.agilecheckup.persistency.repository.DepartmentRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

  private static final String DEPARTMENT_ID = "1234";

  @InjectMocks
  @Spy
  private DepartmentService departmentService;

  @Mock
  private DepartmentRepository mockDepartmentRepository;

  @Mock
  private CompanyService mockCompanyService;

  private final Department originalDepartment = createMockedDepartmentWithDependenciesId(GENERIC_ID_1234);

  private final Company company = createMockedCompany(GENERIC_ID_1234);

  @Test
  void create() {
    Department savedDepartment = copyDepartmentAndAddId(originalDepartment, DEPARTMENT_ID);

    // Prevent/Stub
    doReturn(savedDepartment).when(mockDepartmentRepository).save(originalDepartment);
    doReturn(Optional.of(company)).when(mockCompanyService).findById(originalDepartment.getCompany().getId());

    // When
    Optional<Department> departmentOptional = departmentService.create(
        originalDepartment.getName(),
        originalDepartment.getDescription(),
        originalDepartment.getTenantId(),
        originalDepartment.getCompany().getId()
    );

    // Then
    assertTrue(departmentOptional.isPresent());
    assertEquals(savedDepartment, departmentOptional.get());
    verify(mockDepartmentRepository).save(originalDepartment);
    verify(departmentService).create(originalDepartment.getName(), originalDepartment.getDescription(), originalDepartment.getTenantId(), originalDepartment.getCompany().getId());
    verify(mockCompanyService).findById(originalDepartment.getCompany().getId());
  }

  @Test
  void createInvalidCompanyId() {
    Department savedDepartment = copyDepartmentAndAddId(originalDepartment, DEPARTMENT_ID);

    // Prevent/Stub
    doReturn(Optional.empty()).when(mockCompanyService).findById(originalDepartment.getCompany().getId());

    // When
    assertThrows(InvalidIdReferenceException.class, () -> {
      departmentService.create(
          originalDepartment.getName(),
          originalDepartment.getDescription(),
          originalDepartment.getTenantId(),
          originalDepartment.getCompany().getId()
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
          originalDepartment.getCompany().getId()
      );
    });
  }
}
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

import static com.agilecheckup.util.TestObjectFactory.cloneWithId;
import static com.agilecheckup.util.TestObjectFactory.copyDepartmentAndAddId;
import static com.agilecheckup.util.TestObjectFactory.createMockedCompany;
import static com.agilecheckup.util.TestObjectFactory.createMockedDepartmentWithDependenciesId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceLegacyTest extends AbstractCrudServiceTest<Department, AbstractCrudRepository<Department>> {

  @InjectMocks
  @Spy
  private DepartmentServiceLegacy departmentService;

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
    doAnswerForSaveWithRandomEntityId(savedDepartment, mockDepartmentRepository);
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
    verify(mockDepartmentRepository).save(savedDepartment);
    verify(departmentService).create(originalDepartment.getName(), originalDepartment.getDescription(), originalDepartment.getTenantId(), originalDepartment.getCompanyId());
    verify(mockCompanyService).findById(originalDepartment.getCompanyId());
  }

  @Test
  void createInvalidCompanyId() {
    // Prevent/Stub
    doReturn(Optional.empty()).when(mockCompanyService).findById(originalDepartment.getCompanyId());

    // When
    assertThrows(InvalidIdReferenceException.class, () -> departmentService.create(
        originalDepartment.getName(),
        originalDepartment.getDescription(),
        originalDepartment.getTenantId(),
        originalDepartment.getCompanyId()
    ));
  }

  @Test
  void create_NullDepartmentName() {
    // When
    assertThrows(NullPointerException.class, () -> departmentService.create(
        null,
        originalDepartment.getDescription(),
        originalDepartment.getTenantId(),
        originalDepartment.getCompanyId()
    ));
  }

  @Test
  void update_existingDepartment_shouldSucceed() {
    // Prepare
    Department existingDepartment = createMockedDepartmentWithDependenciesId(DEFAULT_ID);
    existingDepartment = cloneWithId(existingDepartment, DEFAULT_ID);
    Department updatedDepartmentDetails = createMockedDepartmentWithDependenciesId(DEFAULT_ID);
    updatedDepartmentDetails.setName("Updated Department Name");
    updatedDepartmentDetails.setDescription("Updated Description");
    updatedDepartmentDetails.setTenantId("Updated Tenant Id");
    updatedDepartmentDetails.setCompanyId("updatedCompanyId");

    Company updatedCompany = createMockedCompany("updatedCompanyId");

    // Mock repository calls
    doReturn(existingDepartment).when(mockDepartmentRepository).findById(DEFAULT_ID);
    doAnswerForUpdate(updatedDepartmentDetails, mockDepartmentRepository);
    doReturn(Optional.of(updatedCompany)).when(mockCompanyService).findById("updatedCompanyId");

    // When
    Optional<Department> resultOptional = departmentService.update(
        DEFAULT_ID,
        updatedDepartmentDetails.getName(),
        updatedDepartmentDetails.getDescription(),
        updatedDepartmentDetails.getTenantId(),
        updatedDepartmentDetails.getCompanyId()
    );

    // Then
    assertTrue(resultOptional.isPresent());
    assertEquals(updatedDepartmentDetails, resultOptional.get());
    verify(mockDepartmentRepository).findById(DEFAULT_ID);
    verify(mockDepartmentRepository).save(updatedDepartmentDetails);
    verify(mockCompanyService).findById("updatedCompanyId");
    verify(departmentService).update(DEFAULT_ID,
        "Updated Department Name",
        "Updated Description",
        "Updated Tenant Id",
        "updatedCompanyId");
  }

  @Test
  void update_nonExistingDepartment_shouldReturnEmpty() {
    // Prepare
    String nonExistingId = "nonExistingId";

    // Mock repository calls
    doReturn(null).when(mockDepartmentRepository).findById(nonExistingId);

    // When
    Optional<Department> resultOptional = departmentService.update(
        nonExistingId,
        "name",
        "desc",
        "tenant",
        "companyId"
    );

    // Then
    assertTrue(resultOptional.isEmpty());
    verify(mockDepartmentRepository).findById(nonExistingId);
    verify(departmentService).update(nonExistingId, "name", "desc", "tenant", "companyId");
  }
}
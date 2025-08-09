package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.Department;
import com.agilecheckup.persistency.repository.DepartmentRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_COMPANY_ID;
import static com.agilecheckup.util.TestObjectFactory.GENERIC_TENANT_ID;
import static com.agilecheckup.util.TestObjectFactory.createMockedCompanyV2;
import static com.agilecheckup.util.TestObjectFactory.createMockedDepartmentV2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest extends AbstractCrudServiceTest<Department, DepartmentRepository> {
    
    @Mock
    private DepartmentRepository mockDepartmentRepositoryV2;
    
    @Mock
    private CompanyService mockCompanyServiceV2;
    
    private DepartmentService departmentServiceV2;
    
    @BeforeEach
    void setUp() {
        departmentServiceV2 = new DepartmentService(mockDepartmentRepositoryV2, mockCompanyServiceV2);
    }
    
    @Test
    @DisplayName("Should create department successfully")
    void shouldCreateDepartmentSuccessfully() {
        String name = "Engineering";
        String description = "Engineering Department";
        Company company = createMockedCompanyV2(GENERIC_COMPANY_ID);
        Department expectedDepartment = createMockedDepartmentV2(name, description, GENERIC_TENANT_ID, GENERIC_COMPANY_ID);
        
        doReturn(Optional.of(company)).when(mockCompanyServiceV2).findById(GENERIC_COMPANY_ID);
        doReturnForSaveWithRandomEntityId(expectedDepartment, mockDepartmentRepositoryV2);
        
        Optional<Department> result = departmentServiceV2.create(name, description, GENERIC_TENANT_ID, GENERIC_COMPANY_ID);
        
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(name);
        assertThat(result.get().getDescription()).isEqualTo(description);
        assertThat(result.get().getTenantId()).isEqualTo(GENERIC_TENANT_ID);
        assertThat(result.get().getCompanyId()).isEqualTo(GENERIC_COMPANY_ID);
        verify(mockDepartmentRepositoryV2).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Should throw exception when company not found during creation")
    void shouldThrowExceptionWhenCompanyNotFoundDuringCreation() {
        doReturn(Optional.empty()).when(mockCompanyServiceV2).findById(GENERIC_COMPANY_ID);
        
        assertThatThrownBy(() -> departmentServiceV2.create("Engineering", "Description", GENERIC_TENANT_ID, GENERIC_COMPANY_ID))
                .isInstanceOf(InvalidIdReferenceException.class);
        
        verify(mockDepartmentRepositoryV2, never()).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Should update department successfully")
    void shouldUpdateDepartmentSuccessfully() {
        String departmentId = "dept-id";
        String newName = "Updated Engineering";
        String newDescription = "Updated Description";
        
        Department existingDepartment = createMockedDepartmentV2(departmentId);
        Department updatedDepartment = createMockedDepartmentV2(departmentId);
        updatedDepartment.setName(newName);
        updatedDepartment.setDescription(newDescription);
        
        Company company = createMockedCompanyV2(GENERIC_COMPANY_ID);
        
        doReturnForFindById(existingDepartment, mockDepartmentRepositoryV2);
        doReturn(Optional.of(company)).when(mockCompanyServiceV2).findById(GENERIC_COMPANY_ID);
        doReturnForUpdate(updatedDepartment, mockDepartmentRepositoryV2);
        
        Optional<Department> result = departmentServiceV2.update(departmentId, newName, newDescription, GENERIC_TENANT_ID, GENERIC_COMPANY_ID);
        
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(newName);
        assertThat(result.get().getDescription()).isEqualTo(newDescription);
        verify(mockDepartmentRepositoryV2).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Should return empty when updating non-existent department")
    void shouldReturnEmptyWhenUpdatingNonExistentDepartment() {
        String nonExistentId = "non-existent";
        
        doReturnEmptyForFindById(mockDepartmentRepositoryV2, nonExistentId);
        
        Optional<Department> result = departmentServiceV2.update(nonExistentId, "Name", "Description", GENERIC_TENANT_ID, GENERIC_COMPANY_ID);
        
        assertThat(result).isEmpty();
        verify(mockDepartmentRepositoryV2, never()).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Should throw exception when company not found during update")
    void shouldThrowExceptionWhenCompanyNotFoundDuringUpdate() {
        String departmentId = "dept-id";
        Department existingDepartment = createMockedDepartmentV2(departmentId);
        
        doReturnForFindById(existingDepartment, mockDepartmentRepositoryV2);
        doReturn(Optional.empty()).when(mockCompanyServiceV2).findById(GENERIC_COMPANY_ID);
        
        assertThatThrownBy(() -> departmentServiceV2.update(departmentId, "Name", "Description", GENERIC_TENANT_ID, GENERIC_COMPANY_ID))
                .isInstanceOf(InvalidIdReferenceException.class);
        
        verify(mockDepartmentRepositoryV2, never()).save(any(Department.class));
    }
    
    @Test
    @DisplayName("Should find all departments by tenant id")
    void shouldFindAllDepartmentsByTenantId() {
        Department dept1 = createMockedDepartmentV2("id1");
        Department dept2 = createMockedDepartmentV2("id2");
        List<Department> expectedDepartments = List.of(dept1, dept2);
        
        doReturn(expectedDepartments).when(mockDepartmentRepositoryV2).findAllByTenantId(GENERIC_TENANT_ID);
        
        List<Department> result = departmentServiceV2.findAllByTenantId(GENERIC_TENANT_ID);
        
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(dept1, dept2);
        verify(mockDepartmentRepositoryV2).findAllByTenantId(GENERIC_TENANT_ID);
    }
    
    @Test
    @DisplayName("Should find department by id")
    void shouldFindDepartmentById() {
        String departmentId = "dept-id";
        Department expectedDepartment = createMockedDepartmentV2(departmentId);
        
        doReturnForFindById(expectedDepartment, mockDepartmentRepositoryV2);
        
        Optional<Department> result = departmentServiceV2.findById(departmentId);
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedDepartment);
        verify(mockDepartmentRepositoryV2).findById(departmentId);
    }
    
    @Test
    @DisplayName("Should delete department by id")
    void shouldDeleteDepartmentById() {
        String departmentId = "dept-id";
        
        doReturn(true).when(mockDepartmentRepositoryV2).deleteById(departmentId);
        
        boolean result = departmentServiceV2.deleteById(departmentId);
        
        assertThat(result).isTrue();
        verify(mockDepartmentRepositoryV2).deleteById(departmentId);
    }
}
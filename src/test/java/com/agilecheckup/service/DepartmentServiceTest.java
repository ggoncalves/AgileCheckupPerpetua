package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.DepartmentV2;
import com.agilecheckup.persistency.repository.DepartmentRepositoryV2;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.createMockedCompany;
import static com.agilecheckup.util.TestObjectFactoryV2.GENERIC_COMPANY_ID;
import static com.agilecheckup.util.TestObjectFactoryV2.GENERIC_TENANT_ID;
import static com.agilecheckup.util.TestObjectFactoryV2.createMockedDepartmentV2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest extends AbstractCrudServiceTestV2<DepartmentV2, DepartmentRepositoryV2> {
    
    @Mock
    private DepartmentRepositoryV2 mockDepartmentRepositoryV2;
    
    @Mock
    private CompanyService mockCompanyService;
    
    private DepartmentService departmentService;
    
    @BeforeEach
    void setUp() {
        departmentService = new DepartmentService(mockDepartmentRepositoryV2, mockCompanyService);
    }
    
    @Test
    @DisplayName("Should create department successfully")
    void shouldCreateDepartmentSuccessfully() {
        String name = "Engineering";
        String description = "Engineering Department";
        Company company = createMockedCompany(GENERIC_COMPANY_ID);
        DepartmentV2 expectedDepartment = createMockedDepartmentV2(name, description, GENERIC_TENANT_ID, GENERIC_COMPANY_ID);
        
        doReturn(Optional.of(company)).when(mockCompanyService).findById(GENERIC_COMPANY_ID);
        doReturnForSaveWithRandomEntityId(expectedDepartment, mockDepartmentRepositoryV2);
        
        Optional<DepartmentV2> result = departmentService.create(name, description, GENERIC_TENANT_ID, GENERIC_COMPANY_ID);
        
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(name);
        assertThat(result.get().getDescription()).isEqualTo(description);
        assertThat(result.get().getTenantId()).isEqualTo(GENERIC_TENANT_ID);
        assertThat(result.get().getCompanyId()).isEqualTo(GENERIC_COMPANY_ID);
        verify(mockDepartmentRepositoryV2).save(any(DepartmentV2.class));
    }
    
    @Test
    @DisplayName("Should throw exception when company not found during creation")
    void shouldThrowExceptionWhenCompanyNotFoundDuringCreation() {
        doReturn(Optional.empty()).when(mockCompanyService).findById(GENERIC_COMPANY_ID);
        
        assertThatThrownBy(() -> departmentService.create("Engineering", "Description", GENERIC_TENANT_ID, GENERIC_COMPANY_ID))
                .isInstanceOf(InvalidIdReferenceException.class);
        
        verify(mockDepartmentRepositoryV2, never()).save(any(DepartmentV2.class));
    }
    
    @Test
    @DisplayName("Should update department successfully")
    void shouldUpdateDepartmentSuccessfully() {
        String departmentId = "dept-id";
        String newName = "Updated Engineering";
        String newDescription = "Updated Description";
        
        DepartmentV2 existingDepartment = createMockedDepartmentV2(departmentId);
        DepartmentV2 updatedDepartment = createMockedDepartmentV2(departmentId);
        updatedDepartment.setName(newName);
        updatedDepartment.setDescription(newDescription);
        
        Company company = createMockedCompany(GENERIC_COMPANY_ID);
        
        doReturnForFindById(existingDepartment, mockDepartmentRepositoryV2);
        doReturn(Optional.of(company)).when(mockCompanyService).findById(GENERIC_COMPANY_ID);
        doReturnForUpdate(updatedDepartment, mockDepartmentRepositoryV2);
        
        Optional<DepartmentV2> result = departmentService.update(departmentId, newName, newDescription, GENERIC_TENANT_ID, GENERIC_COMPANY_ID);
        
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(newName);
        assertThat(result.get().getDescription()).isEqualTo(newDescription);
        verify(mockDepartmentRepositoryV2).save(any(DepartmentV2.class));
    }
    
    @Test
    @DisplayName("Should return empty when updating non-existent department")
    void shouldReturnEmptyWhenUpdatingNonExistentDepartment() {
        String nonExistentId = "non-existent";
        
        doReturnEmptyForFindById(mockDepartmentRepositoryV2, nonExistentId);
        
        Optional<DepartmentV2> result = departmentService.update(nonExistentId, "Name", "Description", GENERIC_TENANT_ID, GENERIC_COMPANY_ID);
        
        assertThat(result).isEmpty();
        verify(mockDepartmentRepositoryV2, never()).save(any(DepartmentV2.class));
    }
    
    @Test
    @DisplayName("Should throw exception when company not found during update")
    void shouldThrowExceptionWhenCompanyNotFoundDuringUpdate() {
        String departmentId = "dept-id";
        DepartmentV2 existingDepartment = createMockedDepartmentV2(departmentId);
        
        doReturnForFindById(existingDepartment, mockDepartmentRepositoryV2);
        doReturn(Optional.empty()).when(mockCompanyService).findById(GENERIC_COMPANY_ID);
        
        assertThatThrownBy(() -> departmentService.update(departmentId, "Name", "Description", GENERIC_TENANT_ID, GENERIC_COMPANY_ID))
                .isInstanceOf(InvalidIdReferenceException.class);
        
        verify(mockDepartmentRepositoryV2, never()).save(any(DepartmentV2.class));
    }
    
    @Test
    @DisplayName("Should find all departments by tenant id")
    void shouldFindAllDepartmentsByTenantId() {
        DepartmentV2 dept1 = createMockedDepartmentV2("id1");
        DepartmentV2 dept2 = createMockedDepartmentV2("id2");
        List<DepartmentV2> expectedDepartments = List.of(dept1, dept2);
        
        doReturn(expectedDepartments).when(mockDepartmentRepositoryV2).findAllByTenantId(GENERIC_TENANT_ID);
        
        List<DepartmentV2> result = departmentService.findAllByTenantId(GENERIC_TENANT_ID);
        
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(dept1, dept2);
        verify(mockDepartmentRepositoryV2).findAllByTenantId(GENERIC_TENANT_ID);
    }
    
    @Test
    @DisplayName("Should find department by id")
    void shouldFindDepartmentById() {
        String departmentId = "dept-id";
        DepartmentV2 expectedDepartment = createMockedDepartmentV2(departmentId);
        
        doReturnForFindById(expectedDepartment, mockDepartmentRepositoryV2);
        
        Optional<DepartmentV2> result = departmentService.findById(departmentId);
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedDepartment);
        verify(mockDepartmentRepositoryV2).findById(departmentId);
    }
    
    @Test
    @DisplayName("Should delete department by id")
    void shouldDeleteDepartmentById() {
        String departmentId = "dept-id";
        
        doReturn(true).when(mockDepartmentRepositoryV2).deleteById(departmentId);
        
        boolean result = departmentService.deleteById(departmentId);
        
        assertThat(result).isTrue();
        verify(mockDepartmentRepositoryV2).deleteById(departmentId);
    }
}
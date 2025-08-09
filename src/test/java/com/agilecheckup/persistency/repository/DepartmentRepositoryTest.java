package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_TENANT_ID;
import static com.agilecheckup.util.TestObjectFactory.createMockedDepartmentV2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentRepositoryTest extends AbstractRepositoryTest<Department> {
    
    private DepartmentRepository departmentRepositoryV2;
    
    @Mock
    private DynamoDbIndex<Department> mockIndex;
    
    @Mock
    private SdkIterable<Page<Department>> mockPages;
    
    @Mock
    private Page<Department> mockPage;
    
    @Override
    protected Class<Department> getEntityClass() {
        return Department.class;
    }
    
    @Override
    protected String getTableName() {
        return "Department";
    }
    
    @BeforeEach
    void setUp() {
        super.setUp();
        departmentRepositoryV2 = new DepartmentRepository(mockEnhancedClient);
    }
    
    @Test
    @DisplayName("Should save department successfully")
    void shouldSaveDepartmentSuccessfully() {
        Department department = createMockedDepartmentV2();
        mockTablePutItem(department);
        
        Optional<Department> result = departmentRepositoryV2.save(department);
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(department);
        assertThat(result.get().getId()).isNotNull();
        verify(mockTable).putItem(department);
    }
    
    @Test
    @DisplayName("Should find department by id successfully")
    void shouldFindDepartmentByIdSuccessfully() {
        Department department = createMockedDepartmentV2("test-id");
        mockTableGetItem(department);
        
        Optional<Department> result = departmentRepositoryV2.findById("test-id");
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(department);
        verify(mockTable).getItem(any(Key.class));
    }
    
    @Test
    @DisplayName("Should return empty when department not found")
    void shouldReturnEmptyWhenDepartmentNotFound() {
        mockTableGetItemNull();
        
        Optional<Department> result = departmentRepositoryV2.findById("non-existent-id");
        
        assertThat(result).isEmpty();
        verify(mockTable).getItem(any(Key.class));
    }
    
    @Test
    @DisplayName("Should find all departments by tenant id")
    void shouldFindAllDepartmentsByTenantId() {
        Department department1 = createMockedDepartmentV2("id1");
        Department department2 = createMockedDepartmentV2("id2");
        
        when(mockTable.index(anyString())).thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPages);
        when(mockPages.stream()).thenReturn(Stream.of(mockPage));
        when(mockPage.items()).thenReturn(List.of(department1, department2));
        
        List<Department> result = departmentRepositoryV2.findAllByTenantId(GENERIC_TENANT_ID);
        
        assertThat(result).hasSize(2);
        assertThat(result).contains(department1, department2);
    }
    
    @Test
    @DisplayName("Should delete department by id successfully")
    void shouldDeleteDepartmentByIdSuccessfully() {
        boolean result = departmentRepositoryV2.deleteById("test-id");
        
        assertThat(result).isTrue();
        verify(mockTable).deleteItem(any(Key.class));
    }
    
    @Test
    @DisplayName("Should check if department exists by id")
    void shouldCheckIfDepartmentExistsById() {
        Department department = createMockedDepartmentV2("test-id");
        mockTableGetItem(department);
        
        boolean result = departmentRepositoryV2.existsById("test-id");
        
        assertThat(result).isTrue();
        verify(mockTable).getItem(any(Key.class));
    }
}
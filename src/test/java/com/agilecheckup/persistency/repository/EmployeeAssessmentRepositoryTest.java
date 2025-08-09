package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmployeeAssessmentRepositoryTest {

    @Mock
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Mock
    private DynamoDbTable<EmployeeAssessment> table;

    @Mock
    private DynamoDbIndex<EmployeeAssessment> gsi;

    private EmployeeAssessmentRepository repository;

    @BeforeEach
    void setUp() {
        // Create a real repository instance
        repository = new EmployeeAssessmentRepository(dynamoDbEnhancedClient, "TestEmployeeAssessment");
        
        // Mock the enhanced client to return our mocked table
        lenient().doReturn(table).when(dynamoDbEnhancedClient).table(eq("TestEmployeeAssessment"), any());
        lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");
        lenient().doReturn(gsi).when(table).index("tenantId-index");
    }

    @Test
    void testExistsByAssessmentMatrixAndEmployeeEmail_ReturnsTrueWhenExists() {
        String assessmentMatrixId = "matrix-123";
        String employeeEmail = "John.Doe@Example.com";
        String normalizedEmail = "john.doe@example.com";

        NaturalPerson employee = NaturalPerson.builder()
            .id("person-123")
            .name("John Doe")
            .email(employeeEmail)
            .build();

        EmployeeAssessment existingAssessment = EmployeeAssessment.builder()
            .id("assessment-123")
            .assessmentMatrixId(assessmentMatrixId)
            .teamId("team-123")
            .employee(employee)
            .employeeEmailNormalized(normalizedEmail)
            .build();
        existingAssessment.setTenantId("tenant-123");

        PageIterable<EmployeeAssessment> pageIterable = mock(PageIterable.class);
        SdkIterable<EmployeeAssessment> itemsIterable = mock(SdkIterable.class);
        
        lenient().doReturn(itemsIterable).when(pageIterable).items();
        lenient().doReturn(Arrays.asList(existingAssessment).stream()).when(itemsIterable).stream();
        lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));
        
        // Ensure the GSI is returned when the method calls table.index()
        lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");
        
        // Create a mock Page that returns our items
        Page<EmployeeAssessment> page = mock(Page.class);
        lenient().doReturn(Arrays.asList(existingAssessment)).when(page).items();
        lenient().doReturn(Arrays.asList(page).stream()).when(pageIterable).stream();

        boolean result = repository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrixId, employeeEmail);

        assertThat(result).isTrue();
        verify(gsi).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void testExistsByAssessmentMatrixAndEmployeeEmail_ReturnsFalseWhenNotExists() {
        String assessmentMatrixId = "matrix-123";
        String employeeEmail = "john.doe@example.com";

        PageIterable<EmployeeAssessment> pageIterable = mock(PageIterable.class);
        SdkIterable<EmployeeAssessment> itemsIterable = mock(SdkIterable.class);
        
        lenient().doReturn(itemsIterable).when(pageIterable).items();
        lenient().doReturn(Collections.<EmployeeAssessment>emptyList().stream()).when(itemsIterable).stream();
        lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));
        
        // Ensure the GSI is returned when the method calls table.index()
        lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");
        
        // Create a mock Page that returns empty items
        Page<EmployeeAssessment> page = mock(Page.class);
        lenient().doReturn(Collections.<EmployeeAssessment>emptyList()).when(page).items();
        lenient().doReturn(Arrays.asList(page).stream()).when(pageIterable).stream();

        boolean result = repository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrixId, employeeEmail);

        assertThat(result).isFalse();
        verify(gsi).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void testExistsByAssessmentMatrixAndEmployeeEmail_HandlesExceptions() {
        String assessmentMatrixId = "matrix-123";
        String employeeEmail = "john.doe@example.com";

        // Test that exceptions are properly wrapped and rethrown
        RuntimeException gsiException = new RuntimeException("GSI not available");
        lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");
        lenient().doThrow(gsiException).when(gsi).query(any(QueryEnhancedRequest.class));

        assertThatThrownBy(() -> repository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrixId, employeeEmail))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("GSI query failed");
    }

    @Test
    void testFindByAssessmentMatrixId_ReturnsFilteredResults() {
        String assessmentMatrixId = "matrix-123";
        String tenantId = "tenant-123";

        NaturalPerson employee1 = NaturalPerson.builder()
            .name("John Doe")
            .email("john.doe@example.com")
            .build();

        NaturalPerson employee2 = NaturalPerson.builder()
            .name("Jane Smith")
            .email("jane.smith@example.com")
            .build();

        EmployeeAssessment assessment1 = EmployeeAssessment.builder()
            .id("assessment-1")
            .assessmentMatrixId(assessmentMatrixId)
            .teamId("team-123")
            .employee(employee1)
            .assessmentStatus(AssessmentStatus.IN_PROGRESS)
            .build();
        assessment1.setTenantId(tenantId);

        EmployeeAssessment assessment2 = EmployeeAssessment.builder()
            .id("assessment-2")
            .assessmentMatrixId(assessmentMatrixId)
            .teamId("team-123")
            .employee(employee2)
            .assessmentStatus(AssessmentStatus.COMPLETED)
            .build();
        assessment2.setTenantId(tenantId);

        List<EmployeeAssessment> expectedResults = Arrays.asList(assessment1, assessment2);

        PageIterable<EmployeeAssessment> pageIterable = mock(PageIterable.class);
        SdkIterable<EmployeeAssessment> itemsIterable = mock(SdkIterable.class);
        
        lenient().doReturn(itemsIterable).when(pageIterable).items();
        lenient().doReturn(expectedResults.stream()).when(itemsIterable).stream();
        lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));
        
        // Ensure the GSI is returned when the method calls table.index()
        lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");
        
        // Create a mock Page that returns our items
        Page<EmployeeAssessment> page = mock(Page.class);
        lenient().doReturn(expectedResults).when(page).items();
        lenient().doReturn(Arrays.asList(page).stream()).when(pageIterable).stream();

        List<EmployeeAssessment> result = repository.findByAssessmentMatrixId(assessmentMatrixId, tenantId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(assessment1, assessment2);
        verify(gsi).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void testFindByAssessmentMatrixId_ReturnsEmptyListWhenNoMatches() {
        String assessmentMatrixId = "matrix-123";
        String tenantId = "tenant-123";

        PageIterable<EmployeeAssessment> pageIterable = mock(PageIterable.class);
        SdkIterable<EmployeeAssessment> itemsIterable = mock(SdkIterable.class);
        
        lenient().doReturn(itemsIterable).when(pageIterable).items();
        lenient().doReturn(Collections.<EmployeeAssessment>emptyList().stream()).when(itemsIterable).stream();
        lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));
        
        // Ensure the GSI is returned when the method calls table.index()
        lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");
        
        // Create a mock Page that returns empty items
        Page<EmployeeAssessment> page = mock(Page.class);
        lenient().doReturn(Collections.<EmployeeAssessment>emptyList()).when(page).items();
        lenient().doReturn(Arrays.asList(page).stream()).when(pageIterable).stream();

        List<EmployeeAssessment> result = repository.findByAssessmentMatrixId(assessmentMatrixId, tenantId);

        assertThat(result).isEmpty();
        verify(gsi).query(any(QueryEnhancedRequest.class));
    }
}
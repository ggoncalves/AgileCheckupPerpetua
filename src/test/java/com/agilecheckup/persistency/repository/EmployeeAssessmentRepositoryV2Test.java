package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessmentV2;
import com.agilecheckup.persistency.entity.person.NaturalPersonV2;
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
class EmployeeAssessmentRepositoryV2Test {

    @Mock
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Mock
    private DynamoDbTable<EmployeeAssessmentV2> table;

    @Mock
    private DynamoDbIndex<EmployeeAssessmentV2> gsi;

    private EmployeeAssessmentRepositoryV2 repository;

    @BeforeEach
    void setUp() {
        // Create a real repository instance
        repository = new EmployeeAssessmentRepositoryV2(dynamoDbEnhancedClient, "TestEmployeeAssessment");
        
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

        NaturalPersonV2 employee = NaturalPersonV2.builder()
            .id("person-123")
            .name("John Doe")
            .email(employeeEmail)
            .build();

        EmployeeAssessmentV2 existingAssessment = EmployeeAssessmentV2.builder()
            .id("assessment-123")
            .assessmentMatrixId(assessmentMatrixId)
            .teamId("team-123")
            .employee(employee)
            .employeeEmailNormalized(normalizedEmail)
            .build();
        existingAssessment.setTenantId("tenant-123");

        PageIterable<EmployeeAssessmentV2> pageIterable = mock(PageIterable.class);
        SdkIterable<EmployeeAssessmentV2> itemsIterable = mock(SdkIterable.class);
        
        lenient().doReturn(itemsIterable).when(pageIterable).items();
        lenient().doReturn(Arrays.asList(existingAssessment).stream()).when(itemsIterable).stream();
        lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));
        
        // Ensure the GSI is returned when the method calls table.index()
        lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");
        
        // Create a mock Page that returns our items
        Page<EmployeeAssessmentV2> page = mock(Page.class);
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

        PageIterable<EmployeeAssessmentV2> pageIterable = mock(PageIterable.class);
        SdkIterable<EmployeeAssessmentV2> itemsIterable = mock(SdkIterable.class);
        
        lenient().doReturn(itemsIterable).when(pageIterable).items();
        lenient().doReturn(Collections.<EmployeeAssessmentV2>emptyList().stream()).when(itemsIterable).stream();
        lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));
        
        // Ensure the GSI is returned when the method calls table.index()
        lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");
        
        // Create a mock Page that returns empty items
        Page<EmployeeAssessmentV2> page = mock(Page.class);
        lenient().doReturn(Collections.<EmployeeAssessmentV2>emptyList()).when(page).items();
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

        NaturalPersonV2 employee1 = NaturalPersonV2.builder()
            .name("John Doe")
            .email("john.doe@example.com")
            .build();

        NaturalPersonV2 employee2 = NaturalPersonV2.builder()
            .name("Jane Smith")
            .email("jane.smith@example.com")
            .build();

        EmployeeAssessmentV2 assessment1 = EmployeeAssessmentV2.builder()
            .id("assessment-1")
            .assessmentMatrixId(assessmentMatrixId)
            .teamId("team-123")
            .employee(employee1)
            .assessmentStatus(AssessmentStatus.IN_PROGRESS)
            .build();
        assessment1.setTenantId(tenantId);

        EmployeeAssessmentV2 assessment2 = EmployeeAssessmentV2.builder()
            .id("assessment-2")
            .assessmentMatrixId(assessmentMatrixId)
            .teamId("team-123")
            .employee(employee2)
            .assessmentStatus(AssessmentStatus.COMPLETED)
            .build();
        assessment2.setTenantId(tenantId);

        List<EmployeeAssessmentV2> expectedResults = Arrays.asList(assessment1, assessment2);

        PageIterable<EmployeeAssessmentV2> pageIterable = mock(PageIterable.class);
        SdkIterable<EmployeeAssessmentV2> itemsIterable = mock(SdkIterable.class);
        
        lenient().doReturn(itemsIterable).when(pageIterable).items();
        lenient().doReturn(expectedResults.stream()).when(itemsIterable).stream();
        lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));
        
        // Ensure the GSI is returned when the method calls table.index()
        lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");
        
        // Create a mock Page that returns our items
        Page<EmployeeAssessmentV2> page = mock(Page.class);
        lenient().doReturn(expectedResults).when(page).items();
        lenient().doReturn(Arrays.asList(page).stream()).when(pageIterable).stream();

        List<EmployeeAssessmentV2> result = repository.findByAssessmentMatrixId(assessmentMatrixId, tenantId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(assessment1, assessment2);
        verify(gsi).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void testFindByAssessmentMatrixId_ReturnsEmptyListWhenNoMatches() {
        String assessmentMatrixId = "matrix-123";
        String tenantId = "tenant-123";

        PageIterable<EmployeeAssessmentV2> pageIterable = mock(PageIterable.class);
        SdkIterable<EmployeeAssessmentV2> itemsIterable = mock(SdkIterable.class);
        
        lenient().doReturn(itemsIterable).when(pageIterable).items();
        lenient().doReturn(Collections.<EmployeeAssessmentV2>emptyList().stream()).when(itemsIterable).stream();
        lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));
        
        // Ensure the GSI is returned when the method calls table.index()
        lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");
        
        // Create a mock Page that returns empty items
        Page<EmployeeAssessmentV2> page = mock(Page.class);
        lenient().doReturn(Collections.<EmployeeAssessmentV2>emptyList()).when(page).items();
        lenient().doReturn(Arrays.asList(page).stream()).when(pageIterable).stream();

        List<EmployeeAssessmentV2> result = repository.findByAssessmentMatrixId(assessmentMatrixId, tenantId);

        assertThat(result).isEmpty();
        verify(gsi).query(any(QueryEnhancedRequest.class));
    }
}
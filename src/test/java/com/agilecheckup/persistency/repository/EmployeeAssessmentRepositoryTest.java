package com.agilecheckup.persistency.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.person.NaturalPerson;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

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

    NaturalPerson employee = NaturalPerson.builder().id("person-123").name("John Doe").email(employeeEmail).build();

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
    lenient().doReturn(Stream.of(existingAssessment)).when(itemsIterable).stream();
    lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));

    // Ensure the GSI is returned when the method calls table.index()
    lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");

    // Create a mock Page that returns our items
    Page<EmployeeAssessment> page = mock(Page.class);
    lenient().doReturn(List.of(existingAssessment)).when(page).items();
    lenient().doReturn(Stream.of(page)).when(pageIterable).stream();

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
    lenient().doReturn(Stream.<EmployeeAssessment>empty()).when(itemsIterable).stream();
    lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));

    // Ensure the GSI is returned when the method calls table.index()
    lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");

    // Create a mock Page that returns empty items
    Page<EmployeeAssessment> page = mock(Page.class);
    lenient().doReturn(List.of()).when(page).items();
    lenient().doReturn(Stream.of(page)).when(pageIterable).stream();

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

    assertThatThrownBy(() -> repository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrixId, employeeEmail)).isInstanceOf(RuntimeException.class)
                                                                                                                    .hasMessageContaining("GSI query failed");
  }

  @Test
  void testFindByAssessmentMatrixId_ReturnsFilteredResults() {
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-123";

    NaturalPerson employee1 = NaturalPerson.builder().name("John Doe").email("john.doe@example.com").build();

    NaturalPerson employee2 = NaturalPerson.builder().name("Jane Smith").email("jane.smith@example.com").build();

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

    List<EmployeeAssessment> expectedResults = List.of(assessment1, assessment2);

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
    lenient().doReturn(Stream.of(page)).when(pageIterable).stream();

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
    lenient().doReturn(Stream.empty()).when(itemsIterable).stream();
    lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));

    // Ensure the GSI is returned when the method calls table.index()
    lenient().doReturn(gsi).when(table).index("assessmentMatrixId-employeeEmail-index");

    // Create a mock Page that returns empty items
    Page<EmployeeAssessment> page = mock(Page.class);
    lenient().doReturn(List.of()).when(page).items();
    lenient().doReturn(Stream.of(page)).when(pageIterable).stream();

    List<EmployeeAssessment> result = repository.findByAssessmentMatrixId(assessmentMatrixId, tenantId);

    assertThat(result).isEmpty();
    verify(gsi).query(any(QueryEnhancedRequest.class));
  }

  @Test
  void testAreAllAssessmentsCompleted_ReturnsTrueWhenAllCompleted() {
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-123";

    // Mock the first query (looking for non-completed) - should return empty
    PageIterable<EmployeeAssessment> emptyPageIterable = mock(PageIterable.class);
    SdkIterable<EmployeeAssessment> emptyItemsIterable = mock(SdkIterable.class);
    Page<EmployeeAssessment> emptyPage = mock(Page.class);

    lenient().doReturn(List.of()).when(emptyPage).items();
    lenient().doReturn(Stream.of(emptyPage)).when(emptyPageIterable).stream();
    lenient().doReturn(emptyItemsIterable).when(emptyPageIterable).items();
    lenient().doReturn(Stream.empty()).when(emptyItemsIterable).stream();

    // Mock the second query (checking if any assessments exist) - should return some
    EmployeeAssessment completedAssessment = createTestAssessment("assessment-1", assessmentMatrixId, tenantId, AssessmentStatus.COMPLETED);

    PageIterable<EmployeeAssessment> existsPageIterable = mock(PageIterable.class);
    SdkIterable<EmployeeAssessment> existsItemsIterable = mock(SdkIterable.class);
    Page<EmployeeAssessment> existsPage = mock(Page.class);

    lenient().doReturn(List.of(completedAssessment)).when(existsPage).items();
    lenient().doReturn(Stream.of(existsPage)).when(existsPageIterable).stream();
    lenient().doReturn(existsItemsIterable).when(existsPageIterable).items();
    lenient().doReturn(Stream.of(completedAssessment)).when(existsItemsIterable).stream();

    // Configure mocks to return different results for different queries
    lenient().doReturn(emptyPageIterable, existsPageIterable).when(gsi).query(any(QueryEnhancedRequest.class));

    boolean result = repository.areAllAssessmentsCompleted(assessmentMatrixId, tenantId);

    assertThat(result).isTrue();
    verify(gsi, times(2)).query(any(QueryEnhancedRequest.class));
  }

  @Test
  void testAreAllAssessmentsCompleted_ReturnsFalseWhenSomeIncomplete() {
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-123";

    // Mock finding a non-completed assessment
    EmployeeAssessment inProgressAssessment = createTestAssessment("assessment-1", assessmentMatrixId, tenantId, AssessmentStatus.IN_PROGRESS);

    PageIterable<EmployeeAssessment> pageIterable = mock(PageIterable.class);
    SdkIterable<EmployeeAssessment> itemsIterable = mock(SdkIterable.class);
    Page<EmployeeAssessment> page = mock(Page.class);

    lenient().doReturn(List.of(inProgressAssessment)).when(page).items();
    lenient().doReturn(Stream.of(page)).when(pageIterable).stream();
    lenient().doReturn(itemsIterable).when(pageIterable).items();
    lenient().doReturn(Stream.of(inProgressAssessment)).when(itemsIterable).stream();
    lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));

    boolean result = repository.areAllAssessmentsCompleted(assessmentMatrixId, tenantId);

    assertThat(result).isFalse();
    verify(gsi, times(1)).query(any(QueryEnhancedRequest.class)); // Should stop after first query
  }

  @Test
  void testAreAllAssessmentsCompleted_ReturnsFalseWhenNoAssessmentsExist() {
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-123";

    // Create separate mocks for each query to avoid stream reuse issues

    // First query (looking for non-completed) - should return empty
    PageIterable<EmployeeAssessment> firstEmptyPageIterable = mock(PageIterable.class);
    SdkIterable<EmployeeAssessment> firstEmptyItemsIterable = mock(SdkIterable.class);
    Page<EmployeeAssessment> firstEmptyPage = mock(Page.class);

    lenient().doReturn(List.of()).when(firstEmptyPage).items();
    lenient().doReturn(Stream.of(firstEmptyPage)).when(firstEmptyPageIterable).stream();
    lenient().doReturn(firstEmptyItemsIterable).when(firstEmptyPageIterable).items();
    lenient().doReturn(Stream.empty()).when(firstEmptyItemsIterable).stream();

    // Second query (checking if any assessments exist) - should also return empty
    PageIterable<EmployeeAssessment> secondEmptyPageIterable = mock(PageIterable.class);
    SdkIterable<EmployeeAssessment> secondEmptyItemsIterable = mock(SdkIterable.class);
    Page<EmployeeAssessment> secondEmptyPage = mock(Page.class);

    lenient().doReturn(List.of()).when(secondEmptyPage).items();
    lenient().doReturn(Stream.of(secondEmptyPage)).when(secondEmptyPageIterable).stream();
    lenient().doReturn(secondEmptyItemsIterable).when(secondEmptyPageIterable).items();
    lenient().doReturn(Stream.empty()).when(secondEmptyItemsIterable).stream();

    // Configure mocks to return different instances for different queries
    lenient().doReturn(firstEmptyPageIterable, secondEmptyPageIterable)
             .when(gsi)
             .query(any(QueryEnhancedRequest.class));

    boolean result = repository.areAllAssessmentsCompleted(assessmentMatrixId, tenantId);

    assertThat(result).isFalse(); // Should return false when no assessments exist
    verify(gsi, times(2)).query(any(QueryEnhancedRequest.class));
  }

  @Test
  void testAreAllAssessmentsCompleted_HandlesException() {
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-123";

    RuntimeException gsiException = new RuntimeException("DynamoDB error");
    lenient().doThrow(gsiException).when(gsi).query(any(QueryEnhancedRequest.class));

    assertThatThrownBy(() -> repository.areAllAssessmentsCompleted(assessmentMatrixId, tenantId))
                                                                                                 .isInstanceOf(RuntimeException.class)
                                                                                                 .hasMessageContaining("Failed to check completion status for matrix ID: matrix-123");
  }

  @Test
  void testAreAllAssessmentsCompleted_WithNullParameters() {
    assertThatThrownBy(() -> repository.areAllAssessmentsCompleted(null, "tenant-123"))
                                                                                       .isInstanceOf(RuntimeException.class);

    assertThatThrownBy(() -> repository.areAllAssessmentsCompleted("matrix-123", null))
                                                                                       .isInstanceOf(RuntimeException.class);
  }

  @Test
  void testAreAllAssessmentsCompleted_WithEmptyParameters() {
    assertThatThrownBy(() -> repository.areAllAssessmentsCompleted("", "tenant-123"))
                                                                                     .isInstanceOf(RuntimeException.class);

    assertThatThrownBy(() -> repository.areAllAssessmentsCompleted("matrix-123", ""))
                                                                                     .isInstanceOf(RuntimeException.class);
  }

  // ===== Tests for countNonCompletedAssessments() =====

  @Test
  void testCountNonCompletedAssessments_ReturnsCorrectCount() {
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-123";

    List<EmployeeAssessment> nonCompletedAssessments = List.of(
                                                               createTestAssessment("assessment-1", assessmentMatrixId, tenantId, AssessmentStatus.IN_PROGRESS), createTestAssessment("assessment-2", assessmentMatrixId, tenantId, AssessmentStatus.IN_PROGRESS), createTestAssessment("assessment-3", assessmentMatrixId, tenantId, AssessmentStatus.IN_PROGRESS)
    );

    PageIterable<EmployeeAssessment> pageIterable = mock(PageIterable.class);
    SdkIterable<EmployeeAssessment> itemsIterable = mock(SdkIterable.class);
    Page<EmployeeAssessment> page = mock(Page.class);

    lenient().doReturn(nonCompletedAssessments).when(page).items();
    lenient().doReturn(Stream.of(page)).when(pageIterable).stream();
    lenient().doReturn(itemsIterable).when(pageIterable).items();
    lenient().doReturn(nonCompletedAssessments.stream()).when(itemsIterable).stream();
    lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));

    long count = repository.countNonCompletedAssessments(assessmentMatrixId, tenantId);

    assertThat(count).isEqualTo(3);
    verify(gsi).query(any(QueryEnhancedRequest.class));
  }

  @Test
  void testCountNonCompletedAssessments_ReturnsZeroWhenAllCompleted() {
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-123";

    PageIterable<EmployeeAssessment> emptyPageIterable = mock(PageIterable.class);
    SdkIterable<EmployeeAssessment> emptyItemsIterable = mock(SdkIterable.class);
    Page<EmployeeAssessment> emptyPage = mock(Page.class);

    lenient().doReturn(List.of()).when(emptyPage).items();
    lenient().doReturn(Stream.of(emptyPage)).when(emptyPageIterable).stream();
    lenient().doReturn(emptyItemsIterable).when(emptyPageIterable).items();
    lenient().doReturn(Stream.empty()).when(emptyItemsIterable).stream();
    lenient().doReturn(emptyPageIterable).when(gsi).query(any(QueryEnhancedRequest.class));

    long count = repository.countNonCompletedAssessments(assessmentMatrixId, tenantId);

    assertThat(count).isEqualTo(0);
    verify(gsi).query(any(QueryEnhancedRequest.class));
  }

  @Test
  void testCountNonCompletedAssessments_ReturnsZeroWhenNoAssessments() {
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-123";

    PageIterable<EmployeeAssessment> emptyPageIterable = mock(PageIterable.class);
    SdkIterable<EmployeeAssessment> emptyItemsIterable = mock(SdkIterable.class);
    Page<EmployeeAssessment> emptyPage = mock(Page.class);

    lenient().doReturn(List.of()).when(emptyPage).items();
    lenient().doReturn(Stream.of(emptyPage)).when(emptyPageIterable).stream();
    lenient().doReturn(emptyItemsIterable).when(emptyPageIterable).items();
    lenient().doReturn(Stream.empty()).when(emptyItemsIterable).stream();
    lenient().doReturn(emptyPageIterable).when(gsi).query(any(QueryEnhancedRequest.class));

    long count = repository.countNonCompletedAssessments(assessmentMatrixId, tenantId);

    assertThat(count).isEqualTo(0);
    verify(gsi).query(any(QueryEnhancedRequest.class));
  }

  @Test
  void testCountNonCompletedAssessments_HandlesException() {
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-123";

    RuntimeException gsiException = new RuntimeException("DynamoDB error");
    lenient().doThrow(gsiException).when(gsi).query(any(QueryEnhancedRequest.class));

    assertThatThrownBy(() -> repository.countNonCompletedAssessments(assessmentMatrixId, tenantId))
                                                                                                   .isInstanceOf(RuntimeException.class)
                                                                                                   .hasMessageContaining("Failed to count non-completed assessments for matrix ID: matrix-123");
  }

  @Test
  void testCountNonCompletedAssessments_WithLargeDataset() {
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-123";

    // Simulate a large dataset with multiple pages
    List<EmployeeAssessment> firstPageAssessments = List.of(
                                                            createTestAssessment("assessment-1", assessmentMatrixId, tenantId, AssessmentStatus.IN_PROGRESS), createTestAssessment("assessment-2", assessmentMatrixId, tenantId, AssessmentStatus.IN_PROGRESS)
    );

    List<EmployeeAssessment> secondPageAssessments = List.of(
                                                             createTestAssessment("assessment-3", assessmentMatrixId, tenantId, AssessmentStatus.IN_PROGRESS)
    );

    PageIterable<EmployeeAssessment> pageIterable = mock(PageIterable.class);
    SdkIterable<EmployeeAssessment> itemsIterable = mock(SdkIterable.class);
    Page<EmployeeAssessment> firstPage = mock(Page.class);
    Page<EmployeeAssessment> secondPage = mock(Page.class);

    lenient().doReturn(firstPageAssessments).when(firstPage).items();
    lenient().doReturn(secondPageAssessments).when(secondPage).items();
    lenient().doReturn(Stream.of(firstPage, secondPage)).when(pageIterable).stream();
    lenient().doReturn(itemsIterable).when(pageIterable).items();

    // Combine all items for the stream
    List<EmployeeAssessment> allAssessments = List.of(
                                                      firstPageAssessments.get(0), firstPageAssessments.get(1), secondPageAssessments.get(0)
    );
    lenient().doReturn(allAssessments.stream()).when(itemsIterable).stream();
    lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));

    long count = repository.countNonCompletedAssessments(assessmentMatrixId, tenantId);

    assertThat(count).isEqualTo(3);
    verify(gsi).query(any(QueryEnhancedRequest.class));
  }

  // ===== Edge Cases and Integration Tests =====

  @Test
  void testAreAllAssessmentsCompleted_WithMixedStatuses() {
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-123";

    // Test scenario where we have both COMPLETED and various non-completed statuses
    EmployeeAssessment draftAssessment = createTestAssessment("assessment-1", assessmentMatrixId, tenantId, AssessmentStatus.IN_PROGRESS);

    PageIterable<EmployeeAssessment> pageIterable = mock(PageIterable.class);
    SdkIterable<EmployeeAssessment> itemsIterable = mock(SdkIterable.class);
    Page<EmployeeAssessment> page = mock(Page.class);

    lenient().doReturn(List.of(draftAssessment)).when(page).items();
    lenient().doReturn(Stream.of(page)).when(pageIterable).stream();
    lenient().doReturn(itemsIterable).when(pageIterable).items();
    lenient().doReturn(Stream.of(draftAssessment)).when(itemsIterable).stream();
    lenient().doReturn(pageIterable).when(gsi).query(any(QueryEnhancedRequest.class));

    boolean result = repository.areAllAssessmentsCompleted(assessmentMatrixId, tenantId);

    assertThat(result).isFalse();
  }

  @Test
  void testCountVsCompletionConsistency() {
    // This test ensures that countNonCompletedAssessments and areAllAssessmentsCompleted are consistent
    String assessmentMatrixId = "matrix-123";
    String tenantId = "tenant-123";

    // Setup for count method
    List<EmployeeAssessment> nonCompletedAssessments = List.of(
                                                               createTestAssessment("assessment-1", assessmentMatrixId, tenantId, AssessmentStatus.IN_PROGRESS)
    );

    PageIterable<EmployeeAssessment> countPageIterable = mock(PageIterable.class);
    SdkIterable<EmployeeAssessment> countItemsIterable = mock(SdkIterable.class);
    Page<EmployeeAssessment> countPage = mock(Page.class);

    lenient().doReturn(nonCompletedAssessments).when(countPage).items();
    lenient().doReturn(Stream.of(countPage)).when(countPageIterable).stream();
    lenient().doReturn(countItemsIterable).when(countPageIterable).items();
    lenient().doReturn(nonCompletedAssessments.stream()).when(countItemsIterable).stream();

    // Setup for completion check method
    PageIterable<EmployeeAssessment> completionPageIterable = mock(PageIterable.class);
    SdkIterable<EmployeeAssessment> completionItemsIterable = mock(SdkIterable.class);
    Page<EmployeeAssessment> completionPage = mock(Page.class);

    lenient().doReturn(nonCompletedAssessments).when(completionPage).items();
    lenient().doReturn(Stream.of(completionPage)).when(completionPageIterable).stream();
    lenient().doReturn(completionItemsIterable).when(completionPageIterable).items();
    lenient().doReturn(nonCompletedAssessments.stream()).when(completionItemsIterable).stream();

    lenient().doReturn(countPageIterable, completionPageIterable).when(gsi).query(any(QueryEnhancedRequest.class));

    long count = repository.countNonCompletedAssessments(assessmentMatrixId, tenantId);
    boolean allCompleted = repository.areAllAssessmentsCompleted(assessmentMatrixId, tenantId);

    // If count > 0, then not all should be completed
    assertThat(count).isEqualTo(1);
    assertThat(allCompleted).isFalse();
  }

  // Helper method to create test assessments
  private EmployeeAssessment createTestAssessment(String id, String assessmentMatrixId, String tenantId, AssessmentStatus status) {
    NaturalPerson employee = NaturalPerson.builder()
                                          .id("person-" + id)
                                          .name("Test Employee")
                                          .email("test@example.com")
                                          .build();

    EmployeeAssessment assessment = EmployeeAssessment.builder()
                                                      .id(id)
                                                      .assessmentMatrixId(assessmentMatrixId)
                                                      .teamId("team-123")
                                                      .employee(employee)
                                                      .assessmentStatus(status)
                                                      .build();

    assessment.setTenantId(tenantId);
    return assessment;
  }
}
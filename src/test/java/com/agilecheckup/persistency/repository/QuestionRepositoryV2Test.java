package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.QuestionV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionRepositoryV2Test {

  @Mock
  private DynamoDbEnhancedClient enhancedClient;

  @Mock
  private DynamoDbTable<QuestionV2> table;

  @Mock
  private PageIterable<QuestionV2> pageIterable;

  @Mock
  private SdkIterable<Page<QuestionV2>> scanIterable;

  @Mock
  private Page<QuestionV2> page;

  private QuestionRepositoryV2 repository;

  @BeforeEach
  void setUp() {
    when(enhancedClient.table(eq("Question"), any())).thenReturn((DynamoDbTable) table);
    repository = new QuestionRepositoryV2(enhancedClient);
  }

  @Test
  void shouldSaveQuestion() {
    QuestionV2 question = createTestQuestion("question-1", "tenant-123");

    Optional<QuestionV2> result = repository.save(question);

    assertThat(result).isPresent();
    assertThat(result.get()).isSameAs(question);
    verify(table).putItem(question);
  }

  @Test
  void shouldFindQuestionById() {
    String id = "question-1";
    QuestionV2 question = createTestQuestion(id, "tenant-123");

    when(table.getItem(any(Key.class))).thenReturn(question);

    Optional<QuestionV2> result = repository.findById(id);

    assertThat(result).isPresent();
    assertThat(result.get()).isSameAs(question);
    verify(table).getItem(any(Key.class));
  }

  @Test
  void shouldFindQuestionsByAssessmentMatrixId() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";
    List<QuestionV2> expectedQuestions = Arrays.asList(
        createTestQuestion("question-1", tenantId, matrixId),
        createTestQuestion("question-2", tenantId, matrixId)
    );

    // Mock the scan operation - use raw SdkIterable type
    when(table.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);
    when(pageIterable.stream()).thenReturn(Stream.of(page));
    when(page.items()).thenReturn(expectedQuestions);

    List<QuestionV2> results = repository.findByAssessmentMatrixId(matrixId, tenantId);

    assertThat(results).hasSize(2);
    assertThat(results).extracting(QuestionV2::getId).containsExactly("question-1", "question-2");
    assertThat(results).allMatch(q -> q.getAssessmentMatrixId().equals(matrixId));
    assertThat(results).allMatch(q -> q.getTenantId().equals(tenantId));

    verify(table).scan(any(ScanEnhancedRequest.class));
  }

  @Test
  void shouldReturnTrueWhenQuestionsExistForCategory() {
    // Given
    String matrixId = "matrix-123";
    String categoryId = "category-456";
    String tenantId = "tenant-123";
    List<QuestionV2> existingQuestions = Arrays.asList(
        createTestQuestionWithCategory("question-1", tenantId, matrixId, categoryId)
    );

    // Mock the scan operation - use raw SdkIterable type
    when(table.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);
    when(pageIterable.stream()).thenReturn(Stream.of(page));
    when(page.items()).thenReturn(existingQuestions);

    // When
    boolean result = repository.existsByCategoryId(matrixId, categoryId, tenantId);

    // Then
    assertThat(result).isTrue();
    verify(table).scan(any(ScanEnhancedRequest.class));
  }

  @Test
  void shouldReturnFalseWhenNoQuestionsExistForCategory() {
    String matrixId = "matrix-123";
    String categoryId = "category-456";
    String tenantId = "tenant-123";

    // Mock empty scan result - use raw SdkIterable type
    when(table.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);
    when(pageIterable.stream()).thenReturn(Stream.of(page));
    when(page.items()).thenReturn(Arrays.asList());

    boolean result = repository.existsByCategoryId(matrixId, categoryId, tenantId);

    assertThat(result).isFalse();
    verify(table).scan(any(ScanEnhancedRequest.class));
  }

  @Test
  void shouldReturnEmptyOptionalWhenQuestionNotFound() {
    String id = "non-existent-id";
    when(table.getItem(any(Key.class))).thenReturn(null);

    Optional<QuestionV2> result = repository.findById(id);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldDeleteQuestionById() {
    String id = "question-1";

    boolean result = repository.deleteById(id);

    assertThat(result).isTrue();
    verify(table).deleteItem(any(Key.class));
  }

  private QuestionV2 createTestQuestion(String id, String tenantId) {
    return createTestQuestion(id, tenantId, "matrix-123");
  }

  private QuestionV2 createTestQuestion(String id, String tenantId, String matrixId) {
    return QuestionV2.builder()
        .id(id)
        .question("Test question " + id)
        .questionType(QuestionType.YES_NO)
        .tenantId(tenantId)
        .assessmentMatrixId(matrixId)
        .pillarId("pillar-123")
        .pillarName("Test Pillar")
        .categoryId("category-123")
        .categoryName("Test Category")
        .points(10.0)
        .build();
  }

  private QuestionV2 createTestQuestionWithCategory(String id, String tenantId, String matrixId, String categoryId) {
    return QuestionV2.builder()
        .id(id)
        .question("Test question " + id)
        .questionType(QuestionType.YES_NO)
        .tenantId(tenantId)
        .assessmentMatrixId(matrixId)
        .pillarId("pillar-123")
        .pillarName("Test Pillar")
        .categoryId(categoryId)
        .categoryName("Test Category")
        .points(10.0)
        .build();
  }
}
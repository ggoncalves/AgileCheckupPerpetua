package com.agilecheckup.persistency.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

@ExtendWith(MockitoExtension.class)
class QuestionRepositoryTest {

  @Mock
  private DynamoDbEnhancedClient enhancedClient;

  @Mock
  private DynamoDbTable<Question> table;

  @Mock
  private PageIterable<Question> pageIterable;

  @Mock
  private SdkIterable<Page<Question>> scanIterable;

  @Mock
  private Page<Question> page;

  private QuestionRepository repository;

  @BeforeEach
  void setUp() {
    when(enhancedClient.table(eq("Question"), any())).thenReturn((DynamoDbTable) table);
    repository = new QuestionRepository(enhancedClient);
  }

  @Test
  void shouldSaveQuestion() {
    Question question = createTestQuestion("question-1", "tenant-123");

    Optional<Question> result = repository.save(question);

    assertThat(result).isPresent();
    assertThat(result.get()).isSameAs(question);
    verify(table).putItem(question);
  }

  @Test
  void shouldFindQuestionById() {
    String id = "question-1";
    Question question = createTestQuestion(id, "tenant-123");

    when(table.getItem(any(Key.class))).thenReturn(question);

    Optional<Question> result = repository.findById(id);

    assertThat(result).isPresent();
    assertThat(result.get()).isSameAs(question);
    verify(table).getItem(any(Key.class));
  }

  @Test
  void shouldFindQuestionsByAssessmentMatrixId() {
    String matrixId = "matrix-123";
    String tenantId = "tenant-123";
    List<Question> expectedQuestions = Arrays.asList(
                                                     createTestQuestion("question-1", tenantId, matrixId), createTestQuestion("question-2", tenantId, matrixId)
    );

    // Mock the scan operation - use raw SdkIterable type
    when(table.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);
    when(pageIterable.stream()).thenReturn(Stream.of(page));
    when(page.items()).thenReturn(expectedQuestions);

    List<Question> results = repository.findByAssessmentMatrixId(matrixId, tenantId);

    assertThat(results).hasSize(2);
    assertThat(results).extracting(Question::getId).containsExactly("question-1", "question-2");
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
    List<Question> existingQuestions = Arrays.asList(
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

    Optional<Question> result = repository.findById(id);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldDeleteQuestionById() {
    String id = "question-1";

    boolean result = repository.deleteById(id);

    assertThat(result).isTrue();
    verify(table).deleteItem(any(Key.class));
  }

  private Question createTestQuestion(String id, String tenantId) {
    return createTestQuestion(id, tenantId, "matrix-123");
  }

  private Question createTestQuestion(String id, String tenantId, String matrixId) {
    return Question.builder()
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

  private Question createTestQuestionWithCategory(String id, String tenantId, String matrixId, String categoryId) {
    return Question.builder()
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
package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.AnswerV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerRepositoryV2Test {

    @Mock
    private DynamoDbEnhancedClient mockEnhancedClient;

    @Mock
    private DynamoDbTable<AnswerV2> mockTable;

    @Mock
    private DynamoDbIndex<AnswerV2> mockIndex;

    @Mock
    private PageIterable<AnswerV2> mockPageIterable;

    @Mock
    private Page<AnswerV2> mockPage;

    private AnswerRepositoryV2 repository;

    @BeforeEach
    void setUp() {
        when(mockEnhancedClient.table(eq("Answer"), any())).thenReturn((DynamoDbTable) mockTable);
        repository = new AnswerRepositoryV2(mockEnhancedClient, "Answer");
    }

    @Test
    void shouldFindByEmployeeAssessmentId() {
        // Given
        String employeeAssessmentId = "assessment-123";
        String tenantId = "tenant-123";

        AnswerV2 answer1 = createMockAnswer("answer-1", employeeAssessmentId, "question-1", tenantId);
        AnswerV2 answer2 = createMockAnswer("answer-2", employeeAssessmentId, "question-2", tenantId);
        List<AnswerV2> expectedAnswers = Arrays.asList(answer1, answer2);

        when(mockTable.index("employeeAssessmentId-tenantId-index")).thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);
        when(mockPageIterable.stream()).thenReturn(Arrays.asList(mockPage).stream());
        when(mockPage.items()).thenReturn(expectedAnswers);

        // When
        List<AnswerV2> result = repository.findByEmployeeAssessmentId(employeeAssessmentId, tenantId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedAnswers);
        
        verify(mockTable).index("employeeAssessmentId-tenantId-index");
        verify(mockIndex).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void shouldFindAnsweredQuestionIds() {
        // Given
        String employeeAssessmentId = "assessment-123";
        String tenantId = "tenant-123";

        AnswerV2 answer1 = createMockAnswer("answer-1", employeeAssessmentId, "question-1", tenantId);
        AnswerV2 answer2 = createMockAnswer("answer-2", employeeAssessmentId, "question-2", tenantId);
        List<AnswerV2> answers = Arrays.asList(answer1, answer2);

        when(mockTable.index("employeeAssessmentId-tenantId-index")).thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);
        when(mockPageIterable.stream()).thenReturn(Arrays.asList(mockPage).stream());
        when(mockPage.items()).thenReturn(answers);

        // When
        Set<String> result = repository.findAnsweredQuestionIds(employeeAssessmentId, tenantId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder("question-1", "question-2");
        
        verify(mockTable).index("employeeAssessmentId-tenantId-index");
        verify(mockIndex).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void shouldFindByEmployeeAssessmentIdAndQuestionId() {
        // Given
        String employeeAssessmentId = "assessment-123";
        String questionId = "question-1";
        String tenantId = "tenant-123";

        AnswerV2 answer1 = createMockAnswer("answer-1", employeeAssessmentId, "question-1", tenantId);
        AnswerV2 answer2 = createMockAnswer("answer-2", employeeAssessmentId, "question-2", tenantId);
        List<AnswerV2> answers = Arrays.asList(answer1, answer2);

        when(mockTable.index("employeeAssessmentId-tenantId-index")).thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);
        when(mockPageIterable.stream()).thenReturn(Arrays.asList(mockPage).stream());
        when(mockPage.items()).thenReturn(answers);

        // When
        Optional<AnswerV2> result = repository.findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getQuestionId()).isEqualTo("question-1");
        assertThat(result.get().getEmployeeAssessmentId()).isEqualTo(employeeAssessmentId);
        
        verify(mockTable).index("employeeAssessmentId-tenantId-index");
        verify(mockIndex).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void shouldReturnEmptyWhenNoMatchingQuestionFound() {
        // Given
        String employeeAssessmentId = "assessment-123";
        String questionId = "non-existent-question";
        String tenantId = "tenant-123";

        AnswerV2 answer1 = createMockAnswer("answer-1", employeeAssessmentId, "question-1", tenantId);
        List<AnswerV2> answers = Arrays.asList(answer1);

        when(mockTable.index("employeeAssessmentId-tenantId-index")).thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);
        when(mockPageIterable.stream()).thenReturn(Arrays.asList(mockPage).stream());
        when(mockPage.items()).thenReturn(answers);

        // When
        Optional<AnswerV2> result = repository.findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleEmptyResultsForEmployeeAssessmentId() {
        // Given
        String employeeAssessmentId = "assessment-123";
        String tenantId = "tenant-123";

        when(mockTable.index("employeeAssessmentId-tenantId-index")).thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);
        when(mockPageIterable.stream()).thenReturn(Arrays.asList(mockPage).stream());
        when(mockPage.items()).thenReturn(Arrays.asList());

        // When
        List<AnswerV2> result = repository.findByEmployeeAssessmentId(employeeAssessmentId, tenantId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleEmptyResultsForAnsweredQuestionIds() {
        // Given
        String employeeAssessmentId = "assessment-123";
        String tenantId = "tenant-123";

        when(mockTable.index("employeeAssessmentId-tenantId-index")).thenReturn(mockIndex);
        when(mockIndex.query(any(QueryEnhancedRequest.class))).thenReturn(mockPageIterable);
        when(mockPageIterable.stream()).thenReturn(Arrays.asList(mockPage).stream());
        when(mockPage.items()).thenReturn(Arrays.asList());

        // When
        Set<String> result = repository.findAnsweredQuestionIds(employeeAssessmentId, tenantId);

        // Then
        assertThat(result).isEmpty();
    }

    private AnswerV2 createMockAnswer(String answerId, String employeeAssessmentId, String questionId, String tenantId) {
        return AnswerV2.builder()
                .id(answerId)
                .employeeAssessmentId(employeeAssessmentId)
                .answeredAt(LocalDateTime.now())
                .pillarId("pillar-123")
                .categoryId("category-123")
                .questionId(questionId)
                .questionType(QuestionType.YES_NO)
                .value("Yes")
                .tenantId(tenantId)
                .build();
    }
}
package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.agilecheckup.service.exception.InvalidLocalDateTimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.agilecheckup.util.TestObjectFactory.createMockedQuestionV2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @InjectMocks
    private AnswerService answerService;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private EmployeeAssessmentService employeeAssessmentService;

    @Mock
    private QuestionService questionService;

    @Mock
    private AssessmentMatrixService assessmentMatrixService;

    private Question testQuestion;
    private EmployeeAssessment testEmployeeAssessment;
    private AssessmentMatrix testAssessmentMatrix;

    @BeforeEach
    void setUp() {
        testQuestion = createMockedQuestionV2();
        testEmployeeAssessment = createMockedEmployeeAssessmentV2();
        testAssessmentMatrix = createMockedAssessmentMatrixV2();

        lenient().doReturn(Optional.of(testQuestion)).when(questionService).findById(anyString());
        lenient().doReturn(Optional.of(testEmployeeAssessment)).when(employeeAssessmentService).findById(anyString());
        lenient().doReturn(Optional.of(testAssessmentMatrix)).when(assessmentMatrixService).findById(anyString());
    }

    private EmployeeAssessment createMockedEmployeeAssessmentV2() {
        EmployeeAssessment assessment = new EmployeeAssessment();
        assessment.setId("assessment-123");
        assessment.setAssessmentMatrixId("matrix-123");
        assessment.setAnsweredQuestionCount(5);
        assessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);
        assessment.setTenantId("tenant-123");
        return assessment;
    }

    private AssessmentMatrix createMockedAssessmentMatrixV2() {
        AssessmentMatrix matrix = new AssessmentMatrix();
        matrix.setId("matrix-123");
        matrix.setQuestionCount(10);
        matrix.setTenantId("tenant-123");
        return matrix;
    }

    @Test
    void shouldCreateNewAnswerWhenNoExistingAnswer() {
        // Given
        String employeeAssessmentId = "assessment-123";
        String questionId = "question-123";
        LocalDateTime answeredAt = LocalDateTime.now();
        String value = "Yes";
        String tenantId = "tenant-123";
        String notes = "Test notes";

        when(answerRepository.findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId))
                .thenReturn(Optional.empty());

        Answer savedAnswer = createMockAnswerV2("answer-123", employeeAssessmentId, questionId, tenantId);
        doReturn(Optional.of(savedAnswer)).when(answerRepository).save(any(Answer.class));

        // When
        Optional<Answer> result = answerService.create(employeeAssessmentId, questionId, answeredAt, value, tenantId, notes);

        // Then
        assertThat(result).isPresent();
        verify(answerRepository).findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);
        verify(answerRepository).save(any(Answer.class));
        verify(employeeAssessmentService).incrementAnsweredQuestionCount(employeeAssessmentId);
    }

    @Test
    void shouldUpdateExistingAnswerWhenDuplicateDetected() {
        // Given
        String employeeAssessmentId = "assessment-123";
        String questionId = "question-123";
        LocalDateTime answeredAt = LocalDateTime.now();
        String value = "No";
        String tenantId = "tenant-123";
        String notes = "Updated notes";

        Answer existingAnswer = createMockAnswerV2("answer-123", employeeAssessmentId, questionId, tenantId);
        when(answerRepository.findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId))
                .thenReturn(Optional.of(existingAnswer));

        doReturn(Optional.of(existingAnswer)).when(answerRepository).save(any(Answer.class));

        // When
        Optional<Answer> result = answerService.create(employeeAssessmentId, questionId, answeredAt, value, tenantId, notes);

        // Then
        assertThat(result).isPresent();
        verify(answerRepository).findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);
        verify(answerRepository).save(existingAnswer);
        verify(employeeAssessmentService).incrementAnsweredQuestionCount(employeeAssessmentId);
    }

    @Test
    void shouldUpdateAnswerById() {
        // Given
        String answerId = "answer-123";
        LocalDateTime answeredAt = LocalDateTime.now();
        String value = "Updated value";
        String notes = "Updated notes";

        Answer existingAnswer = createMockAnswerV2(answerId, "assessment-123", "question-123", "tenant-123");
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(existingAnswer));
        doReturn(Optional.of(existingAnswer)).when(answerRepository).save(any(Answer.class));

        // When
        Optional<Answer> result = answerService.update(answerId, answeredAt, value, notes);

        // Then
        assertThat(result).isPresent();
        verify(answerRepository).findById(answerId);
        verify(answerRepository).save(existingAnswer);
        verify(employeeAssessmentService).incrementAnsweredQuestionCount("assessment-123");
    }

    @Test
    void shouldReturnEmptyWhenUpdatingNonExistentAnswer() {
        // Given
        String answerId = "non-existent";
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());

        // When
        Optional<Answer> result = answerService.update(answerId, LocalDateTime.now(), "value", "notes");

        // Then
        assertThat(result).isEmpty();
        verify(answerRepository).findById(answerId);
        verify(answerRepository, never()).save(any(Answer.class));
    }

    @Test
    void shouldThrowExceptionForFutureAnsweredAt() {
        // Given
        LocalDateTime futureTime = LocalDateTime.now().plusHours(2);

        // When & Then
        assertThatThrownBy(() -> answerService.create("assessment-123", "question-123", futureTime, "Yes", "tenant-123", null))
                .isInstanceOf(InvalidLocalDateTimeException.class);
    }

    @Test
    void shouldThrowExceptionWhenQuestionNotFound() {
        // Given
        when(questionService.findById(anyString())).thenReturn(Optional.empty());
        when(answerRepository.findByEmployeeAssessmentIdAndQuestionId(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerService.create("assessment-123", "non-existent", LocalDateTime.now(), "Yes", "tenant-123", null))
                .isInstanceOf(InvalidIdReferenceException.class);
    }

    @Test
    void shouldThrowExceptionWhenEmployeeAssessmentNotFound() {
        // Given
        when(employeeAssessmentService.findById(anyString())).thenReturn(Optional.empty());
        when(answerRepository.findByEmployeeAssessmentIdAndQuestionId(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> answerService.create("non-existent", "question-123", LocalDateTime.now(), "Yes", "tenant-123", null))
                .isInstanceOf(InvalidIdReferenceException.class);
    }

    @Test
    void shouldFindByEmployeeAssessmentId() {
        // Given
        String employeeAssessmentId = "assessment-123";
        String tenantId = "tenant-123";
        List<Answer> expectedAnswers = Arrays.asList(
                createMockAnswerV2("answer-1", employeeAssessmentId, "question-1", tenantId),
                createMockAnswerV2("answer-2", employeeAssessmentId, "question-2", tenantId)
        );

        when(answerRepository.findByEmployeeAssessmentId(employeeAssessmentId, tenantId))
                .thenReturn(expectedAnswers);

        // When
        List<Answer> result = answerService.findByEmployeeAssessmentId(employeeAssessmentId, tenantId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedAnswers);
        verify(answerRepository).findByEmployeeAssessmentId(employeeAssessmentId, tenantId);
    }

    @Test
    void shouldFindAnsweredQuestionIds() {
        // Given
        String employeeAssessmentId = "assessment-123";
        String tenantId = "tenant-123";
        Set<String> expectedQuestionIds = Set.of("question-1", "question-2", "question-3");

        when(answerRepository.findAnsweredQuestionIds(employeeAssessmentId, tenantId))
                .thenReturn(expectedQuestionIds);

        // When
        Set<String> result = answerService.findAnsweredQuestionIds(employeeAssessmentId, tenantId);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedQuestionIds);
        verify(answerRepository).findAnsweredQuestionIds(employeeAssessmentId, tenantId);
    }

    @Test
    void shouldUpdateAssessmentStatusToCompletedWhenAllQuestionsAnswered() {
        // Given
        String employeeAssessmentId = "assessment-123";
        testEmployeeAssessment.setAnsweredQuestionCount(10);
        testEmployeeAssessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);
        testAssessmentMatrix.setQuestionCount(10);

        Answer savedAnswer = createMockAnswerV2("answer-123", employeeAssessmentId, "question-123", "tenant-123");
        doReturn(Optional.of(savedAnswer)).when(answerRepository).save(any(Answer.class));
        when(answerRepository.findByEmployeeAssessmentIdAndQuestionId(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // When
        answerService.create(employeeAssessmentId, "question-123", LocalDateTime.now(), "Yes", "tenant-123", null);

        // Then
        verify(employeeAssessmentService).incrementAnsweredQuestionCount(employeeAssessmentId);
        verify(employeeAssessmentService).updateLastActivityDate(employeeAssessmentId);
        verify(employeeAssessmentService).save(testEmployeeAssessment);
        verify(employeeAssessmentService).updateEmployeeAssessmentScore(employeeAssessmentId, "tenant-123");
    }

    @Test
    void shouldNotUpdateLastActivityDateWhenAssessmentCompleted() {
        // Given
        testEmployeeAssessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
        
        Answer savedAnswer = createMockAnswerV2("answer-123", "assessment-123", "question-123", "tenant-123");
        doReturn(Optional.of(savedAnswer)).when(answerRepository).save(any(Answer.class));
        when(answerRepository.findByEmployeeAssessmentIdAndQuestionId(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // When
        answerService.create("assessment-123", "question-123", LocalDateTime.now(), "Yes", "tenant-123", null);

        // Then
        verify(employeeAssessmentService).incrementAnsweredQuestionCount("assessment-123");
        verify(employeeAssessmentService, never()).updateLastActivityDate("assessment-123");
    }


    private Answer createMockAnswerV2(String answerId, String employeeAssessmentId, String questionId, String tenantId) {
        return Answer.builder()
                .id(answerId)
                .employeeAssessmentId(employeeAssessmentId)
                .answeredAt(LocalDateTime.now())
                .pillarId("pillar-123")
                .categoryId("category-123")
                .questionId(questionId)
                .questionType(QuestionType.YES_NO)
                .value("Yes")
                .score(10.0)
                .tenantId(tenantId)
                .build();
    }
}
package com.agilecheckup.service;

import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agilecheckup.persistency.entity.AssessmentConfiguration;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.QuestionNavigationType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.service.dto.AnswerWithProgressResponse;
import com.agilecheckup.service.exception.InvalidIdReferenceException;

@ExtendWith(MockitoExtension.class)
class AssessmentNavigationServiceTest {

  @Mock
  private QuestionService questionService;

  @Mock
  private AnswerService answerService;

  @Mock
  private EmployeeAssessmentService employeeAssessmentService;

  @Mock
  private AssessmentMatrixService assessmentMatrixService;

  private AssessmentNavigationService assessmentNavigationService;

  private static final String EMPLOYEE_ASSESSMENT_ID = "ea-123";
  private static final String TENANT_ID = "tenant-123";
  private static final String MATRIX_ID = "matrix-123";
  private static final String QUESTION_ID = "question-123";
  private static final LocalDateTime ANSWERED_AT = LocalDateTime.of(2024, 1, 15, 10, 30);

  @BeforeEach
  void setUp() {
    assessmentNavigationService = new AssessmentNavigationService(
                                                                  questionService, answerService, employeeAssessmentService, assessmentMatrixService);
  }

  @Test
  void shouldGetNextUnansweredQuestion_WhenQuestionsAvailable() {
    // Given
    EmployeeAssessment assessment = createMockAssessment(AssessmentStatus.IN_PROGRESS, 5);
    AssessmentMatrix matrix = createMockMatrix(10);
    List<Question> questions = createMockQuestions(3);
    Set<String> answeredQuestionIds = new HashSet<>(Arrays.asList("q1", "q2"));
    AssessmentConfiguration config = createMockConfiguration(QuestionNavigationType.SEQUENTIAL);

    when(employeeAssessmentService.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
    when(assessmentMatrixService.findById(MATRIX_ID)).thenReturn(Optional.of(matrix));
    when(answerService.findAnsweredQuestionIds(EMPLOYEE_ASSESSMENT_ID, TENANT_ID)).thenReturn(answeredQuestionIds);
    when(questionService.findByAssessmentMatrixId(MATRIX_ID, TENANT_ID)).thenReturn(questions);
    when(assessmentMatrixService.getEffectiveConfiguration(matrix)).thenReturn(config);

    // When
    AnswerWithProgressResponse response = assessmentNavigationService.getNextUnansweredQuestion(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getQuestion()).isEqualTo(questions.get(2)); // q3 is the first unanswered question
    assertThat(response.getCurrentProgress()).isEqualTo(5);
    assertThat(response.getTotalQuestions()).isEqualTo(10);
    assertThat(response.getExistingAnswer()).isNull();

    verify(employeeAssessmentService).findById(EMPLOYEE_ASSESSMENT_ID);
    verify(assessmentMatrixService).findById(MATRIX_ID);
    verify(answerService).findAnsweredQuestionIds(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);
    verify(questionService).findByAssessmentMatrixId(MATRIX_ID, TENANT_ID);
  }

  @Test
  void shouldReturnNullQuestion_WhenAllQuestionsAnswered() {
    // Given
    EmployeeAssessment assessment = createMockAssessment(AssessmentStatus.IN_PROGRESS, 3);
    AssessmentMatrix matrix = createMockMatrix(3);
    List<Question> questions = createMockQuestions(3);
    Set<String> answeredQuestionIds = new HashSet<>(Arrays.asList("q1", "q2", "q3"));

    when(employeeAssessmentService.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
    when(assessmentMatrixService.findById(MATRIX_ID)).thenReturn(Optional.of(matrix));
    when(answerService.findAnsweredQuestionIds(EMPLOYEE_ASSESSMENT_ID, TENANT_ID)).thenReturn(answeredQuestionIds);
    when(questionService.findByAssessmentMatrixId(MATRIX_ID, TENANT_ID)).thenReturn(questions);

    // When
    AnswerWithProgressResponse response = assessmentNavigationService.getNextUnansweredQuestion(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getQuestion()).isNull();
    assertThat(response.getCurrentProgress()).isEqualTo(3);
    assertThat(response.getTotalQuestions()).isEqualTo(3);

    // Verify assessment was updated to COMPLETED
    verify(employeeAssessmentService).finalizeAssessmentIfCompleted(any());
  }

  @Test
  void shouldUpdateStatusToInProgress_WhenStatusIsConfirmed() {
    // Given
    EmployeeAssessment assessment = createMockAssessment(AssessmentStatus.CONFIRMED, 0);
    AssessmentMatrix matrix = createMockMatrix(5);
    List<Question> questions = createMockQuestions(2);
    AssessmentConfiguration config = createMockConfiguration(QuestionNavigationType.RANDOM);

    when(employeeAssessmentService.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
    when(assessmentMatrixService.findById(MATRIX_ID)).thenReturn(Optional.of(matrix));
    when(answerService.findAnsweredQuestionIds(EMPLOYEE_ASSESSMENT_ID, TENANT_ID)).thenReturn(Collections.emptySet());
    when(questionService.findByAssessmentMatrixId(MATRIX_ID, TENANT_ID)).thenReturn(questions);
    when(assessmentMatrixService.getEffectiveConfiguration(matrix)).thenReturn(config);

    // When
    assessmentNavigationService.getNextUnansweredQuestion(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);

    // Then
    ArgumentCaptor<EmployeeAssessment> assessmentCaptor = ArgumentCaptor.forClass(EmployeeAssessment.class);
    verify(employeeAssessmentService).update(assessmentCaptor.capture());
    assertThat(assessmentCaptor.getValue().getAssessmentStatus()).isEqualTo(AssessmentStatus.IN_PROGRESS);
  }

  @Test
  void shouldNotUpdateStatus_WhenStatusIsAlreadyInProgress() {
    // Given
    EmployeeAssessment assessment = createMockAssessment(AssessmentStatus.IN_PROGRESS, 2);
    AssessmentMatrix matrix = createMockMatrix(5);
    List<Question> questions = createMockQuestions(2);
    AssessmentConfiguration config = createMockConfiguration(QuestionNavigationType.SEQUENTIAL);

    when(employeeAssessmentService.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
    when(assessmentMatrixService.findById(MATRIX_ID)).thenReturn(Optional.of(matrix));
    when(answerService.findAnsweredQuestionIds(EMPLOYEE_ASSESSMENT_ID, TENANT_ID)).thenReturn(Collections.emptySet());
    when(questionService.findByAssessmentMatrixId(MATRIX_ID, TENANT_ID)).thenReturn(questions);
    when(assessmentMatrixService.getEffectiveConfiguration(matrix)).thenReturn(config);

    // When
    assessmentNavigationService.getNextUnansweredQuestion(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);

    // Then
    verify(employeeAssessmentService, never()).update(any(EmployeeAssessment.class));
  }

  @Test
  void shouldSelectRandomQuestion_WhenNavigationTypeIsRandom() {
    // Given
    EmployeeAssessment assessment = createMockAssessment(AssessmentStatus.IN_PROGRESS, 0);
    AssessmentMatrix matrix = createMockMatrix(5);
    List<Question> questions = createMockQuestions(3);
    AssessmentConfiguration config = createMockConfiguration(QuestionNavigationType.RANDOM);

    when(employeeAssessmentService.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
    when(assessmentMatrixService.findById(MATRIX_ID)).thenReturn(Optional.of(matrix));
    when(answerService.findAnsweredQuestionIds(EMPLOYEE_ASSESSMENT_ID, TENANT_ID)).thenReturn(Collections.emptySet());
    when(questionService.findByAssessmentMatrixId(MATRIX_ID, TENANT_ID)).thenReturn(questions);
    when(assessmentMatrixService.getEffectiveConfiguration(matrix)).thenReturn(config);

    // When
    AnswerWithProgressResponse response = assessmentNavigationService.getNextUnansweredQuestion(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);

    // Then
    assertThat(response.getQuestion()).isIn(questions);
  }

  @Test
    void shouldThrowException_WhenEmployeeAssessmentNotFound() {
        // Given
        when(employeeAssessmentService.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> assessmentNavigationService.getNextUnansweredQuestion(EMPLOYEE_ASSESSMENT_ID, TENANT_ID))
            .isInstanceOf(InvalidIdReferenceException.class)
            .hasMessageContaining(EMPLOYEE_ASSESSMENT_ID);

        verify(assessmentMatrixService, never()).findById(anyString());
    }

  @Test
  void shouldThrowException_WhenAssessmentMatrixNotFound() {
    // Given
    EmployeeAssessment assessment = createMockAssessment(AssessmentStatus.IN_PROGRESS, 0);
    when(employeeAssessmentService.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
    when(assessmentMatrixService.findById(MATRIX_ID)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> assessmentNavigationService.getNextUnansweredQuestion(EMPLOYEE_ASSESSMENT_ID, TENANT_ID)).isInstanceOf(InvalidIdReferenceException.class)
                                                                                                                      .hasMessageContaining(MATRIX_ID);
  }

  @Test
  void shouldSaveAnswerAndGetNext_WhenAnswerSaved() {
    // Given
    String questionId = "question-456";
    String value = "Yes";
    String notes = "Test notes";

    Answer savedAnswer = createMockAnswer();
    EmployeeAssessment assessment = createMockAssessment(AssessmentStatus.IN_PROGRESS, 1);
    AssessmentMatrix matrix = createMockMatrix(5);
    List<Question> questions = createMockQuestions(2);
    AssessmentConfiguration config = createMockConfiguration(QuestionNavigationType.SEQUENTIAL);

    doReturn(Optional.of(savedAnswer)).when(answerService).create(
                                                                  eq(EMPLOYEE_ASSESSMENT_ID), eq(questionId), eq(ANSWERED_AT), eq(value), eq(TENANT_ID), eq(notes));
    when(employeeAssessmentService.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
    when(assessmentMatrixService.findById(MATRIX_ID)).thenReturn(Optional.of(matrix));
    when(answerService.findAnsweredQuestionIds(EMPLOYEE_ASSESSMENT_ID, TENANT_ID)).thenReturn(Collections.emptySet());
    when(questionService.findByAssessmentMatrixId(MATRIX_ID, TENANT_ID)).thenReturn(questions);
    when(assessmentMatrixService.getEffectiveConfiguration(matrix)).thenReturn(config);

    // When
    AnswerWithProgressResponse response = assessmentNavigationService.saveAnswerAndGetNext(
                                                                                           EMPLOYEE_ASSESSMENT_ID, questionId, ANSWERED_AT, value, TENANT_ID, notes);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getQuestion()).isEqualTo(questions.get(0));
    assertThat(response.getCurrentProgress()).isEqualTo(1);

    verify(answerService).create(EMPLOYEE_ASSESSMENT_ID, questionId, ANSWERED_AT, value, TENANT_ID, notes);
    verify(employeeAssessmentService).findById(EMPLOYEE_ASSESSMENT_ID);
  }

  @Test
  void shouldThrowException_WhenAnswerSaveFails() {
    // Given
    doReturn(Optional.empty()).when(answerService).create(
                                                          anyString(), anyString(), any(LocalDateTime.class), anyString(), anyString(), anyString());

    // When/Then
    assertThatThrownBy(() -> assessmentNavigationService.saveAnswerAndGetNext(
                                                                              EMPLOYEE_ASSESSMENT_ID, QUESTION_ID, ANSWERED_AT, "Yes", TENANT_ID, "notes")).isInstanceOf(RuntimeException.class)
                                                                                                                                                           .hasMessage("Failed to save answer - answerService.create returned empty Optional");
  }

  @Test
  void shouldFilterUnansweredQuestions_WhenSomeQuestionsAnswered() {
    // Given
    EmployeeAssessment assessment = createMockAssessment(AssessmentStatus.IN_PROGRESS, 2);
    AssessmentMatrix matrix = createMockMatrix(5);

    Question q1 = createMockQuestion("q1", "Question 1");
    Question q2 = createMockQuestion("q2", "Question 2");
    Question q3 = createMockQuestion("q3", "Question 3");
    List<Question> allQuestions = Arrays.asList(q1, q2, q3);

    Set<String> answeredQuestionIds = new HashSet<>(Arrays.asList("q1"));
    AssessmentConfiguration config = createMockConfiguration(QuestionNavigationType.SEQUENTIAL);

    when(employeeAssessmentService.findById(EMPLOYEE_ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
    when(assessmentMatrixService.findById(MATRIX_ID)).thenReturn(Optional.of(matrix));
    when(answerService.findAnsweredQuestionIds(EMPLOYEE_ASSESSMENT_ID, TENANT_ID)).thenReturn(answeredQuestionIds);
    when(questionService.findByAssessmentMatrixId(MATRIX_ID, TENANT_ID)).thenReturn(allQuestions);
    when(assessmentMatrixService.getEffectiveConfiguration(matrix)).thenReturn(config);

    // When
    AnswerWithProgressResponse response = assessmentNavigationService.getNextUnansweredQuestion(EMPLOYEE_ASSESSMENT_ID, TENANT_ID);

    // Then
    assertThat(response.getQuestion()).isIn(q2, q3); // Should not be q1 (already answered)
  }

  // Helper methods for creating test objects
  private EmployeeAssessment createMockAssessment(AssessmentStatus status, int answeredCount) {
    EmployeeAssessment assessment = new EmployeeAssessment();
    assessment.setId(EMPLOYEE_ASSESSMENT_ID);
    assessment.setAssessmentMatrixId(MATRIX_ID);
    assessment.setAssessmentStatus(status);
    assessment.setAnsweredQuestionCount(answeredCount);
    return assessment;
  }

  private AssessmentMatrix createMockMatrix(int questionCount) {
    AssessmentMatrix matrix = new AssessmentMatrix();
    matrix.setId(MATRIX_ID);
    matrix.setQuestionCount(questionCount);
    return matrix;
  }

  private List<Question> createMockQuestions(int count) {
    List<Question> questions = new java.util.ArrayList<>();
    for (int i = 1; i <= count; i++) {
      questions.add(createMockQuestion("q" + i, "Question " + i));
    }
    return questions;
  }

  private Question createMockQuestion(String id, String text) {
    Question question = createMockedQuestion(id, 10.0);
    question.setQuestion(text);
    question.setTenantId(TENANT_ID);
    question.setAssessmentMatrixId(MATRIX_ID);
    return question;
  }

  private AssessmentConfiguration createMockConfiguration(QuestionNavigationType navigationType) {
    return AssessmentConfiguration.builder().navigationMode(navigationType).build();
  }

  private Answer createMockAnswer() {
    Answer answer = new Answer();
    answer.setId("answer-123");
    answer.setEmployeeAssessmentId(EMPLOYEE_ASSESSMENT_ID);
    answer.setQuestionId(QUESTION_ID);
    answer.setValue("Yes");
    answer.setScore(10.0);
    answer.setTenantId(TENANT_ID);
    return answer;
  }
}
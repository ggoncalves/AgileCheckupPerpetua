package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentConfiguration;
import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.QuestionNavigationType;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.service.dto.AnswerWithProgressResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.agilecheckup.util.TestObjectFactory.EMPLOYEE_NAME_JOHN;
import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.createMockedAnswer;
import static com.agilecheckup.util.TestObjectFactory.createMockedEmployeeAssessment;
import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

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

  @InjectMocks
  private AssessmentNavigationService assessmentNavigationService;

  private EmployeeAssessment employeeAssessment;
  private AssessmentMatrix assessmentMatrix;
  private AssessmentConfiguration configuration;
  private Question question1;
  private Question question2;
  private Question question3;

  @BeforeEach
  void setUp() {
    String employeeAssessmentId = "ea123";
    String assessmentMatrixId = "am123";
    String tenantId = "tenant123";

    employeeAssessment = createMockedEmployeeAssessment(employeeAssessmentId, EMPLOYEE_NAME_JOHN, assessmentMatrixId);
    employeeAssessment.setAssessmentStatus(AssessmentStatus.CONFIRMED);
    employeeAssessment.setAnsweredQuestionCount(0);
    employeeAssessment.setTenantId(tenantId);

    configuration = AssessmentConfiguration.builder()
        .navigationMode(QuestionNavigationType.RANDOM)
        .allowQuestionReview(true)
        .requireAllQuestions(true)
        .autoSave(true)
        .build();

    assessmentMatrix = AssessmentMatrix.builder()
        .id(assessmentMatrixId)
        .tenantId(tenantId)
        .name("Test Assessment Matrix")
        .description("Test Description")
        .performanceCycleId("pc123")
        .questionCount(3)
        .configuration(configuration)
        .build();

    question1 = createMockedQuestion("q1", QuestionType.YES_NO);
    question2 = createMockedQuestion("q2", QuestionType.ONE_TO_TEN);
    question3 = createMockedQuestion("q3", QuestionType.STAR_FIVE);
  }

  @Test
  void getNextUnansweredQuestion_firstTime_shouldUpdateStatusToInProgress() {
    // Prepare
    String employeeAssessmentId = "ea123";
    String tenantId = "tenant123";
    List<Question> allQuestions = Arrays.asList(question1, question2, question3);

    // Mock service calls
    doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(employeeAssessmentId);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(assessmentMatrix.getId());
    doReturn(configuration).when(assessmentMatrixService).getEffectiveConfiguration(assessmentMatrix);
    doReturn(allQuestions).when(questionService).findByAssessmentMatrixId(assessmentMatrix.getId(), tenantId);
    doReturn(Collections.emptySet()).when(answerService).findAnsweredQuestionIds(employeeAssessmentId, tenantId);

    // When
    AnswerWithProgressResponse result = assessmentNavigationService.getNextUnansweredQuestion(employeeAssessmentId, tenantId);

    // Then
    assertNotNull(result.getQuestion());
    assertEquals(AssessmentStatus.IN_PROGRESS, employeeAssessment.getAssessmentStatus());
    verify(employeeAssessmentService).save(employeeAssessment);
  }

  @Test
  void getNextUnansweredQuestion_allQuestionsAnswered_shouldUpdateStatusToCompleted() {
    // Prepare
    String employeeAssessmentId = "ea123";
    String tenantId = "tenant123";
    List<Question> allQuestions = Arrays.asList(question1, question2, question3);
    Set<String> answeredQuestionIds = Set.of("q1", "q2", "q3");

    employeeAssessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);

    // Mock service calls
    doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(employeeAssessmentId);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(assessmentMatrix.getId());
    doReturn(allQuestions).when(questionService).findByAssessmentMatrixId(assessmentMatrix.getId(), tenantId);
    doReturn(answeredQuestionIds).when(answerService).findAnsweredQuestionIds(employeeAssessmentId, tenantId);

    // When
    AnswerWithProgressResponse result = assessmentNavigationService.getNextUnansweredQuestion(employeeAssessmentId, tenantId);

    // Then
    assertNull(result.getQuestion());
    assertEquals(AssessmentStatus.COMPLETED, employeeAssessment.getAssessmentStatus());
    verify(employeeAssessmentService).save(employeeAssessment);
  }

  @Test
  void getNextUnansweredQuestion_randomMode_shouldReturnConsistentOrder() {
    // Prepare
    String employeeAssessmentId = "ea123";
    String tenantId = "tenant123";
    List<Question> allQuestions = Arrays.asList(question1, question2, question3);

    // Mock service calls
    doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(employeeAssessmentId);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(assessmentMatrix.getId());
    doReturn(configuration).when(assessmentMatrixService).getEffectiveConfiguration(assessmentMatrix);
    doReturn(allQuestions).when(questionService).findByAssessmentMatrixId(assessmentMatrix.getId(), tenantId);
    doReturn(Collections.emptySet()).when(answerService).findAnsweredQuestionIds(employeeAssessmentId, tenantId);

    // When - call multiple times
    AnswerWithProgressResponse result1 = assessmentNavigationService.getNextUnansweredQuestion(employeeAssessmentId, tenantId);
    AnswerWithProgressResponse result2 = assessmentNavigationService.getNextUnansweredQuestion(employeeAssessmentId, tenantId);

    // Then - should return the same question (consistent random seed)
    assertNotNull(result1.getQuestion());
    assertNotNull(result2.getQuestion());
    assertEquals(result1.getQuestion().getId(), result2.getQuestion().getId());
  }

  @Test
  void getNextUnansweredQuestion_shouldReturnCompleteResponse() {
    // Prepare
    String employeeAssessmentId = "ea123";
    String tenantId = "tenant123";
    List<Question> allQuestions = Arrays.asList(question1, question2, question3);
    Set<String> answeredQuestionIds = Set.of("q1");

    employeeAssessment.setAnsweredQuestionCount(1);

    // Mock service calls
    doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(employeeAssessmentId);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(assessmentMatrix.getId());
    doReturn(configuration).when(assessmentMatrixService).getEffectiveConfiguration(assessmentMatrix);
    doReturn(allQuestions).when(questionService).findByAssessmentMatrixId(assessmentMatrix.getId(), tenantId);
    doReturn(answeredQuestionIds).when(answerService).findAnsweredQuestionIds(employeeAssessmentId, tenantId);

    // When
    AnswerWithProgressResponse response = assessmentNavigationService.getNextUnansweredQuestion(employeeAssessmentId, tenantId);

    // Then
    assertNotNull(response);
    assertNotNull(response.getQuestion());
    assertNull(response.getExistingAnswer());
    assertEquals(Integer.valueOf(1), response.getCurrentProgress());
    assertEquals(Integer.valueOf(3), response.getTotalQuestions());
  }

  @Test
  void getNextUnansweredQuestion_existingAnswerAlwaysNull() {
    // Prepare
    String employeeAssessmentId = "ea123";
    String tenantId = "tenant123";
    List<Question> allQuestions = Arrays.asList(question1, question2, question3);
    Set<String> answeredQuestionIds = Set.of("q1");

    employeeAssessment.setAnsweredQuestionCount(1);

    // Mock service calls
    doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(employeeAssessmentId);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(assessmentMatrix.getId());
    doReturn(configuration).when(assessmentMatrixService).getEffectiveConfiguration(assessmentMatrix);
    doReturn(allQuestions).when(questionService).findByAssessmentMatrixId(assessmentMatrix.getId(), tenantId);
    doReturn(answeredQuestionIds).when(answerService).findAnsweredQuestionIds(employeeAssessmentId, tenantId);

    // When
    AnswerWithProgressResponse response = assessmentNavigationService.getNextUnansweredQuestion(employeeAssessmentId, tenantId);

    // Then
    assertNotNull(response);
    assertNotNull(response.getQuestion());
    // existingAnswer should always be null as documented in AnswerWithProgressResponse
    assertNull(response.getExistingAnswer());
  }

  private Answer createAnswerForQuestion(String questionId, String employeeAssessmentId) {
    Question question = createMockedQuestion(questionId, QuestionType.YES_NO);
    Answer answer = createMockedAnswer(GENERIC_ID_1234, employeeAssessmentId, question, QuestionType.YES_NO, LocalDateTime.now(), "Yes", 5.0);
    answer.setQuestionId(questionId);
    return answer;
  }
}
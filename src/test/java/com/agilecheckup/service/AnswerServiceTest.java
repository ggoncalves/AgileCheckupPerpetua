package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.score.AbstractScoreCalculator;
import com.agilecheckup.persistency.entity.score.ScoreCalculationStrategyFactory;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.agilecheckup.service.exception.InvalidLocalDateTimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest extends AbstractCrudServiceTest<Answer, AbstractCrudRepository<Answer>> {

  @InjectMocks
  @Spy
  private AnswerService answerService;

  @Mock
  private EmployeeAssessmentService employeeAssessmentService;

  @Mock
  private QuestionService questionService;

  @Mock
  private AnswerRepository answerRepository;

  private static final LocalDateTime NOW_DATE_TIME = LocalDateTime.now();
  private static final LocalDateTime FUTURE_DATE_TIME_59_MINUTES = LocalDateTime.now().plusMinutes(59);
  private static final LocalDateTime FUTURE_DATE_TIME_61_MINUTES = LocalDateTime.now().plusMinutes(61);

  private final Question mockedStarFiveQuestion = createMockedQuestion(GENERIC_ID_1234, QuestionType.STAR_FIVE);

  private final EmployeeAssessment employeeAssessment = createMockedEmployeeAssessment(GENERIC_ID_1234, EMPLOYEE_NAME_JOHN, GENERIC_ID_1234);

  @BeforeEach
  void setUpBefore() {
    // originalAnswer = createMockedAnswer(DEFAULT_ID, GENERIC_ID_1234, QuestionType.STAR_FIVE, NOW_DATE_TIME, "3");
  }

  @AfterEach
  void afterEach() {
    Mockito.reset(questionService, answerService, questionService, employeeAssessmentService);
  }

  @Test
  void createWithEmployeeAssessmentId_Invalid() {
    assertThrows(InvalidIdReferenceException.class, () -> assertCreateAnswerWithEmployeeAssessmentId("Invalid"));
  }

  @Test
  void createWithEmployeeAssessmentId_Null() {
    assertThrows(NullPointerException.class, () -> assertCreateAnswerWithEmployeeAssessmentId(null));
  }

  @Test
  void createWithQuestionId_Invalid() {
    assertThrows(InvalidIdReferenceException.class, () -> assertCreateAnswerWithQuestionId("Invalid"));
  }

  @Test
  void createWithQuestionId_Null() {
    assertThrows(NullPointerException.class, () -> assertCreateAnswerWithQuestionId(null));
  }

  @Test
  void createWithAnsweredAt_Null() {
    assertThrows(NullPointerException.class, () -> assertCreateAnswerWithAnsweredAt(null));
  }

  @Test
  void createWithAnsweredAt_Future59minutes() {
    assertCreate(FUTURE_DATE_TIME_59_MINUTES);
  }

  @Test
  void createWithAnsweredAt_Future61minutes() {
    InvalidLocalDateTimeException exception = assertThrows(InvalidLocalDateTimeException.class, () -> assertCreateAnswerWithAnsweredAt(FUTURE_DATE_TIME_61_MINUTES));
    assertEquals("Can't create the answer in the future", exception.getMessage());
  }

  void assertCreateAnswerWithEmployeeAssessmentId(String employeeAssessmentId) {
    String value = "3";
    AbstractScoreCalculator scoreCalculator = ScoreCalculationStrategyFactory.createStrategy(mockedStarFiveQuestion,
        value);
    Answer answer = createMockedAnswer(DEFAULT_ID, GENERIC_ID_1234, mockedStarFiveQuestion, QuestionType.STAR_FIVE,
        NOW_DATE_TIME, value, scoreCalculator.getCalculatedScore());
    if (employeeAssessmentId != null) {
      doReturn(Optional.empty()).when(employeeAssessmentService).findById(employeeAssessmentId);
    }

    // When
    answerService.create(
        employeeAssessmentId,
        answer.getQuestionId(),
        answer.getAnsweredAt(),
        answer.getValue(),
        answer.getTenantId(),
        answer.getNotes())
      ;
  }

  void assertCreateAnswerWithQuestionId(String questionId) {
    String value = "3";
    AbstractScoreCalculator scoreCalculator = ScoreCalculationStrategyFactory.createStrategy(mockedStarFiveQuestion,
        value);
    Answer answer = createMockedAnswer(DEFAULT_ID, GENERIC_ID_1234, mockedStarFiveQuestion, QuestionType.STAR_FIVE,
        NOW_DATE_TIME, value, scoreCalculator.getCalculatedScore());
    if (questionId != null) {
      doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(answer.getEmployeeAssessmentId());
      doReturn(Optional.empty()).when(employeeAssessmentService).findById(questionId);
    }
    // When
    answerService.create(
        answer.getEmployeeAssessmentId(),
        questionId,
        answer.getAnsweredAt(),
        answer.getValue(),
        answer.getTenantId(),
        answer.getNotes());
  }

  void assertCreateAnswerWithAnsweredAt(LocalDateTime answeredAt) {
    String value = "3";
    AbstractScoreCalculator scoreCalculator = ScoreCalculationStrategyFactory.createStrategy(mockedStarFiveQuestion,
        value);
    Answer answer = createMockedAnswer(DEFAULT_ID, GENERIC_ID_1234, mockedStarFiveQuestion, QuestionType.STAR_FIVE,
        NOW_DATE_TIME, value, scoreCalculator.getCalculatedScore());
    // When
    answerService.create(
        answer.getEmployeeAssessmentId(),
        answer.getQuestionId(),
        answeredAt,
        answer.getValue(),
        answer.getTenantId(),
        answer.getNotes());
  }

  @Test
  void create() {
    assertCreate(NOW_DATE_TIME);
  }

  void assertCreate(LocalDateTime answeredAt) {
    assertCreateWithValidValue(answeredAt, mockedStarFiveQuestion, "3");
  }

  @Test
  void createStarThree() {
    Question question = createMockedQuestion(GENERIC_ID_1234, QuestionType.STAR_THREE);
    assertCreateWithValidValue(NOW_DATE_TIME, question, "3");
  }

  @Test
  void createStarThree_Invalid() {
    Question question = createMockedQuestion(GENERIC_ID_1234, QuestionType.STAR_THREE);
    assertThrows(IllegalArgumentException.class, () -> assertCreateWithInvalidValue(question, "4"));
  }

  @Test
  void createStarFive() {
    Question question = createMockedQuestion(GENERIC_ID_1234, QuestionType.STAR_FIVE);
    assertCreateWithValidValue(NOW_DATE_TIME, question, "5");
  }

  @Test
  void createStarFive_Invalid() {
    Question question = createMockedQuestion(GENERIC_ID_1234, QuestionType.STAR_FIVE);
    assertThrows(IllegalArgumentException.class, () -> assertCreateWithInvalidValue(question, "6"));
  }

  @Test
  void createOneToTen() {
    Question question = createMockedQuestion(GENERIC_ID_1234, QuestionType.ONE_TO_TEN);
    assertCreateWithValidValue(NOW_DATE_TIME, question, "5");
  }

  @Test
  void createOneToTen_Invalid() {
    Question question = createMockedQuestion(GENERIC_ID_1234, QuestionType.ONE_TO_TEN);
    assertThrows(IllegalArgumentException.class, () -> assertCreateWithInvalidValue(question, "11"));
  }

  @Test
  void createYesNo_True() {
    Question question = createMockedQuestion(GENERIC_ID_1234, QuestionType.YES_NO);
    assertCreateWithValidValue(NOW_DATE_TIME, question, "true");
  }

  @Test
  void createYesNo_False() {
    Question question = createMockedQuestion(GENERIC_ID_1234, QuestionType.YES_NO);
    assertCreateWithValidValue(NOW_DATE_TIME, question, "false");
  }

  @Test
  void createGoodBad_True() {
    Question question = createMockedQuestion(GENERIC_ID_1234, QuestionType.GOOD_BAD);
    assertCreateWithValidValue(NOW_DATE_TIME, question, "true");
  }

  @Test
  void createGoodBad_False() {
    Question question = createMockedQuestion(GENERIC_ID_1234, QuestionType.GOOD_BAD);
    assertCreateWithValidValue(NOW_DATE_TIME, question, "false");
  }

  @Test
  void createOpenAnswer() {
    Question question = createMockedQuestion(GENERIC_ID_1234, QuestionType.OPEN_ANSWER);
    assertCreateWithValidValue(NOW_DATE_TIME, question, "This is an open Answer", true);
  }

  @Test
  void createCustomizedAnswer() {
    Question question = createMockedCustomQuestion(GENERIC_ID_1234);
    assertCreateWithValidValue(NOW_DATE_TIME, question, "4");
  }

  @Test
  void createCustomizedAnswer_Invalid() {
    Question question = createMockedCustomQuestion(GENERIC_ID_1234);
    assertThrows(IllegalArgumentException.class, () -> assertCreateWithInvalidValue(question, "6"));
  }

  void assertCreateWithValidValue(LocalDateTime answeredAt, Question question, String valueString) {
    assertCreateWithValidValue(answeredAt, question, valueString, false);
  }

  void assertCreateWithValidValue(LocalDateTime answeredAt, Question question, String valueString,
                                  boolean pendingReview) {
    AbstractScoreCalculator scoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question,
        valueString);
    Answer answer = createMockedAnswer(DEFAULT_ID, GENERIC_ID_1234, question, question.getQuestionType(), answeredAt,
        valueString, scoreCalculator.getCalculatedScore());
    answer.setPendingReview(pendingReview);
    Answer savedAnswer = cloneWithId(answer, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedAnswer).when(answerRepository).save(any());
    doReturn(Optional.of(question)).when(questionService).findById(answer.getQuestionId());
    doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(answer.getEmployeeAssessmentId());

    // When
    Optional<Answer> optionalAnswer = answerService.create(
        answer.getEmployeeAssessmentId(),
        answer.getQuestionId(),
        answer.getAnsweredAt(),
        answer.getValue(),
        answer.getTenantId(),
        answer.getNotes());

    // Then
    assertTrue(optionalAnswer.isPresent());
    assertEquals(savedAnswer, optionalAnswer.get());
    verify(answerRepository).save(savedAnswer);
    verify(answerService).create(
        answer.getEmployeeAssessmentId(),
        answer.getQuestionId(),
        answer.getAnsweredAt(),
        answer.getValue(),
        answer.getTenantId(),
        answer.getNotes());

    verify(employeeAssessmentService).findById(answer.getEmployeeAssessmentId());
    verify(employeeAssessmentService).incrementAnsweredQuestionCount(answer.getEmployeeAssessmentId());
    verify(questionService).findById(answer.getQuestionId());
  }

  void assertCreateWithInvalidValue(Question question, String valueString) {
    Answer answer = createMockedAnswer(DEFAULT_ID, GENERIC_ID_1234, question, question.getQuestionType(), AnswerServiceTest.NOW_DATE_TIME,
        valueString, 0d);
    Answer savedAnswer = cloneWithId(answer, DEFAULT_ID);

    // Prevent/Stub
    doReturn(Optional.of(question)).when(questionService).findById(answer.getQuestionId());

    // When
    Optional<Answer> optionalAnswer = answerService.create(
        answer.getEmployeeAssessmentId(),
        answer.getQuestionId(),
        answer.getAnsweredAt(),
        answer.getValue(),
        answer.getTenantId(),
        answer.getNotes());

    // Then
    assertTrue(optionalAnswer.isPresent());
    assertEquals(savedAnswer, optionalAnswer.get());
    verify(answerService).create(
        answer.getEmployeeAssessmentId(),
        answer.getQuestionId(),
        answer.getAnsweredAt(),
        answer.getValue(),
        answer.getTenantId(),
        answer.getNotes());
    verify(questionService).findById(answer.getQuestionId());
  }
  @Override
  AbstractCrudService<Answer, AbstractCrudRepository<Answer>> getCrudServiceSpy() {
    return answerService;
  }

}
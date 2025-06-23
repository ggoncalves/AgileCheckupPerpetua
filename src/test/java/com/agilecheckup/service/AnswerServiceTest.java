package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.agilecheckup.persistency.entity.AssessmentStatus;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.EMPLOYEE_NAME_JOHN;
import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.cloneWithId;
import static com.agilecheckup.util.TestObjectFactory.createMockedAnswer;
import static com.agilecheckup.util.TestObjectFactory.createMockedCustomQuestion;
import static com.agilecheckup.util.TestObjectFactory.createMockedEmployeeAssessment;
import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
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
  
  @Mock
  private AssessmentMatrixService assessmentMatrixService;

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
    Mockito.reset(questionService, answerService, questionService, employeeAssessmentService, assessmentMatrixService);
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

    // Mock AssessmentMatrix for status completion check
    AssessmentMatrix assessmentMatrix = AssessmentMatrix.builder()
        .id(employeeAssessment.getAssessmentMatrixId())
        .tenantId("tenant123")
        .name("Test Matrix")
        .description("Test Description")
        .performanceCycleId("pc123")
        .questionCount(20)
        .build();

    // Prevent/Stub
//    doReturn(savedAnswer).when(answerRepository).save(any());
    lenient().doReturn(Optional.empty()).when(answerRepository).findByEmployeeAssessmentIdAndQuestionId(answer.getEmployeeAssessmentId(), answer.getQuestionId(), answer.getTenantId());
    doAnswerForSaveWithRandomEntityId(savedAnswer, answerRepository);
    doReturn(Optional.of(question)).when(questionService).findById(answer.getQuestionId());
    lenient().doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(answer.getEmployeeAssessmentId());
    lenient().doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(employeeAssessment.getAssessmentMatrixId());

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

    verify(employeeAssessmentService, atLeastOnce()).findById(answer.getEmployeeAssessmentId());
    verify(employeeAssessmentService).incrementAnsweredQuestionCount(answer.getEmployeeAssessmentId());
    verify(questionService).findById(answer.getQuestionId());
  }

  void assertCreateWithInvalidValue(Question question, String valueString) {
    Answer answer = createMockedAnswer(DEFAULT_ID, GENERIC_ID_1234, question, question.getQuestionType(), AnswerServiceTest.NOW_DATE_TIME,
        valueString, 0d);
    Answer savedAnswer = cloneWithId(answer, DEFAULT_ID);

    // Prevent/Stub
    lenient().doReturn(Optional.empty()).when(answerRepository).findByEmployeeAssessmentIdAndQuestionId(answer.getEmployeeAssessmentId(), answer.getQuestionId(), answer.getTenantId());
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

  @Test
  void update_existingAnswer_shouldSucceed() {
    // Prepare
    Question question = createMockedQuestion(GENERIC_ID_1234, QuestionType.STAR_FIVE);
    Answer existingAnswer = createMockedAnswer(DEFAULT_ID, GENERIC_ID_1234, question, QuestionType.STAR_FIVE,
        NOW_DATE_TIME, "3", 3.0);
    Answer updatedAnswer = cloneWithId(existingAnswer, DEFAULT_ID);
    updatedAnswer.setAnsweredAt(FUTURE_DATE_TIME_59_MINUTES);
    updatedAnswer.setValue("5");
    updatedAnswer.setScore(5.0);
    updatedAnswer.setNotes("Updated notes");

    // Mock AssessmentMatrix for status completion check
    AssessmentMatrix assessmentMatrix = AssessmentMatrix.builder()
        .id(employeeAssessment.getAssessmentMatrixId())
        .tenantId("tenant123")
        .name("Test Matrix")
        .description("Test Description")
        .performanceCycleId("pc123")
        .questionCount(20)
        .build();

    // Mock repository calls
    doReturn(existingAnswer).when(answerRepository).findById(DEFAULT_ID);
    doAnswerForUpdate(updatedAnswer, answerRepository);
    doReturn(Optional.of(question)).when(questionService).findById(question.getId());
    lenient().doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(existingAnswer.getEmployeeAssessmentId());
    lenient().doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(employeeAssessment.getAssessmentMatrixId());

    // When
    Optional<Answer> resultOptional = answerService.update(DEFAULT_ID,
        FUTURE_DATE_TIME_59_MINUTES,
        "5",
        "Updated notes"
    );

    // Then
    assertTrue(resultOptional.isPresent());
    assertEquals(updatedAnswer, resultOptional.get());
    verify(answerRepository).findById(DEFAULT_ID);
    verify(answerRepository).save(updatedAnswer);
    verify(questionService).findById(question.getId());
    verify(answerService).update(DEFAULT_ID,
        FUTURE_DATE_TIME_59_MINUTES,
        "5",
        "Updated notes"
    );
    verify(employeeAssessmentService).incrementAnsweredQuestionCount(updatedAnswer.getEmployeeAssessmentId());
    verify(employeeAssessmentService, atLeastOnce()).findById(updatedAnswer.getEmployeeAssessmentId());
  }

  @Test
  void update_nonExistingAnswer_shouldReturnEmpty() {
    // Prepare
    String nonExistingId = "nonExistingId";

    // Mock repository calls
    doReturn(null).when(answerRepository).findById(nonExistingId);

    // When
    Optional<Answer> resultOptional = answerService.update(
        nonExistingId,
        FUTURE_DATE_TIME_59_MINUTES,
        "5",
        "Updated notes"
                                                          );

    // Then
    assertTrue(resultOptional.isEmpty());
    verify(answerRepository).findById(nonExistingId);
    verify(answerService).update(nonExistingId,
        FUTURE_DATE_TIME_59_MINUTES,
        "5",
        "Updated notes"
    );
  }

  @Test
  void findByEmployeeAssessmentId_shouldReturnAnswers() {
    // Prepare
    String employeeAssessmentId = "ea123";
    String tenantId = "tenant123";
    Question question1 = createMockedQuestion("q1", QuestionType.YES_NO);
    Question question2 = createMockedQuestion("q2", QuestionType.ONE_TO_TEN);
    List<Answer> expectedAnswers = Arrays.asList(
        createMockedAnswer("answer1", employeeAssessmentId, question1, QuestionType.YES_NO, NOW_DATE_TIME, "Yes", 5.0),
        createMockedAnswer("answer2", employeeAssessmentId, question2, QuestionType.ONE_TO_TEN, NOW_DATE_TIME, "8", 8.0)
    );
    
    // Mock repository call
    doReturn(expectedAnswers).when(answerRepository).findByEmployeeAssessmentId(employeeAssessmentId, tenantId);
    
    // When
    List<Answer> actualAnswers = answerService.findByEmployeeAssessmentId(employeeAssessmentId, tenantId);
    
    // Then
    assertEquals(expectedAnswers, actualAnswers);
    verify(answerRepository).findByEmployeeAssessmentId(employeeAssessmentId, tenantId);
  }

  @Test
  void postCreate_shouldUpdateAssessmentStatusToCompletedAndCalculateScore() {
    // Prepare
    String employeeAssessmentId = "ea123";
    String assessmentMatrixId = "am123";
    String tenantId = "tenant123";
    Question question = createMockedQuestion("q123", QuestionType.YES_NO);
    Answer savedAnswer = createMockedAnswer("answer123", employeeAssessmentId, question, QuestionType.YES_NO, NOW_DATE_TIME, "Yes", 5.0);
    
    EmployeeAssessment employeeAssessment = createMockedEmployeeAssessment(employeeAssessmentId, EMPLOYEE_NAME_JOHN, assessmentMatrixId);
    employeeAssessment.setAnsweredQuestionCount(20);
    employeeAssessment.setAssessmentStatus(AssessmentStatus.IN_PROGRESS);
    employeeAssessment.setTenantId(tenantId);
    
    AssessmentMatrix assessmentMatrix = AssessmentMatrix.builder()
        .id(assessmentMatrixId)
        .tenantId(tenantId)
        .name("Test Matrix")
        .description("Test Description")
        .performanceCycleId("pc123")
        .questionCount(20)
        .build();
    
    EmployeeAssessment updatedAssessment = createMockedEmployeeAssessment(employeeAssessmentId, EMPLOYEE_NAME_JOHN, assessmentMatrixId);
    updatedAssessment.setAssessmentStatus(AssessmentStatus.COMPLETED);
    updatedAssessment.setTenantId(tenantId);
    
    // Mock service calls
    doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(employeeAssessmentId);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(assessmentMatrixId);
    doReturn(updatedAssessment).when(employeeAssessmentService).updateEmployeeAssessmentScore(employeeAssessmentId, tenantId);
    
    // When
    answerService.postCreate(savedAnswer);
    
    // Then
    verify(employeeAssessmentService).incrementAnsweredQuestionCount(employeeAssessmentId);
    verify(employeeAssessmentService).findById(employeeAssessmentId);
    verify(assessmentMatrixService).findById(assessmentMatrixId);
    verify(employeeAssessmentService).save(employeeAssessment);
    verify(employeeAssessmentService).updateEmployeeAssessmentScore(employeeAssessmentId, tenantId);
    assertEquals(AssessmentStatus.COMPLETED, employeeAssessment.getAssessmentStatus());
  }

  @Test
  void postCreate_shouldNotCalculateScoreWhenAssessmentAlreadyCompleted() {
    // Prepare
    String employeeAssessmentId = "ea123";
    String assessmentMatrixId = "am123";
    String tenantId = "tenant123";
    Question question = createMockedQuestion("q123", QuestionType.YES_NO);
    Answer savedAnswer = createMockedAnswer("answer123", employeeAssessmentId, question, QuestionType.YES_NO, NOW_DATE_TIME, "Yes", 5.0);
    
    EmployeeAssessment employeeAssessment = createMockedEmployeeAssessment(employeeAssessmentId, EMPLOYEE_NAME_JOHN, assessmentMatrixId);
    employeeAssessment.setAnsweredQuestionCount(20);
    employeeAssessment.setAssessmentStatus(AssessmentStatus.COMPLETED); // Already completed
    employeeAssessment.setTenantId(tenantId);
    
    AssessmentMatrix assessmentMatrix = AssessmentMatrix.builder()
        .id(assessmentMatrixId)
        .tenantId(tenantId)
        .name("Test Matrix")
        .description("Test Description")
        .performanceCycleId("pc123")
        .questionCount(20)
        .build();
    
    // Mock service calls
    doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(employeeAssessmentId);
    doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(assessmentMatrixId);
    
    // When
    answerService.postCreate(savedAnswer);
    
    // Then
    verify(employeeAssessmentService).incrementAnsweredQuestionCount(employeeAssessmentId);
    verify(employeeAssessmentService).findById(employeeAssessmentId);
    verify(assessmentMatrixService).findById(assessmentMatrixId);
    verify(employeeAssessmentService).save(employeeAssessment);
    // Should NOT call updateEmployeeAssessmentScore since status was already COMPLETED
    verify(employeeAssessmentService, never()).updateEmployeeAssessmentScore(employeeAssessmentId, tenantId);
    assertEquals(AssessmentStatus.COMPLETED, employeeAssessment.getAssessmentStatus());
  }

  @Test
  void create_shouldCreateNewAnswerWhenNoDuplicateExists() {
    // Given
    String employeeAssessmentId = "ea123";
    String questionId = "q123";
    String tenantId = "tenant123";
    String value = "5";
    String notes = "Test notes";
    LocalDateTime answeredAt = NOW_DATE_TIME;
    
    Question question = createMockedQuestion(questionId, QuestionType.STAR_FIVE);
    AbstractScoreCalculator scoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question, value);
    Answer expectedAnswer = createMockedAnswer(DEFAULT_ID, employeeAssessmentId, question, 
        QuestionType.STAR_FIVE, answeredAt, value, scoreCalculator.getCalculatedScore());
    expectedAnswer.setNotes(notes);
    expectedAnswer.setTenantId(tenantId); // Ensure correct tenant ID
    Answer savedAnswer = cloneWithId(expectedAnswer, DEFAULT_ID);

    // Mock AssessmentMatrix for status completion check
    AssessmentMatrix assessmentMatrix = AssessmentMatrix.builder()
        .id(employeeAssessment.getAssessmentMatrixId())
        .tenantId("tenant123")
        .name("Test Matrix")
        .description("Test Description")
        .performanceCycleId("pc123")
        .questionCount(20)
        .build();

    // Mock repository and service calls
    lenient().doReturn(Optional.empty()).when(answerRepository).findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);
    doAnswerForSaveWithRandomEntityId(savedAnswer, answerRepository);
    lenient().doReturn(Optional.of(question)).when(questionService).findById(questionId);
    lenient().doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(employeeAssessmentId);
    lenient().doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(employeeAssessment.getAssessmentMatrixId());

    // When
    Optional<Answer> result = answerService.create(employeeAssessmentId, questionId, answeredAt, value, tenantId, notes);

    // Then
    assertTrue(result.isPresent());
    verify(answerRepository).findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);
    verify(questionService).findById(questionId);
    verify(employeeAssessmentService).incrementAnsweredQuestionCount(employeeAssessmentId);
  }

  @Test
  void create_shouldUpdateExistingAnswerWhenDuplicateExists() {
    // Given
    String employeeAssessmentId = "ea123";
    String questionId = "q123";
    String tenantId = "tenant123";
    String newValue = "4";
    String newNotes = "Updated notes";
    LocalDateTime newAnsweredAt = NOW_DATE_TIME.plusMinutes(30);
    
    Question question = createMockedQuestion(questionId, QuestionType.STAR_FIVE);
    AbstractScoreCalculator oldScoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question, "3");
    AbstractScoreCalculator newScoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question, newValue);
    
    // Existing answer in database with proper questionId
    Answer existingAnswer = createMockedAnswer("existing-id", employeeAssessmentId, question, 
        QuestionType.STAR_FIVE, NOW_DATE_TIME, "3", oldScoreCalculator.getCalculatedScore());
    existingAnswer.setQuestionId(questionId); // Ensure question ID matches
    existingAnswer.setNotes("Original notes");
    
    // Expected updated answer
    Answer updatedAnswer = cloneWithId(existingAnswer, "existing-id");
    updatedAnswer.setAnsweredAt(newAnsweredAt);
    updatedAnswer.setValue(newValue);
    updatedAnswer.setScore(newScoreCalculator.getCalculatedScore());
    updatedAnswer.setNotes(newNotes);

    // Mock AssessmentMatrix for status completion check
    AssessmentMatrix assessmentMatrix = AssessmentMatrix.builder()
        .id(employeeAssessment.getAssessmentMatrixId())
        .tenantId("tenant123")
        .name("Test Matrix")
        .description("Test Description")
        .performanceCycleId("pc123")
        .questionCount(20)
        .build();

    // Mock repository and service calls
    lenient().doReturn(Optional.of(existingAnswer)).when(answerRepository).findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);
    doAnswerForUpdate(updatedAnswer, answerRepository);
    lenient().doReturn(Optional.of(question)).when(questionService).findById(questionId);
    lenient().doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(employeeAssessmentId);
    lenient().doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(employeeAssessment.getAssessmentMatrixId());

    // When
    Optional<Answer> result = answerService.create(employeeAssessmentId, questionId, newAnsweredAt, newValue, tenantId, newNotes);

    // Then
    assertTrue(result.isPresent());
    assertEquals(updatedAnswer, result.get());
    verify(answerRepository).findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);
    verify(answerRepository).save(updatedAnswer);
    verify(questionService).findById(questionId);
    verify(employeeAssessmentService).incrementAnsweredQuestionCount(employeeAssessmentId);
    
    // Verify the existing answer was updated with new values
    assertEquals(newAnsweredAt, updatedAnswer.getAnsweredAt());
    assertEquals(newValue, updatedAnswer.getValue());
    assertEquals(newScoreCalculator.getCalculatedScore(), updatedAnswer.getScore());
    assertEquals(newNotes, updatedAnswer.getNotes());
  }

  @Test
  void create_shouldUpdateExistingAnswerWithDifferentQuestionTypes() {
    // Given
    String employeeAssessmentId = "ea123";
    String questionId = "q123";
    String tenantId = "tenant123";
    String newValue = "false";
    LocalDateTime newAnsweredAt = NOW_DATE_TIME.plusMinutes(15);
    
    Question question = createMockedQuestion(questionId, QuestionType.YES_NO);
    AbstractScoreCalculator oldScoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question, "true");
    AbstractScoreCalculator newScoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question, newValue);
    
    // Existing answer with YES_NO type and correct questionId
    Answer existingAnswer = createMockedAnswer("existing-id", employeeAssessmentId, question, 
        QuestionType.YES_NO, NOW_DATE_TIME, "true", oldScoreCalculator.getCalculatedScore());
    existingAnswer.setQuestionId(questionId); // Ensure correct question ID
    
    // Expected updated answer
    Answer updatedAnswer = cloneWithId(existingAnswer, "existing-id");
    updatedAnswer.setAnsweredAt(newAnsweredAt);
    updatedAnswer.setValue(newValue);
    updatedAnswer.setScore(newScoreCalculator.getCalculatedScore());

    // Mock AssessmentMatrix for status completion check
    AssessmentMatrix assessmentMatrix = AssessmentMatrix.builder()
        .id(employeeAssessment.getAssessmentMatrixId())
        .tenantId("tenant123")
        .name("Test Matrix")
        .description("Test Description")
        .performanceCycleId("pc123")
        .questionCount(20)
        .build();

    // Mock repository and service calls
    lenient().doReturn(Optional.of(existingAnswer)).when(answerRepository).findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);
    doAnswerForUpdate(updatedAnswer, answerRepository);
    lenient().doReturn(Optional.of(question)).when(questionService).findById(questionId);
    lenient().doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(employeeAssessmentId);
    lenient().doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(employeeAssessment.getAssessmentMatrixId());

    // When
    Optional<Answer> result = answerService.create(employeeAssessmentId, questionId, newAnsweredAt, newValue, tenantId, null);

    // Then
    assertTrue(result.isPresent());
    assertEquals(updatedAnswer, result.get());
    verify(answerRepository).findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);
    verify(answerRepository).save(updatedAnswer);
    verify(questionService).findById(questionId);
    verify(employeeAssessmentService).incrementAnsweredQuestionCount(employeeAssessmentId);
  }

  @Test
  void create_shouldThrowExceptionWhenDuplicateExistsButFutureTimestamp() {
    // Given
    String employeeAssessmentId = "ea123";
    String questionId = "q123";
    String tenantId = "tenant123";
    String newValue = "4";
    LocalDateTime futureTimestamp = FUTURE_DATE_TIME_61_MINUTES; // Invalid future time
    
    Question question = createMockedQuestion(questionId, QuestionType.STAR_FIVE);
    Answer existingAnswer = createMockedAnswer("existing-id", employeeAssessmentId, question, 
        QuestionType.STAR_FIVE, NOW_DATE_TIME, "3", 3.0);
    existingAnswer.setQuestionId(questionId); // Ensure correct question ID

    // Mock repository call to return existing answer
    doReturn(Optional.of(existingAnswer)).when(answerRepository).findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);
    doReturn(Optional.of(question)).when(questionService).findById(questionId);

    // When & Then
    InvalidLocalDateTimeException exception = assertThrows(InvalidLocalDateTimeException.class, 
        () -> answerService.create(employeeAssessmentId, questionId, futureTimestamp, newValue, tenantId, null));
    
    assertEquals("Can't create the answer in the future", exception.getMessage());
    verify(answerRepository).findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);
    // Note: questionService.findById() is NOT called because validateAnsweredAt() throws exception before it's reached
  }

  @Test
  void create_shouldHandleNullNotesWhenUpdatingDuplicate() {
    // Given
    String employeeAssessmentId = "ea123";
    String questionId = "q123";
    String tenantId = "tenant123";
    String newValue = "8";
    LocalDateTime newAnsweredAt = NOW_DATE_TIME.plusMinutes(20);
    
    Question question = createMockedQuestion(questionId, QuestionType.ONE_TO_TEN);
    AbstractScoreCalculator oldScoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question, "5");
    AbstractScoreCalculator newScoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question, newValue);
    
    // Existing answer with notes and correct questionId
    Answer existingAnswer = createMockedAnswer("existing-id", employeeAssessmentId, question, 
        QuestionType.ONE_TO_TEN, NOW_DATE_TIME, "5", oldScoreCalculator.getCalculatedScore());
    existingAnswer.setQuestionId(questionId); // Ensure correct question ID
    existingAnswer.setNotes("Original notes");
    
    // Expected updated answer with null notes
    Answer updatedAnswer = cloneWithId(existingAnswer, "existing-id");
    updatedAnswer.setAnsweredAt(newAnsweredAt);
    updatedAnswer.setValue(newValue);
    updatedAnswer.setScore(newScoreCalculator.getCalculatedScore());
    updatedAnswer.setNotes(null);

    // Mock AssessmentMatrix for status completion check
    AssessmentMatrix assessmentMatrix = AssessmentMatrix.builder()
        .id(employeeAssessment.getAssessmentMatrixId())
        .tenantId("tenant123")
        .name("Test Matrix")
        .description("Test Description")
        .performanceCycleId("pc123")
        .questionCount(20)
        .build();

    // Mock repository and service calls
    lenient().doReturn(Optional.of(existingAnswer)).when(answerRepository).findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);
    doAnswerForUpdate(updatedAnswer, answerRepository);
    lenient().doReturn(Optional.of(question)).when(questionService).findById(questionId);
    lenient().doReturn(Optional.of(employeeAssessment)).when(employeeAssessmentService).findById(employeeAssessmentId);
    lenient().doReturn(Optional.of(assessmentMatrix)).when(assessmentMatrixService).findById(employeeAssessment.getAssessmentMatrixId());

    // When
    Optional<Answer> result = answerService.create(employeeAssessmentId, questionId, newAnsweredAt, newValue, tenantId, null);

    // Then
    assertTrue(result.isPresent());
    assertEquals(updatedAnswer, result.get());
    assertEquals(null, result.get().getNotes());
    verify(answerRepository).findByEmployeeAssessmentIdAndQuestionId(employeeAssessmentId, questionId, tenantId);
    verify(answerRepository).save(updatedAnswer);
    verify(questionService).findById(questionId);
  }
}
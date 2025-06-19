package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.agilecheckup.util.TestObjectFactory.createMockedAnswer;
import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerRepositoryTest extends AbstractRepositoryTest<Answer> {

  @InjectMocks
  @Spy
  private AnswerRepository answerRepository;

  @Override
  AbstractCrudRepository getRepository() {
    return answerRepository;
  }

  @Override
  Answer createMockedT() {
    return createMockedAnswer(10d);
  }

  @Override
  Class getMockedClass() {
    return Answer.class;
  }

  @Test
  void findByEmployeeAssessmentIdAndQuestionId_shouldReturnAnswerWhenExists() {
    // Given
    String employeeAssessmentId = "ea123";
    String questionId = "q123";
    String tenantId = "tenant123";
    
    Question question = createMockedQuestion(questionId, QuestionType.STAR_FIVE);
    Answer targetAnswer = createMockedAnswer("answer1", employeeAssessmentId, question, 
        QuestionType.STAR_FIVE, LocalDateTime.now(), "5", 5.0);
    targetAnswer.setQuestionId(questionId); // Ensure correct question ID
    
    Answer otherAnswer = createMockedAnswer("answer2", employeeAssessmentId, 
        createMockedQuestion("q456", QuestionType.YES_NO), QuestionType.YES_NO, 
        LocalDateTime.now(), "true", 5.0);
    otherAnswer.setQuestionId("q456"); // Ensure different question ID
    
    // Create a list of answers that will be returned by the query
    List<Answer> answersList = Arrays.asList(targetAnswer, otherAnswer);
    
    // Mock PaginatedQueryList and configure its behavior as a List
    PaginatedQueryList<Answer> mockResults = mock(PaginatedQueryList.class);
    when(mockResults.stream()).thenReturn(answersList.stream());
    
    // Mock DynamoDB query to return PaginatedQueryList
    doReturn(mockResults).when(dynamoDBMapperMock).query(eq(Answer.class), any(DynamoDBQueryExpression.class));
    doReturn(dynamoDBMapperMock).when(answerRepository).getDynamoDBMapper();
    
    // When
    Optional<Answer> result = answerRepository.findByEmployeeAssessmentIdAndQuestionId(
        employeeAssessmentId, questionId, tenantId);
    
    // Then
    assertTrue(result.isPresent());
    assertEquals(targetAnswer, result.get());
    assertEquals(questionId, result.get().getQuestionId());
    verify(dynamoDBMapperMock).query(eq(Answer.class), any(DynamoDBQueryExpression.class));
  }

  @Test
  void findByEmployeeAssessmentIdAndQuestionId_shouldReturnEmptyWhenNotExists() {
    // Given
    String employeeAssessmentId = "ea123";
    String questionId = "q999"; // Non-existent question
    String tenantId = "tenant123";
    
    Question otherQuestion = createMockedQuestion("q456", QuestionType.YES_NO);
    Answer otherAnswer = createMockedAnswer("answer1", employeeAssessmentId, otherQuestion, 
        QuestionType.YES_NO, LocalDateTime.now(), "true", 5.0);
    
    // Create a list of answers that don't match the target questionId
    List<Answer> answersListWithNoMatch = Arrays.asList(otherAnswer);
    
    // Mock PaginatedQueryList for non-matching answers
    PaginatedQueryList<Answer> mockResults = mock(PaginatedQueryList.class);
    when(mockResults.stream()).thenReturn(answersListWithNoMatch.stream());
    
    // Mock DynamoDB query to return answers that don't match the target questionId
    doReturn(mockResults).when(dynamoDBMapperMock).query(eq(Answer.class), any(DynamoDBQueryExpression.class));
    doReturn(dynamoDBMapperMock).when(answerRepository).getDynamoDBMapper();
    
    // When
    Optional<Answer> result = answerRepository.findByEmployeeAssessmentIdAndQuestionId(
        employeeAssessmentId, questionId, tenantId);
    
    // Then
    assertFalse(result.isPresent());
    verify(dynamoDBMapperMock).query(eq(Answer.class), any(DynamoDBQueryExpression.class));
  }

  @Test
  void findByEmployeeAssessmentIdAndQuestionId_shouldReturnEmptyWhenNoAnswers() {
    // Given
    String employeeAssessmentId = "ea123";
    String questionId = "q123";
    String tenantId = "tenant123";
    
    // Mock PaginatedQueryList for empty results
    PaginatedQueryList<Answer> mockResults = mock(PaginatedQueryList.class);
    when(mockResults.stream()).thenReturn(Stream.empty());
    
    // Mock DynamoDB query to return no answers
    doReturn(mockResults).when(dynamoDBMapperMock).query(eq(Answer.class), any(DynamoDBQueryExpression.class));
    doReturn(dynamoDBMapperMock).when(answerRepository).getDynamoDBMapper();
    
    // When
    Optional<Answer> result = answerRepository.findByEmployeeAssessmentIdAndQuestionId(
        employeeAssessmentId, questionId, tenantId);
    
    // Then
    assertFalse(result.isPresent());
    verify(dynamoDBMapperMock).query(eq(Answer.class), any(DynamoDBQueryExpression.class));
  }

  @Test
  void findByEmployeeAssessmentIdAndQuestionId_shouldReturnFirstMatchWhenMultipleExist() {
    // Given - This scenario shouldn't happen in production but tests robustness
    String employeeAssessmentId = "ea123";
    String questionId = "q123";
    String tenantId = "tenant123";
    
    Question question = createMockedQuestion(questionId, QuestionType.STAR_FIVE);
    Answer firstAnswer = createMockedAnswer("answer1", employeeAssessmentId, question, 
        QuestionType.STAR_FIVE, LocalDateTime.now().minusMinutes(10), "3", 3.0);
    Answer secondAnswer = createMockedAnswer("answer2", employeeAssessmentId, question, 
        QuestionType.STAR_FIVE, LocalDateTime.now(), "5", 5.0);
    
    // Create list with duplicate answers (both have same questionId)
    firstAnswer.setQuestionId(questionId);
    secondAnswer.setQuestionId(questionId);
    List<Answer> duplicateAnswersList = Arrays.asList(firstAnswer, secondAnswer);
    
    // Mock PaginatedQueryList for duplicate answers
    PaginatedQueryList<Answer> mockResults = mock(PaginatedQueryList.class);
    when(mockResults.stream()).thenReturn(duplicateAnswersList.stream());
    
    // Mock DynamoDB query to return multiple answers with same questionId
    doReturn(mockResults).when(dynamoDBMapperMock).query(eq(Answer.class), any(DynamoDBQueryExpression.class));
    doReturn(dynamoDBMapperMock).when(answerRepository).getDynamoDBMapper();
    
    // When
    Optional<Answer> result = answerRepository.findByEmployeeAssessmentIdAndQuestionId(
        employeeAssessmentId, questionId, tenantId);
    
    // Then
    assertTrue(result.isPresent());
    assertEquals(firstAnswer, result.get()); // Should return the first match
    assertEquals(questionId, result.get().getQuestionId());
    verify(dynamoDBMapperMock).query(eq(Answer.class), any(DynamoDBQueryExpression.class));
  }
}
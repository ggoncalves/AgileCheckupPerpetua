package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.question.Question;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
class QuestionRepositoryTest extends AbstractRepositoryTest<Question> {

  @InjectMocks
  @Spy
  private QuestionRepository questionRepository;

  @Override
  AbstractCrudRepository getRepository() {
    return questionRepository;
  }

  @Override
  Question createMockedT() {
    return createMockedQuestion();
  }

  @Override
  Class getMockedClass() {
    return Question.class;
  }

  @Test
  void existsByCategoryId_withExistingQuestions_shouldReturnTrue() {
    // Given
    String matrixId = "matrix-123";
    String categoryId = "category-456";
    String tenantId = "tenant-789";
    
    @SuppressWarnings("unchecked")
    PaginatedScanList<Question> mockPaginatedList = mock(PaginatedScanList.class);
    when(mockPaginatedList.isEmpty()).thenReturn(false);
    
    doReturn(mockPaginatedList).when(dynamoDBMapperMock).scan(eq(Question.class), any(DynamoDBScanExpression.class));

    // When
    boolean result = questionRepository.existsByCategoryId(matrixId, categoryId, tenantId);

    // Then
    assertTrue(result);
    verify(dynamoDBMapperMock).scan(eq(Question.class), any(DynamoDBScanExpression.class));
  }

  @Test
  void existsByCategoryId_withNoQuestions_shouldReturnFalse() {
    // Given
    String matrixId = "matrix-123";
    String categoryId = "category-456";
    String tenantId = "tenant-789";
    
    @SuppressWarnings("unchecked")
    PaginatedScanList<Question> mockPaginatedList = mock(PaginatedScanList.class);
    when(mockPaginatedList.isEmpty()).thenReturn(true);
    
    doReturn(mockPaginatedList).when(dynamoDBMapperMock).scan(eq(Question.class), any(DynamoDBScanExpression.class));

    // When
    boolean result = questionRepository.existsByCategoryId(matrixId, categoryId, tenantId);

    // Then
    assertFalse(result);
    verify(dynamoDBMapperMock).scan(eq(Question.class), any(DynamoDBScanExpression.class));
  }

  @Test
  void findByAssessmentMatrixId_shouldCallScanWithCorrectParameters() {
    // Given
    String matrixId = "matrix-123";
    String tenantId = "tenant-789";
    
    @SuppressWarnings("unchecked")
    PaginatedScanList<Question> mockPaginatedList = mock(PaginatedScanList.class);
    
    doReturn(mockPaginatedList).when(dynamoDBMapperMock).scan(eq(Question.class), any(DynamoDBScanExpression.class));

    // When
    List<Question> result = questionRepository.findByAssessmentMatrixId(matrixId, tenantId);

    // Then
    assertEquals(mockPaginatedList, result);
    verify(dynamoDBMapperMock).scan(eq(Question.class), any(DynamoDBScanExpression.class));
  }
}
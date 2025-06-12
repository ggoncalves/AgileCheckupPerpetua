package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.createMockedEmployeeAssessment;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeAssessmentRepositoryTest extends AbstractRepositoryTest<EmployeeAssessment> {

  @InjectMocks
  @Spy
  private EmployeeAssessmentRepository employeeAssessmentRepository;

  @Override
  AbstractCrudRepository getRepository() {
    return employeeAssessmentRepository;
  }

  @Override
  EmployeeAssessment createMockedT() {
    return createMockedEmployeeAssessment(GENERIC_ID_1234, "Josivaldo", GENERIC_ID_1234);
  }

  @Override
  Class getMockedClass() {
    return EmployeeAssessment.class;
  }

  @Test
  void existsByAssessmentMatrixAndEmployeeEmail_shouldReturnTrue_whenEmployeeAssessmentExists() {
    // Given
    String assessmentMatrixId = "assessment-matrix-123";
    String employeeEmail = "john.doe@company.com";
    PaginatedQueryList<EmployeeAssessment> mockResults = mock(PaginatedQueryList.class);
    when(mockResults.isEmpty()).thenReturn(false);

    // Mock the behavior of dynamoDBMapper.query
    when(dynamoDBMapperMock.query(eq(EmployeeAssessment.class), any(DynamoDBQueryExpression.class)))
        .thenReturn(mockResults);

    // When
    boolean result = employeeAssessmentRepository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrixId, employeeEmail);

    // Then
    assertThat(result).isTrue();

    // Verify the query was called with correct parameters
    ArgumentCaptor<DynamoDBQueryExpression<EmployeeAssessment>> queryCaptor = 
        ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
    verify(dynamoDBMapperMock).query(eq(EmployeeAssessment.class), queryCaptor.capture());

    DynamoDBQueryExpression<EmployeeAssessment> capturedQuery = queryCaptor.getValue();
    assertThat(capturedQuery.getIndexName()).isEqualTo("assessmentMatrixId-employeeEmail-index");
    assertThat(capturedQuery.getKeyConditionExpression()).isEqualTo("assessmentMatrixId = :assessmentMatrixId AND employeeEmailNormalized = :employeeEmail");
    assertThat(capturedQuery.isConsistentRead()).isFalse();
    assertThat(capturedQuery.getLimit()).isEqualTo(1);

    Map<String, AttributeValue> expressionAttributeValues = capturedQuery.getExpressionAttributeValues();
    assertThat(expressionAttributeValues).hasSize(2);
    assertThat(expressionAttributeValues.get(":assessmentMatrixId").getS()).isEqualTo(assessmentMatrixId);
    assertThat(expressionAttributeValues.get(":employeeEmail").getS()).isEqualTo(employeeEmail.toLowerCase());
  }

  @Test
  void existsByAssessmentMatrixAndEmployeeEmail_shouldReturnFalse_whenEmployeeAssessmentDoesNotExist() {
    // Given
    String assessmentMatrixId = "assessment-matrix-456";
    String employeeEmail = "jane.doe@company.com";
    PaginatedQueryList<EmployeeAssessment> mockResults = mock(PaginatedQueryList.class);
    when(mockResults.isEmpty()).thenReturn(true);

    // Mock the behavior of dynamoDBMapper.query
    when(dynamoDBMapperMock.query(eq(EmployeeAssessment.class), any(DynamoDBQueryExpression.class)))
        .thenReturn(mockResults);

    // When
    boolean result = employeeAssessmentRepository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrixId, employeeEmail);

    // Then
    assertThat(result).isFalse();

    // Verify the query was called
    verify(dynamoDBMapperMock).query(eq(EmployeeAssessment.class), any(DynamoDBQueryExpression.class));
  }

  @Test
  void existsByAssessmentMatrixAndEmployeeEmail_shouldNormalizeEmailToLowerCase_andTrimWhitespace() {
    // Given
    String assessmentMatrixId = "assessment-matrix-789";
    String employeeEmailWithMixedCase = "  John.DOE@Company.COM  ";
    String expectedNormalizedEmail = "john.doe@company.com";
    PaginatedQueryList<EmployeeAssessment> mockResults = mock(PaginatedQueryList.class);
    when(mockResults.isEmpty()).thenReturn(false);

    // Mock the behavior of dynamoDBMapper.query
    when(dynamoDBMapperMock.query(eq(EmployeeAssessment.class), any(DynamoDBQueryExpression.class)))
        .thenReturn(mockResults);

    // When
    boolean result = employeeAssessmentRepository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrixId, employeeEmailWithMixedCase);

    // Then
    assertThat(result).isTrue();

    // Verify the email was normalized correctly in the query
    ArgumentCaptor<DynamoDBQueryExpression<EmployeeAssessment>> queryCaptor = 
        ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
    verify(dynamoDBMapperMock).query(eq(EmployeeAssessment.class), queryCaptor.capture());

    DynamoDBQueryExpression<EmployeeAssessment> capturedQuery = queryCaptor.getValue();
    Map<String, AttributeValue> expressionAttributeValues = capturedQuery.getExpressionAttributeValues();
    assertThat(expressionAttributeValues.get(":employeeEmail").getS()).isEqualTo(expectedNormalizedEmail);
    assertThat(expressionAttributeValues.get(":assessmentMatrixId").getS()).isEqualTo(assessmentMatrixId);
  }

  @Test
  void existsByAssessmentMatrixAndEmployeeEmail_shouldHandleEmptyResultsList() {
    // Given
    String assessmentMatrixId = "assessment-matrix-empty";
    String employeeEmail = "empty@company.com";
    PaginatedQueryList<EmployeeAssessment> emptyResults = mock(PaginatedQueryList.class);
    when(emptyResults.isEmpty()).thenReturn(true);

    // Mock the behavior of dynamoDBMapper.query
    when(dynamoDBMapperMock.query(eq(EmployeeAssessment.class), any(DynamoDBQueryExpression.class)))
        .thenReturn(emptyResults);

    // When
    boolean result = employeeAssessmentRepository.existsByAssessmentMatrixAndEmployeeEmail(assessmentMatrixId, employeeEmail);

    // Then
    assertThat(result).isFalse();
    verify(dynamoDBMapperMock).query(eq(EmployeeAssessment.class), any(DynamoDBQueryExpression.class));
  }
}
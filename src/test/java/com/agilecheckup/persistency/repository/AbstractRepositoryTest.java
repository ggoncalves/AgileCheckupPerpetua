package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.Company;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
abstract class AbstractRepositoryTest<T> {

  @Mock
  DynamoDBMapper dynamoDBMapperMock;

  private T mockedT;

  @BeforeEach
  void setUp() {
    // Mock
    mockedT = createMockedT();
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void save() {
    // When
    getRepository().save(mockedT);

    // Then
    verify(dynamoDBMapperMock).save(mockedT);
  }

  @Test
  void delete() {
    // When
    getRepository().delete(mockedT);

    // Then
    verify(dynamoDBMapperMock).delete(mockedT);
  }

  @Test
  void findById() {
    // When
    getRepository().findById(GENERIC_ID_1234);

    // Then
    verify(dynamoDBMapperMock).load(argThat(arg -> arg.isInstance(mockedT)), eq(GENERIC_ID_1234));
  }

  @Test
  void findAll() {
    // When
    getRepository().findAll();

    // Then
    verify(dynamoDBMapperMock).scan(argThat(arg -> arg.isInstance(mockedT)), any(DynamoDBScanExpression.class));
  }

  @Test
  void testFindAllByTenantId_shouldCallMapperWithCorrectQueryExpression() {
    // Given
    String tenantId = "test-tenant-id";
    PaginatedQueryList<T> expectedResult = mock(PaginatedQueryList.class);

    // Mock the behavior of dynamoDBMapper.query
    when(dynamoDBMapperMock.query(eq(getMockedClass()), any(DynamoDBQueryExpression.class)))
        .thenReturn(expectedResult);

    // When
    PaginatedQueryList<Company> actualResult = getRepository().findAllByTenantId(tenantId);

    // Then

    // Assert
    // Verify that dynamoDBMapper.query was called once
    ArgumentCaptor<DynamoDBQueryExpression<T>> queryExpressionCaptor =
        ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
    verify(dynamoDBMapperMock).query(eq(getMockedClass()), queryExpressionCaptor.capture());

    DynamoDBQueryExpression<T> capturedExpression = queryExpressionCaptor.getValue();

    assertEquals("tenantId-index", capturedExpression.getIndexName());
    assertEquals("tenantId = :tenantId", capturedExpression.getKeyConditionExpression());
    assertFalse(capturedExpression.isConsistentRead());

    Map<String, AttributeValue> expressionAttributeValues = capturedExpression.getExpressionAttributeValues();
    assertEquals(1, expressionAttributeValues.size());
    assertEquals(tenantId, expressionAttributeValues.get(":tenantId").getS());

    // Assert that the result of companyRepository.findAllByTenantId(...) is the same as the mocked PaginatedQueryList
    assertSame(expectedResult, actualResult);
  }

  abstract AbstractCrudRepository getRepository();
  abstract T createMockedT();
  abstract Class getMockedClass();
}
package com.agilecheckup.persistency.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static org.mockito.Mockito.*;

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
    verify(getRepository()).save(mockedT);
    verify(dynamoDBMapperMock).save(mockedT);
  }

  @Test
  void delete() {
    // When
    getRepository().delete(mockedT);

    // Then
    verify(getRepository()).delete(mockedT);
    verify(dynamoDBMapperMock).delete(mockedT);
  }

  @Test
  void findById() {
    // When
    getRepository().findById(GENERIC_ID_1234);

    // Then
    verify(getRepository()).findById(GENERIC_ID_1234);
    verify(dynamoDBMapperMock).load(argThat(arg -> arg.isInstance(mockedT)), eq(GENERIC_ID_1234));
  }

  @Test
  void findAll() {
    // When
    getRepository().findAll();

    // Then
    verify(getRepository()).findAll();
    verify(dynamoDBMapperMock).scan(argThat(arg -> arg.isInstance(mockedT)), any(DynamoDBScanExpression.class));
  }

  abstract AbstractCrudRepository getRepository();
  abstract T createMockedT();
}
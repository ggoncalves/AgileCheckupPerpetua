package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.Question;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.QUESTION_ID_1234;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
abstract class AbstractRepositoryTest<T> {

  @InjectMocks
  @Spy
  private QuestionRepository questionRepository;
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
    getRepository().findById(QUESTION_ID_1234);

    // Then
    verify(getRepository()).findById(QUESTION_ID_1234);
    verify(dynamoDBMapperMock).load(Question.class, QUESTION_ID_1234);
  }

  @Test
  void findAll() {
    // When
    getRepository().findAll();

    // Then
    verify(getRepository()).findAll();
    verify(dynamoDBMapperMock).scan(eq(mockedT.getClass()), any(DynamoDBScanExpression.class));
  }

  abstract AbstractCrudRepository getRepository();
  abstract T createMockedT();
}
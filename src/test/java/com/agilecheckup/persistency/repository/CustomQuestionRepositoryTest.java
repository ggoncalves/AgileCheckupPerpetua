package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.question.Question;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.createMockedCustomQuestion;

@ExtendWith(MockitoExtension.class)
class CustomQuestionRepositoryTest extends AbstractRepositoryTest<Question> {

  @InjectMocks
  @Spy
  private QuestionRepository questionRepository;

  @Override
  AbstractCrudRepository getRepository() {
    return questionRepository;
  }

  @Override
  Question createMockedT() {
    return createMockedCustomQuestion(GENERIC_ID_1234);
  }

  @Override
  Class getMockedClass() {
    return Question.class;
  }
}
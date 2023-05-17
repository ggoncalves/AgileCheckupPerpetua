package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.Question;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;

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
}
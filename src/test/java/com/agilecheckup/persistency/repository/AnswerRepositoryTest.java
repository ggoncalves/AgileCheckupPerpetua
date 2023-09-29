package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.question.Answer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.createMockedAnswer;

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
    return createMockedAnswer();
  }
}
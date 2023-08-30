package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.RateType;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.copyQuestionAndAddId;
import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest extends AbstractCrudServiceTest<Question, AbstractCrudRepository<Question>> {

  @InjectMocks
  @Spy
  private QuestionService questionService;

  @Mock
  private QuestionRepository questionRepository;

  private Question originalQuestion;

  @BeforeEach
  void setUpBefore() {
    originalQuestion = createMockedQuestion(DEFAULT_ID);
  }

  @Test
  void create() {
    Question savedQuestion = copyQuestionAndAddId(originalQuestion, DEFAULT_ID);

    // Prevent/Stub
    doReturn(savedQuestion).when(questionRepository).save(any());

    // When
    Optional<Question> questionOptional = questionService.create(originalQuestion.getQuestion(), originalQuestion.getRateType(), originalQuestion.getTenantId(), originalQuestion.getPoints());

    // Then
    assertTrue(questionOptional.isPresent());
    assertEquals(savedQuestion, questionOptional.get());
    verify(questionRepository).save(originalQuestion);
    verify(questionService).create("question", RateType.YES_NO, "tenantId", 5);
  }

  @Test
  void create_NullQuestion() {
    // When
    assertThrows(NullPointerException.class, () -> questionService.create(null, originalQuestion.getRateType(), originalQuestion.getTenantId(), originalQuestion.getPoints()));
  }

  @Test
  void create_NullPoints() {
    // When
    assertThrows(NullPointerException.class, () -> questionService.create(originalQuestion.getQuestion(), originalQuestion.getRateType(), originalQuestion.getTenantId(), null));
  }

  @Override
  AbstractCrudService<Question, AbstractCrudRepository<Question>> getCrudServiceSpy() {
    return questionService;
  }
}
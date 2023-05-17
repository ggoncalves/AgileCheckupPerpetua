package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Question;
import com.agilecheckup.persistency.entity.RateType;
import com.agilecheckup.persistency.repository.QuestionRepository;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

  private static final String QUESTION_ID = "1234";

  @InjectMocks
  @Spy
  private QuestionService questionService;

  @Mock
  private QuestionRepository questionRepository;

  private Question originalQuestion = createMockedQuestion();

  @Test
  void create() {
    Question savedQuestion = copyQuestionAndAddId(originalQuestion, QUESTION_ID);

    // Prevent/Stub
    doReturn(savedQuestion).when(questionRepository).save(originalQuestion);

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
    assertThrows(NullPointerException.class, () -> {
      questionService.create(null, originalQuestion.getRateType(), originalQuestion.getTenantId(), originalQuestion.getPoints());
    });
  }

  @Test
  void create_NullPoints() {
    // When
    assertThrows(NullPointerException.class, () -> {
      questionService.create(null, originalQuestion.getRateType(), originalQuestion.getTenantId(), originalQuestion.getPoints());
    });
  }
}
package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Question;
import com.agilecheckup.persistency.entity.RateType;
import com.agilecheckup.persistency.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

class QuestionServiceTest {

  private static final String QUESTION_ID = "1234";

  private QuestionService questionService;

  private QuestionRepository questionRepository = Mockito.mock(QuestionRepository.class);

  private Question originalQuestion = createMockedQuestion();

  @BeforeEach
  void setUp() {
    questionService = Mockito.spy(new QuestionService(questionRepository));
    assertNotNull(questionService);
  }

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

  private Question createMockedQuestion() {
    return Question.builder()
        .question("question")
        .rateType(RateType.YES_NO)
        .tenantId("tenantId")
        .points(5)
        .build();
  }

  private Question copyQuestionAndAddId(Question question, String id) {
    return Question.builder()
        .id(id)
        .question(question.getQuestion())
        .rateType(question.getRateType())
        .tenantId(question.getTenantId())
        .points(question.getPoints())
        .build();
  }
}
package com.agilecheckup.persistency.entity.question;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.createMockedCustomQuestion;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomizedAnswerStrategyTest {

  private Question question = createMockedCustomQuestion("MockedQuestion");
  private AnswerStrategy answerStrategy = CustomizedAnswerStrategy.builder()
      .question(question)
      .build();

  @Test
  void isValidValueTrue() {
    assertTrue(answerStrategy.isValidValue(1));
    assertTrue(answerStrategy.isValidValue(2));
    assertTrue(answerStrategy.isValidValue(3));
    assertTrue(answerStrategy.isValidValue(4));
    assertTrue(answerStrategy.isValidValue(5));
  }

  @Test
  void isValidValueFalse() {
    assertFalse(answerStrategy.isValidValue(0));
    assertFalse(answerStrategy.isValidValue(6));
    assertFalse(answerStrategy.isValidValue(-1));
    assertFalse(answerStrategy.isValidValue(10));
    assertFalse(answerStrategy.isValidValue(100));
    assertFalse(answerStrategy.isValidValue(7));
  }

}
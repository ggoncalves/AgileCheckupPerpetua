package com.agilecheckup.persistency.entity.question;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.createMockedCustomQuestion;
import static org.junit.jupiter.api.Assertions.*;

class CustomizedAnswerStrategyTest {
  private Question question = createMockedCustomQuestion("MockedQuestion");
  private AnswerStrategy answerStrategy = CustomizedAnswerStrategy.builder()
      .question(question)
      .build();

  private void assertIsValidValue(boolean expected, Integer... values) {
    for (Integer value : values) {
      assertEquals(expected, answerStrategy.isValidValue(value));
    }
  }

  @Test
  void isValidValueTrue() {
    assertIsValidValue(true, 1, 2, 3, 4, 5);
  }

  @Test
  void isValidValueFalse() {
    assertIsValidValue(false, 0, 6, -1, 10, 100, 7);
  }
}
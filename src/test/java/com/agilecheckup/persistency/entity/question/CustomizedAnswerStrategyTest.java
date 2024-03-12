package com.agilecheckup.persistency.entity.question;

import org.junit.jupiter.api.Test;

import static com.agilecheckup.util.TestObjectFactory.createMockedCustomQuestion;
import static org.junit.jupiter.api.Assertions.*;

class CustomizedAnswerStrategyTest {

  private final Question singleChoiceQuestion = createMockedCustomQuestion("MockedQuestion", false, 0, 5, 6, 7, 10);
  private final Question multipleChoiceQuestion = createMockedCustomQuestion("MockedQuestion", true, 0, 5, 6, 7, 10);

  private void assertIsValidValue(boolean expected, AnswerStrategy<String> answerStrategy, String values) {
    assertEquals(expected, answerStrategy.isValidValue(values));
  }

  @Test
  void shouldReturnTrueToIndividualValueWhenSingleChoice() {
    AnswerStrategy<String> answerStrategy = createAnswerStrategyFor(singleChoiceQuestion);
    assertIsValidValue(true, answerStrategy, "1");
    assertIsValidValue(true, answerStrategy, "2");
    assertIsValidValue(true, answerStrategy, "3");
    assertIsValidValue(true, answerStrategy, "4");
    assertIsValidValue(true, answerStrategy, "5");
    String value = answerStrategy.assignValue("1");
    assertEquals("1", value);
  }

  @Test
  void shouldReturnFalseToIndividualValueWhenSingleChoice() {
    AnswerStrategy<String> answerStrategy = createAnswerStrategyFor(singleChoiceQuestion);
    assertIsValidValue(false, answerStrategy, "0");
    assertIsValidValue(false, answerStrategy, "6");
    assertIsValidValue(false, answerStrategy, "-1");
    assertIsValidValue(false, answerStrategy, "10");
    assertIsValidValue(false, answerStrategy, "7");
    assertIsValidValue(false, answerStrategy, "100");
  }

  @Test
  void shouldReturnFalseToIndividualValueWhenMultipleChoice() {
    AnswerStrategy<String> answerStrategy = createAnswerStrategyFor(multipleChoiceQuestion);
    assertIsValidValue(false, answerStrategy, "0");
    assertIsValidValue(false, answerStrategy, "6");
    assertIsValidValue(false, answerStrategy, "-1");
    assertIsValidValue(false, answerStrategy, "10");
    assertIsValidValue(false, answerStrategy, "7");
    assertIsValidValue(false, answerStrategy, "100");
  }

  @Test
  void shouldReturnFalseToMultipleValuesWhenSingleChoice() {
    AnswerStrategy<String> answerStrategy = createAnswerStrategyFor(singleChoiceQuestion);
    assertIsValidValue(false, answerStrategy, "1,2");
    assertIsValidValue(false, answerStrategy, "2,3,5");
    assertIsValidValue(false, answerStrategy, "1,3,4,5");
    assertIsValidValue(false, answerStrategy, "1,2,3,4,5");
  }

  @Test
  void shouldReturnTrueToMultipleValuesWhenMultipleChoice() {
    AnswerStrategy<String> answerStrategy = createAnswerStrategyFor(multipleChoiceQuestion);
    assertIsValidValue(true, answerStrategy, "1,2");
    assertIsValidValue(true, answerStrategy, "2,3,5");
    assertIsValidValue(true, answerStrategy, "1,3,4,5");
    assertIsValidValue(true, answerStrategy, "1,2,3,4,5");
    String value = answerStrategy.assignValue("1,2,3,4,5");
    assertEquals("1,2,3,4,5", value);
    value = answerStrategy.assignValue("1,2,5");
    assertEquals("1,2,5", value);
  }

  @Test
  void shouldReturnFalseToMultipleValuesWhenMultipleChoice() {
    AnswerStrategy<String> answerStrategy = createAnswerStrategyFor(multipleChoiceQuestion);
    assertIsValidValue(false, answerStrategy, "1,6");
    assertIsValidValue(false, answerStrategy, "2,3,0");
    assertIsValidValue(false, answerStrategy, "1,3,-4,5");
    assertIsValidValue(false, answerStrategy, "1,2,30,4,5");
  }

  @Test
  void shouldReturnFalseToSingleChoiceInvalidPattern() {
    AnswerStrategy<String> answerStrategy = createAnswerStrategyFor(singleChoiceQuestion);
    assertIsValidValue(false, answerStrategy, "1,6");
    assertIsValidValue(false, answerStrategy, ",1");
    assertIsValidValue(false, answerStrategy, "1,2,2,3");
    assertIsValidValue(false, answerStrategy, "1,1");
    assertIsValidValue(false, answerStrategy, "1;1");
    assertIsValidValue(false, answerStrategy, "1*1");
  }

  @Test
  void shouldReturnFalseToMultipleChoiceInvalidPattern() {
    AnswerStrategy<String> answerStrategy = createAnswerStrategyFor(singleChoiceQuestion);
    assertIsValidValue(false, answerStrategy, ",1,2,");
    assertIsValidValue(false, answerStrategy, "1,2,2,3");
    assertIsValidValue(false, answerStrategy, "1,1");
    assertIsValidValue(false, answerStrategy, "1;1");
    assertIsValidValue(false, answerStrategy, "1*1");
  }

  private AnswerStrategy<String> createAnswerStrategyFor(Question question) {
    return CustomizedAnswerStrategy.builder()
        .question(question)
        .build();
  }
}
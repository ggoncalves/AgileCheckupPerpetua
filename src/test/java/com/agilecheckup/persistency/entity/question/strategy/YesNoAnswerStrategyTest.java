package com.agilecheckup.persistency.entity.question.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YesNoAnswerStrategyTest {

  private YesNoAnswerStrategy strategy = YesNoAnswerStrategy.builder().build();

  @Test
  void isValidValue_True() {
    assertTrue(strategy.isValidValue(true));
    assertTrue(strategy.isValidValue(false));
  }

  @Test
  void allowNullValue_False_Default() {
    YesNoAnswerStrategy strategy = YesNoAnswerStrategy.builder().build();
    assertFalse(strategy.isAllowNullValue());
  }

  @Test
  void allowNullValue_False() {
    YesNoAnswerStrategy strategy = YesNoAnswerStrategy.builder().allowNullValue(false).build();
    assertFalse(strategy.isAllowNullValue());
  }

  @Test
  void allowNullValue_True() {
    YesNoAnswerStrategy strategy = YesNoAnswerStrategy.builder().allowNullValue(true).build();
    assertTrue(strategy.isAllowNullValue());
  }

  @Test
  void isYesAnswer() {
    YesNoAnswerStrategy strategy = YesNoAnswerStrategy.builder().build();
    strategy.assignValue("true");
    assertTrue(strategy.isYesAnswer());
    assertFalse(strategy.isNoAnswer());
  }

  @Test
  void isNoAnswer() {
    YesNoAnswerStrategy strategy = YesNoAnswerStrategy.builder().build();
    strategy.assignValue("false");
    assertFalse(strategy.isYesAnswer());
    assertTrue(strategy.isNoAnswer());
  }

  @Test
  void invalidStringFormatAnswer() {
    YesNoAnswerStrategy strategy = YesNoAnswerStrategy.builder().build();
    strategy.assignValue("6a7");
    assertFalse(strategy.isYesAnswer());
    assertTrue(strategy.isNoAnswer());
  }

  @Test
  void isNullAnswer() {
    YesNoAnswerStrategy strategy = YesNoAnswerStrategy.builder().allowNullValue(true).build();
    strategy.assignValue(null);
    assertFalse(strategy.isYesAnswer());
    assertFalse(strategy.isNoAnswer());
  }
}
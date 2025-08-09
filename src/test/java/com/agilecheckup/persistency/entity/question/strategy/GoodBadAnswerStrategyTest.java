package com.agilecheckup.persistency.entity.question.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoodBadAnswerStrategyTest {

  private GoodBadAnswerStrategy strategy = GoodBadAnswerStrategy.builder().build();

  @Test
  void isValidValue_True() {
    assertTrue(strategy.isValidValue(true));
    assertTrue(strategy.isValidValue(false));
  }

  @Test
  void allowNullValue_False_Default() {
    GoodBadAnswerStrategy strategy = GoodBadAnswerStrategy.builder().build();
    assertFalse(strategy.isAllowNullValue());
  }

  @Test
  void allowNullValue_False() {
    GoodBadAnswerStrategy strategy = GoodBadAnswerStrategy.builder().allowNullValue(false).build();
    assertFalse(strategy.isAllowNullValue());
  }

  @Test
  void allowNullValue_True() {
    GoodBadAnswerStrategy strategy = GoodBadAnswerStrategy.builder().allowNullValue(true).build();
    assertTrue(strategy.isAllowNullValue());
  }

  @Test
  void isYesAnswer() {
    GoodBadAnswerStrategy strategy = GoodBadAnswerStrategy.builder().build();
    strategy.assignValue("true");
    assertTrue(strategy.isGoodAnswer());
    assertFalse(strategy.isBadAnswer());
  }

  @Test
  void isNoAnswer() {
    GoodBadAnswerStrategy strategy = GoodBadAnswerStrategy.builder().build();
    strategy.assignValue("false");
    assertFalse(strategy.isGoodAnswer());
    assertTrue(strategy.isBadAnswer());
  }

  @Test
  void isNullAnswer() {
    GoodBadAnswerStrategy strategy = GoodBadAnswerStrategy.builder().allowNullValue(true).build();
    strategy.assignValue(null);
    assertFalse(strategy.isGoodAnswer());
    assertFalse(strategy.isBadAnswer());
  }
}
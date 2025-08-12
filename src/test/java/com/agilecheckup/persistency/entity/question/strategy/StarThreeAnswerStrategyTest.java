package com.agilecheckup.persistency.entity.question.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StarThreeAnswerStrategyTest {

  private StarThreeAnswerStrategy strategy = StarThreeAnswerStrategy.builder().build();

  @Test
  void isValidValue_True() {
    assertTrue(strategy.isValidValue(1));
    assertTrue(strategy.isValidValue(2));
    assertTrue(strategy.isValidValue(3));
  }

  @Test
  void isValidValue_False_Negative() {
    assertFalse(strategy.isValidValue(-1));
    assertFalse(strategy.isValidValue(-2));
    assertFalse(strategy.isValidValue(-20));
  }

  @Test
  void isValidValue_False_Zero() {
    assertFalse(strategy.isValidValue(0));
  }

  @Test
  void isValidValue_False_UpperBounds() {
    assertFalse(strategy.isValidValue(4));
    assertFalse(strategy.isValidValue(5));
    assertFalse(strategy.isValidValue(10));
  }

  @Test
  void allowNullValue_False_Default() {
    StarThreeAnswerStrategy strategy = StarThreeAnswerStrategy.builder().build();
    assertFalse(strategy.isAllowNullValue());
  }

  @Test
  void allowNullValue_False() {
    StarThreeAnswerStrategy strategy = StarThreeAnswerStrategy.builder().allowNullValue(false).build();
    assertFalse(strategy.isAllowNullValue());
  }

  @Test
  void allowNullValue_True() {
    StarThreeAnswerStrategy strategy = StarThreeAnswerStrategy.builder().allowNullValue(true).build();
    assertTrue(strategy.isAllowNullValue());
  }
}
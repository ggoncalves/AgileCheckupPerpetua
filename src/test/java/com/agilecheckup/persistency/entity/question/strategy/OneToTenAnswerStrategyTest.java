package com.agilecheckup.persistency.entity.question.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OneToTenAnswerStrategyTest {

  private OneToTenAnswerStrategy strategy = OneToTenAnswerStrategy.builder().build();

  @Test
  void isValidValue_True() {
    assertTrue(strategy.isValidValue(1));
    assertTrue(strategy.isValidValue(2));
    assertTrue(strategy.isValidValue(5));
    assertTrue(strategy.isValidValue(9));
    assertTrue(strategy.isValidValue(10));
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
    assertFalse(strategy.isValidValue(11));
    assertFalse(strategy.isValidValue(12));
    assertFalse(strategy.isValidValue(20));
  }

  @Test
  void allowNullValue_False_Default() {
    OneToTenAnswerStrategy strategy = OneToTenAnswerStrategy.builder().build();
    assertFalse(strategy.isAllowNullValue());
  }

  @Test
  void allowNullValue_False() {
    OneToTenAnswerStrategy strategy = OneToTenAnswerStrategy.builder().allowNullValue(false).build();
    assertFalse(strategy.isAllowNullValue());
  }

  @Test
  void allowNullValue_True() {
    OneToTenAnswerStrategy strategy = OneToTenAnswerStrategy.builder().allowNullValue(true).build();
    assertTrue(strategy.isAllowNullValue());
  }
}
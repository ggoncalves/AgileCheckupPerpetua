package com.agilecheckup.persistency.entity.question.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import lombok.experimental.SuperBuilder;

class AnswerIntegerStrategyTest {

  private TestAnswerIntegerStrategy strategy = TestAnswerIntegerStrategy.builder().build();

  @Test
  void valueToString_Negative() {
    assertEquals("-5", strategy.valueToString(Integer.valueOf(-5)));
  }

  @Test
  void valueToString_Positive() {
    assertEquals("5", strategy.valueToString(Integer.valueOf(5)));
  }

  @Test
  void valueToString_Zero() {
    assertEquals("0", strategy.valueToString(Integer.valueOf(0)));
  }

  @SuperBuilder
  private static class TestAnswerIntegerStrategy extends AnswerIntegerStrategy {
    @Override
    boolean isValidValue(Integer value) {
      return false;
    }
  }
}
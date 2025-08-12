package com.agilecheckup.persistency.entity.question.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import lombok.experimental.SuperBuilder;

class AnswerBooleanStrategyTest {

  private TestAnswerBooleanStrategy strategy = TestAnswerBooleanStrategy.builder().build();

  @Test
  void valueToString_True() {
    assertEquals("true", strategy.valueToString(Boolean.TRUE));
  }

  @Test
  void valueToString_False() {
    assertEquals("false", strategy.valueToString(Boolean.FALSE));
  }

  @SuperBuilder
  private static class TestAnswerBooleanStrategy extends AnswerBooleanStrategy {
  }
}
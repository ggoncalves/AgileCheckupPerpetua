package com.agilecheckup.persistency.entity.question.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import lombok.experimental.SuperBuilder;

class AnswerStringStrategyTest {

  private TestAnswerStringStrategy strategy = TestAnswerStringStrategy.builder().build();

  @Test
  void valueToString() {
    assertEquals("abcd", strategy.valueToString("abcd"));
  }

  @SuperBuilder
  private static class TestAnswerStringStrategy extends AnswerStringStrategy {
    @Override
    boolean isValidValue(String value) {
      return false;
    }
  }
}
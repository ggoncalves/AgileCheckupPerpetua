package com.agilecheckup.persistency.entity.question;

import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
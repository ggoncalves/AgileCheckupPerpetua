package com.agilecheckup.persistency.entity.question;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class AnswerIntegerIntervalStrategyTest {

  @Test
  void isValidValue_InvalidLowerBounds() {
    AnswerIntegerIntervalStrategy strategy = AnswerIntegerIntervalStrategy.builder()
        .minValue(1)
        .maxValue(3)
        .build();

    assertFalse(strategy.isValidValue(0));
    assertFalse(strategy.isValidValue(-1));
    assertFalse(strategy.isValidValue(-20));
  }

  @Test
  void isValidValue_InvalidUpperBounds() {
    AnswerIntegerIntervalStrategy strategy = AnswerIntegerIntervalStrategy.builder()
        .minValue(1)
        .maxValue(5)
        .build();

    assertFalse(strategy.isValidValue(6));
    assertFalse(strategy.isValidValue(7));
    assertFalse(strategy.isValidValue(20));
  }
}
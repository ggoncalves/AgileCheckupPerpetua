package com.agilecheckup.persistency.entity.question.strategy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class AnswerIntegerIntervalStrategy extends AnswerIntegerStrategy {

  protected Integer minValue;
  protected Integer maxValue;

  @Override
  boolean isValidValue(Integer value) {
    return (value >= minValue && value <= maxValue);
  }

}

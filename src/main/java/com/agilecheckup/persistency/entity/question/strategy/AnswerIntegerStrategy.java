package com.agilecheckup.persistency.entity.question.strategy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper=true)
@SuperBuilder
public abstract class AnswerIntegerStrategy extends AnswerStrategy<Integer> {

  @Override
  public Integer stringToValue(String valueString) {
    return Integer.parseInt(valueString);
  }

  @Override
  public String valueToString(Integer value) {
    return Integer.toString(value);
  }
}

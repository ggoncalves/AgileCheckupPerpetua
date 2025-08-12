package com.agilecheckup.persistency.entity.question.strategy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class OpenAnswerStrategy extends AnswerStringStrategy {

  private final static Integer MAX_CHARACTERS = 500;

  @Override
  boolean isValidValue(String value) {
    return !value.isEmpty() && value.length() <= MAX_CHARACTERS;
  }
}

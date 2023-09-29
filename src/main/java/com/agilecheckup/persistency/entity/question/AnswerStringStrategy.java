package com.agilecheckup.persistency.entity.question;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper=true)
@SuperBuilder
public abstract class AnswerStringStrategy extends AnswerStrategy<String> {

  @Override
  public String stringToValue(@NonNull String valueString) {
    return valueString;
  }

  @Override
  public String valueToString(String value) {
    return value;
  }
}

package com.agilecheckup.persistency.entity.question;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class AnswerBooleanStrategy extends AnswerStrategy<Boolean> {

  @Override
  boolean isValidValue(Boolean value) {
    return true;
  }

  @Override
  public String valueToString(Boolean value) {
    return Boolean.toString(value);
  }

  @Override
  public Boolean stringToValue(@NonNull String valueString) {
    return Boolean.valueOf(valueString);
  }

}

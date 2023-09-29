package com.agilecheckup.persistency.entity.question;

import com.amazonaws.services.dynamodbv2.model.InternalServerErrorException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class CustomizedAnswerStrategy extends AnswerStrategy<Integer> {

  private static final Integer MIN_VALUE = 1;

  @Override
  boolean isValidValue(Integer value) {
    return value >= MIN_VALUE && value <= getQuestion().getOptionGroup().getOptionMap().size();
  }

  @Override
  public String valueToString(Integer value) {
    return Integer.toString(value);
  }

  @Override
  public Integer stringToValue(@NonNull String valueString) {
    return Integer.parseInt(valueString);
  }
}

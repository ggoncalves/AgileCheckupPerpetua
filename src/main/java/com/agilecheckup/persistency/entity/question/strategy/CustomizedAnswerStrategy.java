package com.agilecheckup.persistency.entity.question.strategy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.regex.Pattern;

import static com.agilecheckup.persistency.entity.question.CustomizedValuesSplitter.getSplitValues;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class CustomizedAnswerStrategy extends AnswerStrategy<String> {

  private static final Integer MIN_VALUE = 1;

  private static final Pattern MULTIPLE_CHOICE_PATTERN = Pattern.compile("^(?!.*?(\\b\\d+\\b).*?\\1)[1-9]\\d*(?:,[1-9]\\d*)*$");
  private static final Pattern SINGLEs_CHOICE_PATTERN = Pattern.compile("^[0-9]+$");

  @Override
  boolean isValidValue(String value) {
    if (!isStringValid(value)) return false;
    for (String stringValue : getSplitValues(value)) {
      int intValue = Integer.parseInt(stringValue);
      if (intValue < MIN_VALUE || intValue > getQuestion().getOptionGroup().getOptionMap().size()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String valueToString(String value) {
    return value;
  }

  @Override
  public String stringToValue(@NonNull String valueString) {
    return valueString;
  }

  private boolean isStringValid(String value) {
    return getPattern().matcher(value).matches();
  }

  private Pattern getPattern() {
    if (getQuestion().getOptionGroup().isMultipleChoice()) return MULTIPLE_CHOICE_PATTERN;
    return SINGLEs_CHOICE_PATTERN;
  }
}

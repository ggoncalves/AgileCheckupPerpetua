package com.agilecheckup.persistency.entity.question;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public abstract class AnswerStrategy<T> {

  private static final String INVALID_VALUE_MESSAGE_FORMAT = "Invalid Answer value: %s";

  private static final String UNPARSEABLE_VALUE_MESSAGE_FORMAT = "Invalid Answer value. Not parseable: %s";

  private T value;

  private Question question;

  // TODO: Must refactor to be inside QuestionValidationRules
  // QuestionValidationRules must have allowNullValue, answerMaxCharacters.
  @Builder.Default
  private boolean allowNullValue = false;

  public T assignValue(String valueString) {
    if (valueString == null) assignNullValue();
    T value = null;
    try {
      value = nullableStringToValue(valueString);
    }
    catch (Exception exception) {
      throwUnparseableValueException(valueString);
    }
    if (value != null && !isValidValue(value)) {
      throwInvalidValueException(valueString);
    }
    return this.value = value;
  }

  protected boolean isNullValue() {
    return value == null;
  }

  private void assignNullValue() {
    if (isAllowNullValue()) this.value = null;
    else throwInvalidValueException(null);
  }

  private void throwInvalidValueException(String valueString) {
    String errorMessage = formatInvalidValueErrorMessage(valueString);
    // TODO: This exception can be changed to a subclass of ValidationException
    // However, this decision should be made once the project has the validation being made from the API
    // Then this decision should be easier.
    throw new IllegalArgumentException(errorMessage);
  }

  private void throwUnparseableValueException(String valueString) {
    String errorMessage = formatUnparseableValueErrorMessage(valueString);
    // TODO: This exception can be changed to a subclass of ValidationException
    // However, this decision should be made once the project has the validation being made from the API
    // Then this decision should be easier.
    throw new IllegalArgumentException(errorMessage);
  }

  private String formatInvalidValueErrorMessage(String valueString) {
    return String.format(INVALID_VALUE_MESSAGE_FORMAT, valueString);
  }

  private String formatUnparseableValueErrorMessage(String valueString) {
    return String.format(UNPARSEABLE_VALUE_MESSAGE_FORMAT, valueString);
  }

  private T nullableStringToValue(String valueString) {
    if (valueString == null) return null;
    return stringToValue(valueString);
  }

  public String valueToString() {
    return valueToString(getValue());
  }

  // If the value is null, this method is never called.
  abstract boolean isValidValue(T value);

  public abstract String valueToString(T value);

  public abstract T stringToValue(@NonNull String valueString);

}
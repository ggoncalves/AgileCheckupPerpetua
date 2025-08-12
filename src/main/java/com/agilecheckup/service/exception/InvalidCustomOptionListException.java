package com.agilecheckup.service.exception;

import java.util.Arrays;

import lombok.Getter;

public class InvalidCustomOptionListException extends ValidationException {

  public InvalidCustomOptionListException(String reason) {
    super(reason);
  }

  public InvalidCustomOptionListException(InvalidReasonEnum invalidReasonEnum, int min, int max) {
    this(String.format(invalidReasonEnum.reason, getValue(invalidReasonEnum, min, max)));
  }

  public InvalidCustomOptionListException(InvalidReasonEnum invalidReasonEnum, Integer[] ids) {
    this(String.format(invalidReasonEnum.reason, formatIds(ids)));
  }

  public static String formatIds(Integer[] ids) {
    return Arrays.stream(ids).map(String::valueOf).reduce((id1, id2) -> id1 + ", " + id2).orElse("");
  }

  private static int getValue(InvalidReasonEnum invalidReasonEnum, int min, int max) {
    switch (invalidReasonEnum) {
      case OPTION_LIST_TOO_BIG:
        return max;
      default:
        return min;
    }
  }

  @Getter
  public enum InvalidReasonEnum {
    OPTION_LIST_EMPTY("Question option list is empty. There should be at least %s options."), OPTION_LIST_TEXT_EMPTY("Question option list text is empty."),

    OPTION_LIST_TOO_SHORT("Question option list too small. There should be at least %s options."), OPTION_LIST_TOO_BIG("Question option list too big. There should be at max %s options."), INVALID_OPTIONS_IDS("Question option list must have ids from 1 to last with no missing values and without " + "duplicated " + "ids. The ids in the sorted order: %s");

    protected String reason;

    InvalidReasonEnum(String reason) {
      this.reason = reason;
    }
  }

}

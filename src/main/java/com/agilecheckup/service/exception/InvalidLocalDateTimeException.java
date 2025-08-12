package com.agilecheckup.service.exception;

import com.google.common.annotations.VisibleForTesting;

public class InvalidLocalDateTimeException extends ValidationException {

  public InvalidLocalDateTimeException(String reason) {
    super(reason);
  }

  public InvalidLocalDateTimeException(InvalidReasonEnum invalidReasonEnum) {
    this(invalidReasonEnum.reason);
  }

  public enum InvalidReasonEnum {
    FUTURE_60_MIN("Can't create the answer in the future");

    @VisibleForTesting
    String reason;

    InvalidReasonEnum(String reason) {
      this.reason = reason;
    }
  }

}

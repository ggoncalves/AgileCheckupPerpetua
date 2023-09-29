package com.agilecheckup.service.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InvalidLocalDateTimeExceptionTest {

  private static final String CUSTOM_MESSAGE = "Custom";

  @Test
  void testMessageCustomReason() {
    InvalidLocalDateTimeException exception = new InvalidLocalDateTimeException(CUSTOM_MESSAGE);
    assertEquals(CUSTOM_MESSAGE, exception.getMessage());
  }

  @Test
  void testMessage() {
    InvalidLocalDateTimeException exception = new InvalidLocalDateTimeException(InvalidLocalDateTimeException.InvalidReasonEnum.FUTURE_60_MIN);
    assertEquals(InvalidLocalDateTimeException.InvalidReasonEnum.FUTURE_60_MIN.reason, exception.getMessage());
  }
}
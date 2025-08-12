package com.agilecheckup.service.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvalidCustomOptionListExceptionTest {

  private static final String CUSTOM_MESSAGE = "Custom";

  @Test
  void testMessageCustomReason() {
    InvalidCustomOptionListException exception = new InvalidCustomOptionListException(CUSTOM_MESSAGE);
    assertEquals(CUSTOM_MESSAGE, exception.getMessage());
  }

  @Test
  void testMessage_Empty() {
    InvalidCustomOptionListException exception = new InvalidCustomOptionListException(InvalidCustomOptionListException.InvalidReasonEnum.OPTION_LIST_EMPTY, 2, 64);
    assertEquals("Question option list is empty. There should be at least 2 options.", exception.getMessage());
  }

  @Test
  void testMessage_TooShort() {
    InvalidCustomOptionListException exception = new InvalidCustomOptionListException(InvalidCustomOptionListException.InvalidReasonEnum.OPTION_LIST_TOO_SHORT, 2, 64);
    assertEquals("Question option list too small. There should be at least 2 options.", exception.getMessage());
  }

  @Test
  void testMessage_EmptyText() {
    InvalidCustomOptionListException exception = new InvalidCustomOptionListException(InvalidCustomOptionListException.InvalidReasonEnum.OPTION_LIST_TEXT_EMPTY, 2, 64);
    assertEquals("Question option list text is empty.", exception.getMessage());
  }

  @Test
  void testMessage_TooBig() {
    InvalidCustomOptionListException exception = new InvalidCustomOptionListException(InvalidCustomOptionListException.InvalidReasonEnum.OPTION_LIST_TOO_BIG, 2, 64);
    assertEquals("Question option list too big. There should be at max 64 options.", exception.getMessage());
  }

  @Test
  void testMessage_InvalidOptionIds() {
    Integer[] array = {1, 1, 2};
    InvalidCustomOptionListException exception = new InvalidCustomOptionListException(InvalidCustomOptionListException.InvalidReasonEnum.INVALID_OPTIONS_IDS, array);
    assertEquals("Question option list must have ids from 1 to last with no missing values and without duplicated ids. The ids in the sorted order: 1, 1, 2", exception.getMessage());
  }
}
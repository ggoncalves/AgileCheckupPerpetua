package com.agilecheckup.persistency.entity.question.strategy;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.experimental.SuperBuilder;

@ExtendWith(MockitoExtension.class)
class AnswerStrategyTest {

  private static Integer INVALID_INTEGER_VALUE = 6;
  private static String INVALID_INTEGER_VALUE_STRING = INVALID_INTEGER_VALUE.toString();
  private static Integer VALID_INTEGER_VALUE = 3;

  private static String VALID_INTEGER_VALUE_STRING = VALID_INTEGER_VALUE.toString();
  private static String INVALID_STRING_VALUE = "Invalid";

  private static String VALID_STRING_VALUE = "Valid";

  @Spy
  private IntegerMockAnswerStrategy integerMockAnswerStrategy = IntegerMockAnswerStrategy.builder().build();

  @Spy
  private IntegerMockAnswerStrategy integerMockAllowNullValueAnswerStrategy = IntegerMockAnswerStrategy.builder()
                                                                                                       .allowNullValue(true)
                                                                                                       .build();

  @Spy
  private StringMockAnswerStrategy stringMockAnswerStrategy = StringMockAnswerStrategy.builder().build();

  @Test
  void shouldThrowExceptionWhenAssigningInvalidIntegerValue() {
    doReturn(false).when(integerMockAnswerStrategy).isValidValue(any());

    // When
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> integerMockAnswerStrategy.assignValue(INVALID_INTEGER_VALUE_STRING));

    // Then
    assertEquals("Invalid Answer value: 6", exception.getMessage());
    assertNull(integerMockAnswerStrategy.getValue());

    verify(integerMockAnswerStrategy).stringToValue(INVALID_INTEGER_VALUE_STRING);
    verify(integerMockAnswerStrategy).isValidValue(INVALID_INTEGER_VALUE);
  }

  @Test
  void shouldThrowExceptionWhenAssigningUnparseableIntegerValue() {
    // When
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> integerMockAnswerStrategy.assignValue("Unparseable"));

    // Then
    assertEquals("Invalid Answer value. Not parseable: Unparseable", exception.getMessage());
    assertNull(integerMockAnswerStrategy.getValue());

    verify(integerMockAnswerStrategy).stringToValue("Unparseable");
    verify(integerMockAnswerStrategy, never()).isValidValue(INVALID_INTEGER_VALUE);
    verify(integerMockAnswerStrategy, never()).valueToString(INVALID_INTEGER_VALUE);
  }

  @Test
  void shouldThrowExceptionWhenAssigningInvalidStringValue() {
    doReturn(false).when(stringMockAnswerStrategy).isValidValue(any());

    // When
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> stringMockAnswerStrategy.assignValue(INVALID_STRING_VALUE));

    // Then
    assertEquals("Invalid Answer value: Invalid", exception.getMessage());
    assertNull(stringMockAnswerStrategy.getValue());

    verify(stringMockAnswerStrategy).stringToValue(INVALID_STRING_VALUE);
    verify(stringMockAnswerStrategy).isValidValue(INVALID_STRING_VALUE);
  }

  @Test
  void shouldThrowExceptionWhenAssigningNullIntegerValue() {
    // When
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> integerMockAnswerStrategy.assignValue(null));

    // Then
    assertEquals("Invalid Answer value: null", exception.getMessage());
    assertNull(integerMockAnswerStrategy.getValue());

    verify(integerMockAnswerStrategy, never()).stringToValue(INVALID_INTEGER_VALUE_STRING);
    verify(integerMockAnswerStrategy, never()).isValidValue(any());
    verify(integerMockAnswerStrategy, never()).valueToString(null);
  }

  @Test
  void shouldThrowExceptionWhenAssigningNullStringValue() {
    // When
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> stringMockAnswerStrategy.assignValue(null));

    // Then
    assertEquals("Invalid Answer value: null", exception.getMessage());
    assertNull(stringMockAnswerStrategy.getValue());

    verify(stringMockAnswerStrategy, never()).stringToValue(null);
    verify(stringMockAnswerStrategy, never()).isValidValue(any());
    verify(stringMockAnswerStrategy, never()).valueToString(null);
  }

  @Test
  void shouldAssignValidIntegerValueSuccessfully() {
    doReturn(true).when(integerMockAnswerStrategy).isValidValue(any());

    // When
    integerMockAnswerStrategy.assignValue(VALID_INTEGER_VALUE_STRING);

    // Then
    assertEquals(VALID_INTEGER_VALUE, integerMockAnswerStrategy.getValue());

    verify(integerMockAnswerStrategy).stringToValue(VALID_INTEGER_VALUE_STRING);
    verify(integerMockAnswerStrategy).isValidValue(VALID_INTEGER_VALUE);
    verify(integerMockAnswerStrategy, never()).valueToString(any());
  }

  @Test
  void shouldAssignValidStringValueSuccessfully() {
    doReturn(true).when(stringMockAnswerStrategy).isValidValue(any());

    // When
    stringMockAnswerStrategy.assignValue(VALID_STRING_VALUE);

    // Then
    assertEquals(VALID_STRING_VALUE, stringMockAnswerStrategy.getValue());

    verify(stringMockAnswerStrategy).stringToValue(VALID_STRING_VALUE);
    verify(stringMockAnswerStrategy).isValidValue(VALID_STRING_VALUE);
    verify(stringMockAnswerStrategy, never()).valueToString(any());
  }

  @Test
  void shouldAssignNullValueWhenAllowNullIsTrue() {
    // When
    integerMockAllowNullValueAnswerStrategy.assignValue(null);

    // Then
    assertNull(integerMockAllowNullValueAnswerStrategy.getValue());

    verify(integerMockAllowNullValueAnswerStrategy, never()).stringToValue(null);
    verify(integerMockAllowNullValueAnswerStrategy, never()).isValidValue(any());
    verify(integerMockAllowNullValueAnswerStrategy, never()).valueToString(any());
  }

  @SuperBuilder
  private static class IntegerMockAnswerStrategy extends AnswerIntegerStrategy {

    @Override
    boolean isValidValue(Integer value) {
      return false;
    }
  }

  @SuperBuilder
  private static class StringMockAnswerStrategy extends AnswerStringStrategy {

    @Override
    boolean isValidValue(String value) {
      return false;
    }
  }
}
package com.agilecheckup.persistency.entity.question.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.SecureRandom;

import org.junit.jupiter.api.Test;

class OpenAnswerStrategyTest {

  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  private static final SecureRandom RANDOM = new SecureRandom();

  private OpenAnswerStrategy strategy = OpenAnswerStrategy.builder().build();

  private static final String VALID_STRING_1_CHARS = generateRandomString(1);
  private static final String VALID_STRING_10_CHARS = generateRandomString(10);
  private static final String VALID_STRING_500_CHARS = generateRandomString(500);
  private static final String VALID_STRING_501_CHARS = generateRandomString(501);

  @Test
  void isValidValue_True() {
    assertTrue(strategy.isValidValue(VALID_STRING_1_CHARS));
    assertTrue(strategy.isValidValue(VALID_STRING_10_CHARS));
    assertTrue(strategy.isValidValue(VALID_STRING_500_CHARS));
  }

  @Test
  void isValidValue_False() {
    assertFalse(strategy.isValidValue(""));
    assertFalse(strategy.isValidValue(VALID_STRING_501_CHARS));
  }

  @Test
  void allowNullValue_False_Default() {
    OpenAnswerStrategy strategy = OpenAnswerStrategy.builder().build();
    assertFalse(strategy.isAllowNullValue());
  }

  @Test
  void allowNullValue_False() {
    OpenAnswerStrategy strategy = OpenAnswerStrategy.builder().allowNullValue(false).build();
    assertFalse(strategy.isAllowNullValue());
  }

  @Test
  void allowNullValue_True() {
    OpenAnswerStrategy strategy = OpenAnswerStrategy.builder().allowNullValue(true).build();
    assertTrue(strategy.isAllowNullValue());
  }

  public static String generateRandomString(int length) {
    StringBuilder randomString = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int randomIndex = RANDOM.nextInt(CHARACTERS.length());
      randomString.append(CHARACTERS.charAt(randomIndex));
    }
    return randomString.toString();
  }

}
package com.agilecheckup.persistency.entity.question;

public class CustomizedValuesSplitter {

  private final static String VALUE_SEPARATOR = ",";

  public static String[] getSplitValues(String value) {
    return value.split(VALUE_SEPARATOR);
  }
}

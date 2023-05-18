package com.agilecheckup.persistency.entity.person;

public enum Gender {
  MALE("Male"),
  FEMALE("Female"),
  NON_BINARY("Non-Binary"),
  GENDERQUEER("Genderqueer"),
  TRANSGENDER("Transgender"),
  INTERSEX("Intersex"),
  OTHER("Other");

  private final String label;

  Gender(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}

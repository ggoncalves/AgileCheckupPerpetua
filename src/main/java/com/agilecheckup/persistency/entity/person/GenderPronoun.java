package com.agilecheckup.persistency.entity.person;

public enum GenderPronoun {
  HE("He"),
  SHE("She");

  private final String label;

  GenderPronoun(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}

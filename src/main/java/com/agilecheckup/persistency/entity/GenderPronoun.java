package com.agilecheckup.persistency.entity;

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

package com.agilecheckup.persistency.entity;

import lombok.Getter;

@Getter
public enum Industry {
  TECHNOLOGY("Technology"),
  FINANCE("Finance"),
  HEALTHCARE("Healthcare"),
  MANUFACTURING("Manufacturing"),
  RETAIL("Retail"),
  EDUCATION("Education"),
  CONSULTING("Consulting"),
  GOVERNMENT("Government"),
  NONPROFIT("Nonprofit"),
  OTHER("Other");

  private final String label;

  Industry(String label) {
    this.label = label;
  }
}
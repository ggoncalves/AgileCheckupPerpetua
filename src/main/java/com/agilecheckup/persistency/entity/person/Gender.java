package com.agilecheckup.persistency.entity.person;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Gender {
  MALE("Male"), FEMALE("Female"), NON_BINARY("Non-Binary"), GENDERQUEER("Genderqueer"), TRANSGENDER("Transgender"), INTERSEX("Intersex"), OTHER("Other");

  private final String label;

}

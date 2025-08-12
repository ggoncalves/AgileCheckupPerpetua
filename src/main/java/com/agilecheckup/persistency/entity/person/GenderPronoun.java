package com.agilecheckup.persistency.entity.person;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GenderPronoun {
  HE("He"), SHE("She");

  private final String label;
}

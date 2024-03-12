package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.question.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@Getter
public abstract class AbstractScoreCalculator {

  @NonNull
  final String value;

  @NonNull
  final Question question;

  public abstract Double getCalculatedScore();


}

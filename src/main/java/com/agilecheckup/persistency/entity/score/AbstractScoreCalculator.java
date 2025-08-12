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
  protected final String value;

  protected final Question question;

  public abstract Double getCalculatedScore();

  /**
   * Helper method to get points from question entity.
   */
  protected Double getQuestionPoints() {
    if (question != null) {
      return question.getPoints();
    }
    else {
      throw new IllegalStateException("question is null");
    }
  }

}

package com.agilecheckup.persistency.entity.score;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class IntegerIntervalScoreCalculationStrategy extends AbstractScoreCalculator {

  protected Double numberOfOptions;

  public Double getCalculatedScore() {
    return getPointsPerInterval() * getOption();
  }

  private Double getPointsPerInterval() {
    if (question != null) {
      return question.getPoints() / numberOfOptions;
    } else if (questionV2 != null) {
      return questionV2.getPoints() / numberOfOptions;
    } else {
      throw new IllegalStateException("Both question and questionV2 are null");
    }
  }

  private Integer getOption() {
    return Integer.parseInt(value);
  }

}

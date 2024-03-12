package com.agilecheckup.persistency.entity.score;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class IntegerIntervalScoreCalculationStrategy extends AbstractScoreCalculator {

  protected Double numberOfOptions;

  public Double getCalculatedScore() {
    return getPointsPerInterval() * getOption();
  }

  private Double getPointsPerInterval() {
    return question.getPoints() / numberOfOptions;
  }

  private Integer getOption() {
    return Integer.parseInt(value);
  }

}

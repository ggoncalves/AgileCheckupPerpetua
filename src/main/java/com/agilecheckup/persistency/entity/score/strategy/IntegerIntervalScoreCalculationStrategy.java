package com.agilecheckup.persistency.entity.score.strategy;

import com.agilecheckup.persistency.entity.score.AbstractScoreCalculator;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class IntegerIntervalScoreCalculationStrategy extends AbstractScoreCalculator {

  protected Double numberOfOptions;

  public Double getCalculatedScore() {
    return getPointsPerInterval() * getOption();
  }

  private Double getPointsPerInterval() {
    return getQuestionPoints() / numberOfOptions;
  }

  private Integer getOption() {
    return Integer.parseInt(value);
  }

}

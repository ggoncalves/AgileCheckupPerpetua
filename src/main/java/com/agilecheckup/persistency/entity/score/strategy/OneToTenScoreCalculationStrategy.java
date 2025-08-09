package com.agilecheckup.persistency.entity.score.strategy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class OneToTenScoreCalculationStrategy extends IntegerIntervalScoreCalculationStrategy {

  private static final Double NUMBER_OF_OPTIONS = 10d;

  {
    numberOfOptions = NUMBER_OF_OPTIONS;
  }

}

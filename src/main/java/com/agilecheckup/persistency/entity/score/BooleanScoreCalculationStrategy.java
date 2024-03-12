package com.agilecheckup.persistency.entity.score;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class BooleanScoreCalculationStrategy extends AbstractScoreCalculator {


  @Override
  public Double getCalculatedScore() {
    return ("true".equalsIgnoreCase(value)) ? question.getPoints() : 0d;
  }
}

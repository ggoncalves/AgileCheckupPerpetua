package com.agilecheckup.persistency.entity.score.strategy;

import com.agilecheckup.persistency.entity.score.AbstractScoreCalculator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class OpenAnswerScoreCalculationStrategy extends AbstractScoreCalculator {


  @Override
  public Double getCalculatedScore() {
    return 0d;
  }
}

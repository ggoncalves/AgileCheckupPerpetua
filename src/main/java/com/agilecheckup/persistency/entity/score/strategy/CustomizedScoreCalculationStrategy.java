package com.agilecheckup.persistency.entity.score.strategy;

import com.agilecheckup.persistency.entity.score.AbstractScoreCalculator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;

import static com.agilecheckup.persistency.entity.question.CustomizedValuesSplitter.getSplitValues;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class CustomizedScoreCalculationStrategy extends AbstractScoreCalculator {


  @Override
  public Double getCalculatedScore() {
    String[] stringValues = getSplitValues(value);
    return Arrays.stream(stringValues)
        .map(Integer::valueOf)
        .mapToDouble(value -> questionV2.getOptionGroup().getOptionMap().get(value).getPoints())
        .sum();
  }
}

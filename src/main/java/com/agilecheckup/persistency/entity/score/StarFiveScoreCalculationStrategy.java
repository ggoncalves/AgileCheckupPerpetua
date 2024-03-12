package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.question.Question;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class StarFiveScoreCalculationStrategy extends IntegerIntervalScoreCalculationStrategy {

  private static final Double NUMBER_OF_OPTIONS = 5d;

  {
    numberOfOptions = NUMBER_OF_OPTIONS;
  }

}

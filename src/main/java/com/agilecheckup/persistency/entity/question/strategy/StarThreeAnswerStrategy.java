package com.agilecheckup.persistency.entity.question.strategy;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class StarThreeAnswerStrategy extends AnswerIntegerIntervalStrategy {

  private static final Integer MIN_VALUE = 1;
  private static final Integer MAX_VALUE = 3;

  {
    minValue = MIN_VALUE;
    maxValue = MAX_VALUE;
  }


}

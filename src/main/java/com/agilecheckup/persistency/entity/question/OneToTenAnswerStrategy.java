package com.agilecheckup.persistency.entity.question;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class OneToTenAnswerStrategy extends AnswerIntegerIntervalStrategy {

  private static final Integer MIN_VALUE = 1;
  private static final Integer MAX_VALUE = 10;

  {
    minValue = MIN_VALUE;
    maxValue = MAX_VALUE;
  }


}

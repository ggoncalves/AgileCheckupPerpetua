package com.agilecheckup.persistency.entity.question.strategy;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class GoodBadAnswerStrategy extends AnswerBooleanStrategy {


  public boolean isGoodAnswer() {
    return (isNullValue()) ? false : getValue();
  }

  public boolean isBadAnswer() {
    return (isNullValue()) ? false : !getValue();
  }

}

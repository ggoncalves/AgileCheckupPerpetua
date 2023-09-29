package com.agilecheckup.persistency.entity.question;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class YesNoAnswerStrategy extends AnswerBooleanStrategy {


  public boolean isYesAnswer() {
    return (isNullValue()) ? false : getValue();
  }

  public boolean isNoAnswer() {
    return (isNullValue()) ? false : !getValue();
  }

}

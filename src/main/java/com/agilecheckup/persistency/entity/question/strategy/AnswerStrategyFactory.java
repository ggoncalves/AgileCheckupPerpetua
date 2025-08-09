package com.agilecheckup.persistency.entity.question.strategy;

import com.agilecheckup.persistency.entity.question.Question;

public class AnswerStrategyFactory {

  public static AnswerStrategy<?> createStrategy(Question question, boolean allowNullValue) {
    switch (question.getQuestionType()) {
      case STAR_THREE:
        return StarThreeAnswerStrategy.builder().questionV2(question).allowNullValue(allowNullValue).build();
      case STAR_FIVE:
        return StarFiveAnswerStrategy.builder().questionV2(question).allowNullValue(allowNullValue).build();
      case OPEN_ANSWER:
        return OpenAnswerStrategy.builder().questionV2(question).allowNullValue(allowNullValue).build();
      case GOOD_BAD:
        return GoodBadAnswerStrategy.builder().questionV2(question).allowNullValue(allowNullValue).build();
      case YES_NO:
        return YesNoAnswerStrategy.builder().questionV2(question).allowNullValue(allowNullValue).build();
      case ONE_TO_TEN:
        return OneToTenAnswerStrategy.builder().questionV2(question).allowNullValue(allowNullValue).build();
      case CUSTOMIZED:
        return CustomizedAnswerStrategy.builder().questionV2(question).allowNullValue(allowNullValue).build();
      default:
        throw new IllegalArgumentException("Invalid QuestionType. Please check if there is missing a Strategy");
    }
  }
}
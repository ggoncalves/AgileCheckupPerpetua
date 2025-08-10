package com.agilecheckup.persistency.entity.question.strategy;

import com.agilecheckup.persistency.entity.question.Question;

public class AnswerStrategyFactory {

  public static AnswerStrategy<?> createStrategy(Question question, boolean allowNullValue) {
    switch (question.getQuestionType()) {
      case STAR_THREE:
        return StarThreeAnswerStrategy.builder().question(question).allowNullValue(allowNullValue).build();
      case STAR_FIVE:
        return StarFiveAnswerStrategy.builder().question(question).allowNullValue(allowNullValue).build();
      case OPEN_ANSWER:
        return OpenAnswerStrategy.builder().question(question).allowNullValue(allowNullValue).build();
      case GOOD_BAD:
        return GoodBadAnswerStrategy.builder().question(question).allowNullValue(allowNullValue).build();
      case YES_NO:
        return YesNoAnswerStrategy.builder().question(question).allowNullValue(allowNullValue).build();
      case ONE_TO_TEN:
        return OneToTenAnswerStrategy.builder().question(question).allowNullValue(allowNullValue).build();
      case CUSTOMIZED:
        return CustomizedAnswerStrategy.builder().question(question).allowNullValue(allowNullValue).build();
      default:
        throw new IllegalArgumentException("Invalid QuestionType. Please check if there is missing a Strategy");
    }
  }
}
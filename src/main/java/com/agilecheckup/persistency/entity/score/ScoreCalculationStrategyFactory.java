package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.question.QuestionV2;

public class ScoreCalculationStrategyFactory {

  public static AbstractScoreCalculator createStrategy(QuestionV2 question, String value) {
    switch (question.getQuestionType()) {
      case STAR_THREE:
        return StarThreeScoreCalculationStrategy.builder().questionV2(question).value(value).build();
      case STAR_FIVE:
        return StarFiveScoreCalculationStrategy.builder().questionV2(question).value(value).build();
      case OPEN_ANSWER:
        return OpenAnswerScoreCalculationStrategy.builder().questionV2(question).value(value).build();
      case GOOD_BAD:
        return GoodBadScoreCalculationStrategy.builder().questionV2(question).value(value).build();
      case YES_NO:
        return YesNoScoreCalculationStrategy.builder().questionV2(question).value(value).build();
      case ONE_TO_TEN:
        return OneToTenScoreCalculationStrategy.builder().questionV2(question).value(value).build();
      case CUSTOMIZED:
        return CustomizedScoreCalculationStrategy.builder().questionV2(question).value(value).build();
      default:
        throw new IllegalArgumentException("Invalid QuestionType. Please check if there is missing a Strategy");
    }
  }
}

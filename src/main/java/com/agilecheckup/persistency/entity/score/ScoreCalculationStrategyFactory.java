package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.question.*;

public class ScoreCalculationStrategyFactory {

  public static AbstractScoreCalculator createStrategy(Question question, String value) {
    switch (question.getQuestionType()) {
      case STAR_THREE:
        return StarThreeScoreCalculationStrategy.builder().question(question).value(value).build();
      case STAR_FIVE:
        return StarFiveScoreCalculationStrategy.builder().question(question).value(value).build();
      case OPEN_ANSWER:
        return OpenAnswerScoreCalculationStrategy.builder().question(question).value(value).build();
      case GOOD_BAD:
        return GoodBadScoreCalculationStrategy.builder().question(question).value(value).build();
      case YES_NO:
        return YesNoScoreCalculationStrategy.builder().question(question).value(value).build();
      case ONE_TO_TEN:
        return OneToTenScoreCalculationStrategy.builder().question(question).value(value).build();
      case CUSTOMIZED:
        return CustomizedScoreCalculationStrategy.builder().question(question).value(value).build();
      default:
        throw new IllegalArgumentException("Invalid QuestionType. Please check if there is missing a Strategy");
    }
  }
}

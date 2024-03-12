package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import org.junit.jupiter.api.Test;

import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.*;

class GoodBadScoreCalculationStrategyTest {

  @Test
  void shouldReturn10WhenCalculate10PointsForTrue() {
    GoodBadScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("true",
        createMockedQuestion(10, QuestionType.GOOD_BAD));
    assertEquals(10d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn10WhenCalculate10PointsForTrueAnyCase() {
    GoodBadScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("TRUE",
        createMockedQuestion(10, QuestionType.GOOD_BAD));
    assertEquals(10d, scoreCalculationStrategy.getCalculatedScore(), 0d);

    scoreCalculationStrategy = scoreCalculationStrategyFor("True",
        createMockedQuestion(10, QuestionType.GOOD_BAD));
    assertEquals(10d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn0WhenCalculate10PointsForFalse() {
    GoodBadScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("false",
        createMockedQuestion(10, QuestionType.GOOD_BAD));
    assertEquals(0d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn10WhenCalculate10PointsForFalseAnyCase() {
    GoodBadScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("FALSE",
        createMockedQuestion(10, QuestionType.GOOD_BAD));
    assertEquals(0d, scoreCalculationStrategy.getCalculatedScore(), 0d);

    scoreCalculationStrategy = scoreCalculationStrategyFor("0",
        createMockedQuestion(10, QuestionType.GOOD_BAD));
    assertEquals(0d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  private GoodBadScoreCalculationStrategy scoreCalculationStrategyFor(String value, Question question) {
    return GoodBadScoreCalculationStrategy.builder()
        .value(value)
        .question(question)
        .build();
  }
}
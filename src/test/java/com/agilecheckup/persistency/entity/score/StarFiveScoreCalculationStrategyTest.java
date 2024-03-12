package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import org.junit.jupiter.api.Test;

import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StarFiveScoreCalculationStrategyTest {

  @Test
  void shouldReturn1WhenCalculate5PointsFor1Star() {
    StarFiveScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("1",
        createMockedQuestion(5d, QuestionType.STAR_FIVE));
    assertEquals(1d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn3WhenCalculate5PointsFor3Star() {
    StarFiveScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("3",
        createMockedQuestion(5d, QuestionType.STAR_FIVE));
    assertEquals(3d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn5WhenCalculate5PointsFor5Star() {
    StarFiveScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("5",
        createMockedQuestion(5d, QuestionType.STAR_FIVE));
    assertEquals(5d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn6WhenCalculate10PointsFor3Star() {
    StarFiveScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("3",
        createMockedQuestion(10d, QuestionType.STAR_FIVE));
    assertEquals(6d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn12_6WhenCalculate21PointsFor3Star() {
    StarFiveScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("3",
        createMockedQuestion(21d, QuestionType.STAR_FIVE));
    assertEquals(12.6d, scoreCalculationStrategy.getCalculatedScore(), 0.01d);
  }

  private StarFiveScoreCalculationStrategy scoreCalculationStrategyFor(String value, Question question) {
    return StarFiveScoreCalculationStrategy.builder()
        .value(value)
        .question(question)
        .build();
  }

}
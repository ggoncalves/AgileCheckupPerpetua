package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import org.junit.jupiter.api.Test;

import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StarThreeScoreCalculationStrategyTest {

  @Test
  void shouldReturn1WhenCalculate5PointsFor1Star() {
    StarThreeScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("1",
        createMockedQuestion(3d, QuestionType.STAR_THREE));
    assertEquals(1d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn2WhenCalculate3PointsFor2Star() {
    StarThreeScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("2",
        createMockedQuestion(3d, QuestionType.STAR_THREE));
    assertEquals(2d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn3WhenCalculate3PointsFor3Star() {
    StarThreeScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("3",
        createMockedQuestion(3d, QuestionType.STAR_THREE));
    assertEquals(3d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn10WhenCalculate10PointsFor3Star() {
    StarThreeScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("3",
        createMockedQuestion(10d, QuestionType.STAR_THREE));
    assertEquals(10d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn38WhenCalculate57PointsFor2Star() {
    StarThreeScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("2",
        createMockedQuestion(57d, QuestionType.STAR_THREE));
    assertEquals(38d, scoreCalculationStrategy.getCalculatedScore(), 0.01d);
  }

  private StarThreeScoreCalculationStrategy scoreCalculationStrategyFor(String value, Question question) {
    return StarThreeScoreCalculationStrategy.builder()
        .value(value)
        .question(question)
        .build();
  }

}
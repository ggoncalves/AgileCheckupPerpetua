package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import org.junit.jupiter.api.Test;

import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OneToTenScoreCalculationStrategyTest {

  @Test
  void shouldReturn1WhenCalculate10PointsForValue1() {
    OneToTenScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("1",
        createMockedQuestion(10d, QuestionType.ONE_TO_TEN));
    assertEquals(1d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn5WhenCalculate10PointsForValue5() {
    OneToTenScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("5",
        createMockedQuestion(10d, QuestionType.ONE_TO_TEN));
    assertEquals(5d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn10WhenCalculate10PointsForValue10() {
    OneToTenScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("10",
        createMockedQuestion(10d, QuestionType.ONE_TO_TEN));
    assertEquals(10d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn35WhenCalculate50PointsForValue7() {
    OneToTenScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("7",
        createMockedQuestion(50d, QuestionType.ONE_TO_TEN));
    assertEquals(35d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  private OneToTenScoreCalculationStrategy scoreCalculationStrategyFor(String value, Question question) {
    return OneToTenScoreCalculationStrategy.builder()
        .value(value)
        .question(question)
        .build();
  }

}
package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import org.junit.jupiter.api.Test;

import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.assertEquals;

class YesNoScoreCalculationStrategyTest {

  @Test
  void shouldReturn10WhenCalculate10PointsForTrue() {
    YesNoScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("true",
        createMockedQuestion(10d, QuestionType.YES_NO));
    assertEquals(10d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn10WhenCalculate10PointsForTrueAnyCase() {
    YesNoScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("TRUE",
        createMockedQuestion(10d, QuestionType.YES_NO));
    assertEquals(10d, scoreCalculationStrategy.getCalculatedScore(), 0d);

    scoreCalculationStrategy = scoreCalculationStrategyFor("True",
        createMockedQuestion(10d, QuestionType.YES_NO));
    assertEquals(10d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn0WhenCalculate10PointsForFalse() {
    YesNoScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("false",
        createMockedQuestion(10d, QuestionType.YES_NO));
    assertEquals(0d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturn10WhenCalculate10PointsForFalseAnyCase() {
    YesNoScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("FALSE",
        createMockedQuestion(10d, QuestionType.YES_NO));
    assertEquals(0d, scoreCalculationStrategy.getCalculatedScore(), 0d);

    scoreCalculationStrategy = scoreCalculationStrategyFor("0",
        createMockedQuestion(10d, QuestionType.YES_NO));
    assertEquals(0d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  private YesNoScoreCalculationStrategy scoreCalculationStrategyFor(String value, Question question) {
    return YesNoScoreCalculationStrategy.builder()
        .value(value)
        .question(question)
        .build();
  }
}
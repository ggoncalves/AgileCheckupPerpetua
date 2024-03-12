package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.question.Question;
import org.junit.jupiter.api.Test;

import static com.agilecheckup.util.TestObjectFactory.GENERIC_ID_1234;
import static com.agilecheckup.util.TestObjectFactory.createMockedCustomQuestion;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomizedScoreCalculationStrategyTest {

  @Test
  void shouldReturnValueForSingleOption() {
    Question singleChoiceQuestion = createCustomQuestion(false, 0d, 5d, 10d, 15d, 20d);
    AbstractScoreCalculator scoreCalculationStrategy = scoreCalculationStrategyFor("1", singleChoiceQuestion);
    assertEquals(0d, scoreCalculationStrategy.getCalculatedScore(), 0d);
    scoreCalculationStrategy = scoreCalculationStrategyFor("3", singleChoiceQuestion);
    assertEquals(10d, scoreCalculationStrategy.getCalculatedScore(), 0d);
    scoreCalculationStrategy = scoreCalculationStrategyFor("5", singleChoiceQuestion);
    assertEquals(20d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  @Test
  void shouldReturnSumOfValuesForMultipleOption() {
    Question singleChoiceQuestion = createCustomQuestion(true, 0d, 5d, 10d, 15d, 20d);
    AbstractScoreCalculator scoreCalculationStrategy = scoreCalculationStrategyFor("1,2", singleChoiceQuestion);
    assertEquals(5d, scoreCalculationStrategy.getCalculatedScore(), 0d);
    scoreCalculationStrategy = scoreCalculationStrategyFor("2,3", singleChoiceQuestion);
    assertEquals(15d, scoreCalculationStrategy.getCalculatedScore(), 0d);
    scoreCalculationStrategy = scoreCalculationStrategyFor("1,2,3,5", singleChoiceQuestion);
    assertEquals(35d, scoreCalculationStrategy.getCalculatedScore(), 0d);
    scoreCalculationStrategy = scoreCalculationStrategyFor("1,2,3,4,5", singleChoiceQuestion);
    assertEquals(50d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }

  private Question createCustomQuestion(boolean isMultipleChoice, Double ... points) {
    return createMockedCustomQuestion(GENERIC_ID_1234, isMultipleChoice, points);
  }

  private AbstractScoreCalculator scoreCalculationStrategyFor(String value, Question question) {
    return CustomizedScoreCalculationStrategy.builder()
        .value(value)
        .question(question)
        .build();
  }

}
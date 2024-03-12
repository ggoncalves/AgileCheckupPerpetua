package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import org.junit.jupiter.api.Test;

import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenAnswerScoreCalculationStrategyTest {

  @Test
  void shouldReturn0Always() {
    OpenAnswerScoreCalculationStrategy scoreCalculationStrategy = scoreCalculationStrategyFor("1",
        createMockedQuestion(10, QuestionType.OPEN_ANSWER));
    assertEquals(0d, scoreCalculationStrategy.getCalculatedScore(), 0d);

    scoreCalculationStrategy = scoreCalculationStrategyFor("1",
        createMockedQuestion(100, QuestionType.OPEN_ANSWER));
    assertEquals(0d, scoreCalculationStrategy.getCalculatedScore(), 0d);
  }


  private OpenAnswerScoreCalculationStrategy scoreCalculationStrategyFor(String value, Question question) {
    return OpenAnswerScoreCalculationStrategy.builder()
        .value(value)
        .question(question)
        .build();
  }

}
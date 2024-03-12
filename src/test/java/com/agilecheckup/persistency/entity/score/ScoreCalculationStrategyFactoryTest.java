package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ScoreCalculationStrategyFactoryTest {

  private final Question question = createMockedQuestion();

  private final Map<QuestionType, Class<? extends AbstractScoreCalculator>> questionTypeToClassMap = createUnmodifiableMap(
      new ImmutablePair<>(QuestionType.STAR_THREE, StarThreeScoreCalculationStrategy.class),
      new ImmutablePair<>(QuestionType.STAR_FIVE, StarFiveScoreCalculationStrategy.class),
      new ImmutablePair<>(QuestionType.OPEN_ANSWER, OpenAnswerScoreCalculationStrategy.class),
      new ImmutablePair<>(QuestionType.GOOD_BAD, GoodBadScoreCalculationStrategy.class),
      new ImmutablePair<>(QuestionType.YES_NO, YesNoScoreCalculationStrategy.class),
      new ImmutablePair<>(QuestionType.ONE_TO_TEN, OneToTenScoreCalculationStrategy.class),
      new ImmutablePair<>(QuestionType.CUSTOMIZED, CustomizedScoreCalculationStrategy.class)
  );

  @Test
  void shouldThisTestMapAllEnumValues() {
    assertEquals(QuestionType.values().length, questionTypeToClassMap.size());
  }

  @Test
  void shouldCreateCorrectStrategyForAllMappings() {
    questionTypeToClassMap.keySet().forEach(questionType -> {
      Stream.of("1", "2").forEach(value -> validateStrategyForQuestionType(questionType, value));
    });
  }

  private void validateStrategyForQuestionType(QuestionType questionType, String value) {
    question.setQuestionType(questionType);
    AbstractScoreCalculator scoreCalculator = ScoreCalculationStrategyFactory.createStrategy(question, value);

    assertStrategyTypeMatchesExpected(questionType, scoreCalculator);
    assertStrategyQuestionMatchesExpected(scoreCalculator);
    assertValueMatchesExpected(value, scoreCalculator);
  }

  private void assertValueMatchesExpected(String value, AbstractScoreCalculator scoreCalculator) {
    assertEquals(value, scoreCalculator.getValue());
  }

  private void assertStrategyTypeMatchesExpected(QuestionType questionType, AbstractScoreCalculator scoreCalculator) {
    assertInstanceOf(questionTypeToClassMap.get(questionType), scoreCalculator);
  }

  private void assertStrategyQuestionMatchesExpected(AbstractScoreCalculator scoreCalculator) {
    assertEquals(question, scoreCalculator.getQuestion());
  }

  @SafeVarargs
  public static <K, V> Map<K, V> createUnmodifiableMap(Pair<K, V>... pairs) {
    return Stream.of(pairs)
        .collect(Collectors.collectingAndThen(
            Collectors.toMap(Pair::getKey, Pair::getValue),
            Collections::unmodifiableMap));
  }

}
package com.agilecheckup.persistency.entity.question;

import com.agilecheckup.persistency.entity.QuestionType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.agilecheckup.util.TestObjectFactory.createMockedQuestion;
import static org.junit.jupiter.api.Assertions.*;

class AnswerStrategyFactoryTest {

  private Question question = createMockedQuestion();

  private Map<QuestionType, Class> questionTypeToClassMap = createUnmodifiableMap(
      new ImmutablePair<>(QuestionType.STAR_THREE, StarThreeAnswerStrategy.class),
      new ImmutablePair<>(QuestionType.STAR_FIVE, StarFiveAnswerStrategy.class),
      new ImmutablePair<>(QuestionType.OPEN_ANSWER, OpenAnswerStrategy.class),
      new ImmutablePair<>(QuestionType.GOOD_BAD, GoodBadAnswerStrategy.class),
      new ImmutablePair<>(QuestionType.YES_NO, YesNoAnswerStrategy.class),
      new ImmutablePair<>(QuestionType.ONE_TO_TEN, OneToTenAnswerStrategy.class),
      new ImmutablePair<>(QuestionType.CUSTOMIZED, CustomizedAnswerStrategy.class)
  );

  @Test
  void shouldThisTestMapAllEnumValues() {
    assertEquals(QuestionType.values().length, questionTypeToClassMap.size());
  }

  @Test
  void shouldCreateCorrectStrategyForAllMappings() {
    questionTypeToClassMap.keySet().forEach(questionType -> {
      Stream.of(true, false).forEach(allowNullValue -> validateStrategyForQuestionType(questionType, allowNullValue));
    });
  }

  private void validateStrategyForQuestionType(QuestionType questionType, boolean allowNullValue) {
    question.setQuestionType(questionType);
    AnswerStrategy answerStrategy = AnswerStrategyFactory.createStrategy(question, allowNullValue);

    assertStrategyTypeMatchesExpected(questionType, answerStrategy);
    assertStrategyQuestionMatchesExpected(answerStrategy);
    assertAllowNullValueMatchesExpected(allowNullValue, answerStrategy);
  }

  private void assertAllowNullValueMatchesExpected(boolean allowNullValue, AnswerStrategy answerStrategy) {
    assertEquals(allowNullValue, answerStrategy.isAllowNullValue());
  }

  private void assertStrategyTypeMatchesExpected(QuestionType questionType, AnswerStrategy answerStrategy) {
    assertInstanceOf(questionTypeToClassMap.get(questionType), answerStrategy);
  }

  private void assertStrategyQuestionMatchesExpected(AnswerStrategy answerStrategy) {
    assertEquals(question, answerStrategy.getQuestion());
  }

  @SafeVarargs
  public static <K, V> Map<K, V> createUnmodifiableMap(Pair<K, V>... pairs) {
    return Stream.of(pairs)
        .collect(Collectors.collectingAndThen(
            Collectors.toMap(Pair::getKey, Pair::getValue),
            Collections::unmodifiableMap));
  }
}
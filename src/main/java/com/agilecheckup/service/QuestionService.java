package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.OptionGroup;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.QuestionRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class QuestionService extends AbstractCrudService<Question, AbstractCrudRepository<Question>> {

  private QuestionRepository questionRepository;

  @Inject
  public QuestionService(QuestionRepository questionRepository) {
    this.questionRepository = questionRepository;
  }

  public Optional<Question> create(String questionTxt, QuestionType questionType, String tenantId, Integer points) {
    return super.create(internalCreateQuestion(questionTxt, questionType, tenantId, points));
  }

  public Optional<Question> createCustomQuestion(String questionTxt, QuestionType questionType, String tenantId, boolean isMultipleChoice, boolean showFlushed, List<QuestionOption> options) {
    return super.create(internalCreateCustomQuestion(questionTxt, questionType, tenantId, isMultipleChoice, showFlushed, options));
  }

  private Question internalCreateQuestion(String questionTxt, QuestionType questionType, String tenantId, Integer points) {
    Question question = Question.builder()
        .question(questionTxt)
        .questionType(questionType)
        .tenantId(tenantId)
        .points(points)
        .build();
    return setFixedIdIfConfigured(question);
  }

  private Question internalCreateCustomQuestion(String questionTxt, QuestionType questionType, String tenantId, boolean isMultipleChoice, boolean showFlushed, List<QuestionOption> options) {
    Question question = Question.builder()
        .question(questionTxt)
        .questionType(questionType)
        .optionGroup(createOptionGroup(isMultipleChoice, showFlushed, options))
        .tenantId(tenantId)
        .build();
    return setFixedIdIfConfigured(question);
  }

  private OptionGroup createOptionGroup(boolean isMultipleChoice, boolean showFlushed, List<QuestionOption> options) {
    OptionGroup optionGroup = OptionGroup.builder()
        .isMultipleChoice(isMultipleChoice)
        .showFlushed(showFlushed)
        .options(options)
        .build();
    return optionGroup;
  }


  @Override
  AbstractCrudRepository<Question> getRepository() {
    return questionRepository;
  }
}

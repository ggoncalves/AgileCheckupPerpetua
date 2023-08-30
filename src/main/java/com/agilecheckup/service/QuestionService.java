package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.RateType;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.QuestionRepository;

import javax.inject.Inject;
import java.util.Optional;

public class QuestionService extends AbstractCrudService<Question, AbstractCrudRepository<Question>> {

  private QuestionRepository questionRepository;

  @Inject
  public QuestionService(QuestionRepository questionRepository) {
    this.questionRepository = questionRepository;
  }

  public Optional<Question> create(String questionTxt, RateType rateType, String tenantId, Integer points) {
    return super.create(createQuestion(questionTxt, rateType, tenantId, points));
  }

  private Question createQuestion(String questionTxt, RateType rateType, String tenantId, Integer points) {
    Question question = Question.builder()
        .question(questionTxt)
        .rateType(rateType)
        .tenantId(tenantId)
        .points(points)
        .build();
    return setFixedIdIfConfigured(question);
  }


  @Override
  AbstractCrudRepository<Question> getRepository() {
    return questionRepository;
  }
}

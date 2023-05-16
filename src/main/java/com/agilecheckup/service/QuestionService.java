package com.agilecheckup.service;

import com.agilecheckup.dagger.component.DaggerRepositoryComponent;
import com.agilecheckup.dagger.component.RepositoryComponent;
import com.agilecheckup.persistency.entity.Question;
import com.agilecheckup.persistency.entity.RateType;
import com.agilecheckup.persistency.repository.CrudRepository;
import com.agilecheckup.persistency.repository.QuestionRepository;

import javax.inject.Inject;
import java.util.Optional;

public class QuestionService extends AbstractCrudService<Question, CrudRepository<Question>> {

  private QuestionRepository questionRepository;

  @Inject
  public QuestionService() {
  }

  public Optional<Question> create(String questionTxt, RateType rateType, String tenantId, Integer points) {
    return super.create(Question.builder()
        .question(questionTxt)
        .rateType(rateType)
        .tenantId(tenantId)
        .points(points)
        .build());
  }

//  private ImpactLevel getImpactLevelById(String impactLevelId) {
//    return getById(impactLevelId, impactLevelService, "impactLevelId");
//  }
//
//  private Principle getPrincipleById(String principleId) {
//    return getById(principleId, principleService, "principalId");
//  }

//s


  @Override
  CrudRepository<Question> getRepository() {
    if (questionRepository == null) {
      RepositoryComponent repositoryComponent = DaggerRepositoryComponent.create();
      questionRepository = repositoryComponent.buildQuestionRepository();
    }
    return questionRepository;
  }
}

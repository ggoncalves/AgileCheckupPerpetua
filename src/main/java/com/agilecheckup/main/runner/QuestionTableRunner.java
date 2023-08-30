package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.RateType;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.QuestionService;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
 public class QuestionTableRunner extends AbstractEntityCrudRunner<Question> {

  private QuestionService questionService;

  public QuestionTableRunner(boolean mustDelete) {
    super(mustDelete);
  }

  @Override
  protected Collection<Supplier<Optional<Question>>> getCreateSupplier() {
    Collection<Supplier<Optional<Question>>> collection = new ArrayList<>();
    collection.add(() -> getQuestionService().create("Pergunta oficial", RateType.STAR_THREE, "OrinnovaSuper",
        15
    ));
    return collection;
  }

  @Override
  protected AbstractCrudService<Question, AbstractCrudRepository<Question>> getCrudService() {
    return getQuestionService();
  }

  @Override
  protected void verifySavedEntity(Question savedEntity, Question fetchedEntity) {
    log.info("Is RateType Equals? " + fetchedEntity.getRateType().equals(RateType.OPEN_ANSWER));
  }

  private QuestionService getQuestionService() {
    if (questionService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      questionService = serviceComponent.buildQuestionService();
    }
    return questionService;
  }
}

package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.AnswerService;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
 public class AnswerTableRunner extends AbstractEntityCrudRunner<Answer> {

  private AnswerService answerService;

  public AnswerTableRunner(boolean shouldCleanAfterComplete) {
    super(shouldCleanAfterComplete);
  }

  @Override
  protected Collection<Supplier<Optional<Answer>>> getCreateSupplier() {
    Collection<Supplier<Optional<Answer>>> collection = new ArrayList<>();
    // Keeping in mind that the id references might be wrong. E.g.: tenantId might differ from Question and
    // AssessmentMatrix at this time.
    collection.add(() -> getAnswerService().create("d05ca8e3-b163-42cb-8cd6-96ca65c510bc",
        "b14de828-0735-4122-98a2-317794a89f9a", LocalDateTime.now(), "3",
        "OrinnovaSuper"));
    collection.add(() -> getAnswerService().create("d05ca8e3-b163-42cb-8cd6-96ca65c510bc",
        "26759c9b-4bc8-4ffa-a262-2f5e25a07395", LocalDateTime.now(), "3",
        "OrinnovaSuper"));
    return collection;
  }

  @Override
  protected AbstractCrudService<Answer, AbstractCrudRepository<Answer>> getCrudService() {
    return getAnswerService();
  }

  @Override
  protected void verifySavedEntity(Answer savedEntity, Answer fetchedEntity) {
    switch (savedEntity.getQuestionType()) {
      case CUSTOMIZED:
        assert savedEntity.getQuestionType().equals(QuestionType.CUSTOMIZED);
        assert savedEntity.getQuestionId().equals("26759c9b-4bc8-4ffa-a262-2f5e25a07395");
        assert savedEntity.getValue().equals("3");
        break;
      case STAR_THREE:
        assert savedEntity.getQuestionType().equals(QuestionType.STAR_THREE);
        assert savedEntity.getQuestionId().equals("b14de828-0735-4122-98a2-317794a89f9a");
        assert savedEntity.getValue().equals("3");
        break;
    }
  }

  private AnswerService getAnswerService() {
    if (answerService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      answerService = serviceComponent.buildAnswerService();
    }
    return answerService;
  }
}

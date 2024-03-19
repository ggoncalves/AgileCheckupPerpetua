package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
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

    // STAR_THREE
    collection.add(() -> getAnswerService().create("ea045413-a5dd-46d6-9b14-993d699c2296",
        "b14de828-0735-4122-98a2-317794a89f9a", LocalDateTime.now(), "3",
        "OrinnovaSuper", ""));

    // CUSTOMIZED
    collection.add(() -> getAnswerService().create("ea045413-a5dd-46d6-9b14-993d699c2296",
        "26759c9b-4bc8-4ffa-a262-2f5e25a07395", LocalDateTime.now(), "3",
        "OrinnovaSuper", "notes"));

    // STAR_FIVE
    collection.add(() -> getAnswerService().create("ea045413-a5dd-46d6-9b14-993d699c2296",
        "504a194d-97a8-40dc-8031-433a9d4f90fb", LocalDateTime.now(), "5",
        "OrinnovaSuper", ""));

    // ONE_TO_TEN
    collection.add(() -> getAnswerService().create("ea045413-a5dd-46d6-9b14-993d699c2296",
        "32d3f672-b6ad-42d8-9afc-283772545b11", LocalDateTime.now(), "5",
        "OrinnovaSuper", "observações"));

    // YES_NO
    collection.add(() -> getAnswerService().create("ea045413-a5dd-46d6-9b14-993d699c2296",
        "d885ecac-7d0a-42f8-a74a-410c1286c082", LocalDateTime.now(), "false",
        "OrinnovaSuper", ""));

    // GOOD_BAD
    collection.add(() -> getAnswerService().create("ea045413-a5dd-46d6-9b14-993d699c2296",
        "f75e42b2-7684-4bab-a475-de35b754a264", LocalDateTime.now(), "true",
        "OrinnovaSuper", null));

    // OPEN_ANSWER
    collection.add(() -> getAnswerService().create("ea045413-a5dd-46d6-9b14-993d699c2296",
        "c6a5bb3a-58f3-47d6-8004-2f8dc1300a57", LocalDateTime.now(), "This is an open answer for an open question",
        "OrinnovaSuper", null));


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
        assert savedEntity.getQuestionId().equals("26759c9b-4bc8-4ffa-a262-2f5e25a07395");
        assert savedEntity.getValue().equals("3");
        assert savedEntity.getNotes().equals("notes");
        break;
      case STAR_THREE:
        assert savedEntity.getQuestionId().equals("b14de828-0735-4122-98a2-317794a89f9a");
        assert savedEntity.getValue().equals("3");
        assert savedEntity.getNotes().isEmpty();
        break;
      case GOOD_BAD:
        assert savedEntity.getQuestionId().equals("f75e42b2-7684-4bab-a475-de35b754a264");
        assert savedEntity.getNotes() == null;
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

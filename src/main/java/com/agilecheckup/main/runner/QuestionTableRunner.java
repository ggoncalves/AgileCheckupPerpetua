package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.QuestionType;
import com.agilecheckup.persistency.entity.question.Question;
import com.agilecheckup.persistency.entity.question.QuestionOption;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.QuestionService;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
 public class QuestionTableRunner extends AbstractEntityCrudRunner<Question> {

  private QuestionService questionService;

  public QuestionTableRunner(boolean mustDelete) {
    super(mustDelete);
  }

  @Override
  protected Collection<Supplier<Optional<Question>>> getCreateSupplier() {
    Collection<Supplier<Optional<Question>>> collection = new ArrayList<>();
    collection.add(() -> getQuestionService().create("Pergunta oficial", QuestionType.STAR_THREE, "OrinnovaSuper",
        15,
        "6413d36e-8716-4f97-ae87-7b4e9c2845ce", "1449ea3b-39b1-466c-bb54-f14ce984320f", "078ed4c3-abab-40c2-a237-e352b5172ee2"));
    collection.add(() -> getQuestionService().createCustomQuestion("Pergunta custom oficial", QuestionType.CUSTOMIZED, "OrinnovaSuper",
        false, true, createMockedQuestionOptionList("OptionPrefix", 0, 5, 10, 20, 30),
        "6413d36e-8716-4f97-ae87-7b4e9c2845ce", "39820ef9-7944-44d6-9c7e-8b5586fc0cb2", "60eb412a-00af-43cb-9f15-3308b7ff8a4c"));
    return collection;
  }

  @Override
  protected AbstractCrudService<Question, AbstractCrudRepository<Question>> getCrudService() {
    return getQuestionService();
  }

  @Override
  protected void verifySavedEntity(Question savedEntity, Question fetchedEntity) {
    switch (savedEntity.getQuestionType()) {
      case CUSTOMIZED:
        verifySavedCustomized(savedEntity);
        break;
      case STAR_THREE:
        verifyStarThree(savedEntity);
    }
  }

  private void verifySavedCustomized(Question savedEntity) {
    assert savedEntity.getQuestionType().equals(QuestionType.CUSTOMIZED);
    assert savedEntity.getOptionGroup().isMultipleChoice() == false;
    assert savedEntity.getOptionGroup().isShowFlushed() == true;
    assert savedEntity.getOptionGroup().getOptions().size() == 5;
    assert savedEntity.getOptionGroup().getOptions().get(0).getPoints().equals(Integer.valueOf(0));
    assert savedEntity.getOptionGroup().getOptions().get(0).getText().equals("OptionPrefix1");
    assert savedEntity.getOptionGroup().getOptions().get(1).getPoints().equals(Integer.valueOf(5));
    assert savedEntity.getOptionGroup().getOptions().get(1).getText().equals("OptionPrefix2");
    assert savedEntity.getOptionGroup().getOptions().get(2).getPoints().equals(Integer.valueOf(10));
    assert savedEntity.getOptionGroup().getOptions().get(2).getText().equals("OptionPrefix3");
    assert savedEntity.getOptionGroup().getOptions().get(3).getPoints().equals(Integer.valueOf(20));
    assert savedEntity.getOptionGroup().getOptions().get(3).getText().equals("OptionPrefix4");
    assert savedEntity.getOptionGroup().getOptions().get(4).getPoints().equals(Integer.valueOf(30));
    assert savedEntity.getOptionGroup().getOptions().get(4).getText().equals("OptionPrefix5");

  }

  private void verifyStarThree(Question savedEntity) {
    assert savedEntity.getQuestionType().equals(QuestionType.STAR_THREE);
  }

  public List<QuestionOption> createMockedQuestionOptionList(String prefix, Integer... points) {
    return IntStream.range(0, points.length)
        .mapToObj(index -> createQuestionOption(index, prefix, points[index]))
        .collect(Collectors.toList());
  }

  private QuestionOption createQuestionOption(Integer id, String prefix, int points) {
    return QuestionOption.builder()
        .id(++id)
        .text(prefix + "" + id)
        .points(points)
        .build();
  }

  private QuestionService getQuestionService() {
    if (questionService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      questionService = serviceComponent.buildQuestionService();
    }
    return questionService;
  }
}

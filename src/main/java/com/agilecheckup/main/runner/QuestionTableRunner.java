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

  // Using assessmentMatrixId "6413d36e-8716-4f97-ae87-7b4e9c2845ce"
  String assessmentMatrixId1 = "6413d36e-8716-4f97-ae87-7b4e9c2845ce";
  String pillarId1 = "1449ea3b-39b1-466c-bb54-f14ce984320f";
  String categoryId1 = "078ed4c3-abab-40c2-a237-e352b5172ee2";

  // Using assessmentMatrixId "6413d36e-8716-4f97-ae87-7b4e9c2845ce"
  String assessmentMatrixId2 = "6413d36e-8716-4f97-ae87-7b4e9c2845ce";
  String pillarId2 = "39820ef9-7944-44d6-9c7e-8b5586fc0cb2";
  String categoryId2 = "60eb412a-00af-43cb-9f15-3308b7ff8a4c";

  private QuestionService questionService;

  public QuestionTableRunner(boolean shouldCleanAfterComplete) {
    super(shouldCleanAfterComplete);
  }


  // Define the question types
  QuestionType[] questionTypes = QuestionType.values();


  @Override
  protected Collection<Supplier<Optional<Question>>> getCreateSupplier() {
    Collection<Supplier<Optional<Question>>> collection = new ArrayList<>();
    collection.add(() -> getQuestionService().create("Pergunta oficial", QuestionType.STAR_THREE, "OrinnovaSuper",
        15d,
        "6413d36e-8716-4f97-ae87-7b4e9c2845ce", "1449ea3b-39b1-466c-bb54-f14ce984320f", "078ed4c3-abab-40c2-a237-e352b5172ee2"));
    collection.add(() -> getQuestionService().createCustomQuestion("Pergunta custom oficial", QuestionType.CUSTOMIZED, "OrinnovaSuper",
        false, true, createMockedQuestionOptionList("OptionPrefix", 0, 5, 10, 20, 30),
        "6413d36e-8716-4f97-ae87-7b4e9c2845ce", "39820ef9-7944-44d6-9c7e-8b5586fc0cb2", "60eb412a-00af-43cb-9f15-3308b7ff8a4c"));

    collection.add(() -> getQuestionService().createCustomQuestion("Pergunta custom oficial 2",
        QuestionType.CUSTOMIZED, "OrinnovaSuper",
        false, true, createMockedQuestionOptionList("OptionPrefix", 0, 5, 10, 20, 30),
        assessmentMatrixId2, pillarId2, categoryId2));

//    // Iterate over the question types
//    for (QuestionType questionType : questionTypes) {
//      if (questionType.equals(QuestionType.CUSTOMIZED)) continue;
//      // Create two questions for each question type
//      for (int i = 1; i <= 2; i++) {
//        String questionDescription = "Pergunta " + questionType.toString().toLowerCase()
//            + " " + i;
//
//        collection.add(() -> getQuestionService().create(questionDescription, questionType, "OrinnovaSuper",
//            15,
//            assessmentMatrixId1, pillarId1, categoryId1));
//
//        collection.add(() -> getQuestionService().create(questionDescription, questionType, "OrinnovaSuper",
//            15,
//            assessmentMatrixId2, pillarId2, categoryId2));
//      }
//    }

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
    assert savedEntity.getOptionGroup().getOptionMap().size() == 5;
    assert savedEntity.getOptionGroup().getOptionMap().get(1).getPoints().equals(Double.valueOf(0));
    assert savedEntity.getOptionGroup().getOptionMap().get(1).getText().equals("OptionPrefix1");
    assert savedEntity.getOptionGroup().getOptionMap().get(2).getPoints().equals(Double.valueOf(5));
    assert savedEntity.getOptionGroup().getOptionMap().get(2).getText().equals("OptionPrefix2");
    assert savedEntity.getOptionGroup().getOptionMap().get(3).getPoints().equals(Double.valueOf(10));
    assert savedEntity.getOptionGroup().getOptionMap().get(3).getText().equals("OptionPrefix3");
    assert savedEntity.getOptionGroup().getOptionMap().get(4).getPoints().equals(Double.valueOf(20));
    assert savedEntity.getOptionGroup().getOptionMap().get(4).getText().equals("OptionPrefix4");
    assert savedEntity.getOptionGroup().getOptionMap().get(5).getPoints().equals(Double.valueOf(30));
    assert savedEntity.getOptionGroup().getOptionMap().get(5).getText().equals("OptionPrefix5");

  }

  private void verifyStarThree(Question savedEntity) {
    assert savedEntity.getQuestionType().equals(QuestionType.STAR_THREE);
  }

  public List<QuestionOption> createMockedQuestionOptionList(String prefix, Integer... points) {
    return IntStream.range(0, points.length)
        .mapToObj(index -> createQuestionOption(index, prefix, points[index]))
        .collect(Collectors.toList());
  }

  private QuestionOption createQuestionOption(Integer id, String prefix, double points) {
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

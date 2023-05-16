package com.agilecheckup.main;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.Question;
import com.agilecheckup.persistency.entity.RateType;
import com.agilecheckup.service.QuestionService;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

import java.util.Optional;

public class PerpetuaMain {

  private static QuestionService questionService;

  public static void main(String[] args) {
    System.out.println("Perpetua ran");
    createQuestionTest();
    System.exit(1);
  }

  private static QuestionService getQuestionService() {
    if (questionService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      questionService = serviceComponent.buildQuestionService();
    }
    return questionService;
  }

  private static void createQuestionTest() {


    Optional<Question> question = getQuestionService().create("Pergunta oficial", RateType.STAR_THREE, "OrinnovaSuper",
         15);
    PaginatedScanList<Question> list = getQuestionService().findAll();
    getQuestionService().fetchAndCompare(question.get().getId(), question.get());
    list.forEach(System.out::println);
    testFetchQuestion(question.get().getId(), question.get());
    System.out.println("Removendo: " + question.get().getId());
    getQuestionService().delete(question.get());
  }

  private static void testFetchQuestion(String id, Question question) {
    System.out.println("Fetching: " + id);
    Question fetchedQuestion = getQuestionService().findById(id).get();
    System.out.println("IsEquals = " + question.equals(fetchedQuestion));
    System.out.println("Is Date Okay? " + question.getCreatedDate() + " and " + question.getLastUpdatedDate());
    System.out.println("Is RateType Equals? " + question.getRateType().equals(RateType.OPEN_ANSWER));
  }
}

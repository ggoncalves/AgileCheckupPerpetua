package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.Question;

public class QuestionRepository extends CrudRepository<Question> {

  public QuestionRepository() {
    super(Question.class);
  }

}

package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.Question;

import javax.inject.Inject;

public class QuestionRepository extends CrudRepository<Question> {

  @Inject
  public QuestionRepository() {
    super(Question.class);
  }

}

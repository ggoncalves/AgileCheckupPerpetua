package com.agilecheckup.dagger.module;

import com.agilecheckup.persistency.repository.CrudRepository;
import com.agilecheckup.persistency.repository.QuestionRepository;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class RepositoryModule {

  @Binds
  abstract CrudRepository provideQuestionRepository(QuestionRepository questionRepository);
}

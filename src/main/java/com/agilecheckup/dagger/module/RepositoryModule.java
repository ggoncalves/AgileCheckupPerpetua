package com.agilecheckup.dagger.module;

import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.QuestionRepository;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class RepositoryModule {

  @Binds
  abstract AbstractCrudRepository provideQuestionRepository(QuestionRepository questionRepository);
}

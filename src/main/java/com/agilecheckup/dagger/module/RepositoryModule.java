package com.agilecheckup.dagger.module;

import com.agilecheckup.persistency.repository.QuestionRepository;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class RepositoryModule {

  @Provides
  @Singleton
  public QuestionRepository provideQuestionRepository() {
    return new QuestionRepository();
  }
}

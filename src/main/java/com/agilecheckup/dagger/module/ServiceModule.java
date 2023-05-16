package com.agilecheckup.dagger.module;

import com.agilecheckup.dagger.component.DaggerRepositoryComponent;
import com.agilecheckup.service.QuestionService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class ServiceModule {

  @Provides
  @Singleton
  public QuestionService provideQuestionService() {
    return new QuestionService(DaggerRepositoryComponent.create().buildQuestionRepository());
  }
}

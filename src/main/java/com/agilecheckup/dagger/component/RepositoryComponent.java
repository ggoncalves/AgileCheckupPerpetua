package com.agilecheckup.dagger.component;

import com.agilecheckup.dagger.module.RepositoryModule;
import com.agilecheckup.persistency.repository.QuestionRepository;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {RepositoryModule.class})
public interface RepositoryComponent {
  QuestionRepository buildQuestionRepository();
}

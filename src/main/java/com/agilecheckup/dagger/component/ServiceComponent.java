package com.agilecheckup.dagger.component;

import com.agilecheckup.dagger.module.ServiceModule;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.QuestionService;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class})
public interface ServiceComponent {
  QuestionService buildQuestionService();
  CompanyService buildCompanyService();
}

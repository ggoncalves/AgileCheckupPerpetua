package com.agilecheckup.dagger.module;

import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.QuestionService;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class ServiceModule {

  @Binds
  abstract AbstractCrudService provideQuestionService(QuestionService questionService);

  @Binds
  abstract AbstractCrudService provideCompanyService(CompanyService companyService);

}

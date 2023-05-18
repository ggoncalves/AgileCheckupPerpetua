package com.agilecheckup.dagger.component;

import com.agilecheckup.dagger.module.ServiceModule;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.DepartmentService;
import com.agilecheckup.service.QuestionService;
import com.agilecheckup.service.TeamService;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class})
public interface ServiceComponent {
  QuestionService buildQuestionService();

  CompanyService buildCompanyService();

  DepartmentService buildDepartmentService();

  TeamService buildTeamService();
}

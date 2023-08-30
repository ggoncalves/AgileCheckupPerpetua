package com.agilecheckup.dagger.component;

import com.agilecheckup.dagger.module.ServiceModule;
import com.agilecheckup.service.*;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class})
public interface ServiceComponent {
  QuestionService buildQuestionService();

  CompanyService buildCompanyService();

  DepartmentService buildDepartmentService();

  TeamService buildTeamService();

  PerformanceCycleService buildPerformanceCycleService();

  AssessmentMatrixService buildAssessmentMatrixService();

  EmployeeAssessmentService buildEmployeeAssessmentService();
}

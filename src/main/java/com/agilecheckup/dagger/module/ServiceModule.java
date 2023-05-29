package com.agilecheckup.dagger.module;

import com.agilecheckup.service.*;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class ServiceModule {

  @Binds
  abstract AbstractCrudService provideQuestionService(QuestionService questionService);

  @Binds
  abstract AbstractCrudService provideCompanyService(CompanyService companyService);

  @Binds
  abstract AbstractCrudService provideDepartmentService(DepartmentService departmentService);

  @Binds
  abstract AbstractCrudService provideTeamService(TeamService teamService);

  @Binds
  abstract AbstractCrudService providePerformanceCycleService(PerformanceCycleService performanceCycleService);

  @Binds
  abstract AbstractCrudService provideAssessmentMatrix(AssessmentMatrixService assessmentMatrixService);

}

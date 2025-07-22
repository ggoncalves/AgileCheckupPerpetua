package com.agilecheckup.dagger.module;

import com.agilecheckup.persistency.repository.DepartmentRepositoryV2;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.AnswerService;
import com.agilecheckup.service.AssessmentMatrixService;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.DepartmentService;
import com.agilecheckup.service.EmployeeAssessmentService;
import com.agilecheckup.service.PerformanceCycleService;
import com.agilecheckup.service.QuestionService;
import com.agilecheckup.service.TeamService;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public abstract class ServiceModule {

  @Binds
  abstract AbstractCrudService provideQuestionService(QuestionService questionService);

  @Binds
  abstract AbstractCrudService provideCompanyService(CompanyService companyService);

  @Provides
  @Singleton
  static DepartmentService provideDepartmentService(DepartmentRepositoryV2 departmentRepositoryV2, 
                                                    CompanyService companyService) {
    return new DepartmentService(departmentRepositoryV2, companyService);
  }

  @Binds
  abstract AbstractCrudService provideTeamService(TeamService teamService);

  @Binds
  abstract AbstractCrudService providePerformanceCycleService(PerformanceCycleService performanceCycleService);

  @Binds
  abstract AbstractCrudService provideAssessmentMatrix(AssessmentMatrixService assessmentMatrixService);

  @Binds
  abstract AbstractCrudService provideEmployeeAssessmentService(EmployeeAssessmentService employeeAssessmentService);

  @Binds
  abstract AbstractCrudService provideAnswerService(AnswerService answerService);


}

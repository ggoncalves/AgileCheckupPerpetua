package com.agilecheckup.dagger.component;

import javax.inject.Singleton;

import com.agilecheckup.dagger.module.AwsConfigModule;
import com.agilecheckup.dagger.module.RepositoryModule;
import com.agilecheckup.dagger.module.ServiceModule;
import com.agilecheckup.service.AnswerService;
import com.agilecheckup.service.AssessmentMatrixService;
import com.agilecheckup.service.AssessmentNavigationService;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.DashboardAnalyticsService;
import com.agilecheckup.service.DepartmentService;
import com.agilecheckup.service.EmployeeAssessmentService;
import com.agilecheckup.service.PerformanceCycleService;
import com.agilecheckup.service.QuestionService;
import com.agilecheckup.service.TeamService;

import dagger.Component;

@Singleton
@Component(modules = {ServiceModule.class, AwsConfigModule.class, RepositoryModule.class})
public interface ServiceComponent {
  QuestionService buildQuestionService();

  CompanyService buildCompanyService();

  DepartmentService buildDepartmentService();

  TeamService buildTeamService();

  PerformanceCycleService buildPerformanceCycleService();

  AssessmentMatrixService buildAssessmentMatrixService();

  EmployeeAssessmentService buildEmployeeAssessmentService();

  AnswerService buildAnswerService();

  AssessmentNavigationService buildAssessmentNavigationService();

  DashboardAnalyticsService buildDashboardAnalyticsService();
}

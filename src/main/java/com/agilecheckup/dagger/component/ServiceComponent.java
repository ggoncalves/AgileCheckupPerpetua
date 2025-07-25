package com.agilecheckup.dagger.component;

import com.agilecheckup.dagger.module.AwsConfigModule;
import com.agilecheckup.dagger.module.AwsConfigModuleV2;
import com.agilecheckup.dagger.module.RepositoryModuleV2;
import com.agilecheckup.dagger.module.ServiceModule;
import com.agilecheckup.service.AnswerService;
import com.agilecheckup.service.AssessmentMatrixService;
import com.agilecheckup.service.AssessmentNavigationService;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.CompanyServiceLegacy;
import com.agilecheckup.service.DashboardAnalyticsService;
import com.agilecheckup.service.DepartmentService;
import com.agilecheckup.service.EmployeeAssessmentService;
import com.agilecheckup.service.PerformanceCycleService;
import com.agilecheckup.service.QuestionService;
import com.agilecheckup.service.TeamService;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class, AwsConfigModule.class, AwsConfigModuleV2.class, RepositoryModuleV2.class})
public interface ServiceComponent {
  QuestionService buildQuestionService();

  CompanyService buildCompanyService();

  CompanyServiceLegacy buildCompanyServiceLegacy();

  DepartmentService buildDepartmentService();

  TeamService buildTeamService();

  PerformanceCycleService buildPerformanceCycleService();

  AssessmentMatrixService buildAssessmentMatrixService();

  EmployeeAssessmentService buildEmployeeAssessmentService();

  AnswerService buildAnswerService();
  
  AssessmentNavigationService buildAssessmentNavigationService();

  DashboardAnalyticsService buildDashboardAnalyticsService();
}

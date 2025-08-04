package com.agilecheckup.dagger.component;

import com.agilecheckup.dagger.module.AwsConfigModule;
import com.agilecheckup.dagger.module.AwsConfigModuleV2;
import com.agilecheckup.dagger.module.RepositoryModuleV2;
import com.agilecheckup.dagger.module.ServiceModule;
import com.agilecheckup.service.AnswerService;
import com.agilecheckup.service.AnswerServiceV2;
import com.agilecheckup.service.AssessmentMatrixService;
import com.agilecheckup.service.AssessmentMatrixServiceV2;
import com.agilecheckup.service.AssessmentNavigationService;
import com.agilecheckup.service.AssessmentNavigationServiceV2;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.CompanyServiceLegacy;
import com.agilecheckup.service.DashboardAnalyticsService;
import com.agilecheckup.service.DashboardAnalyticsServiceV2;
import com.agilecheckup.service.DepartmentService;
import com.agilecheckup.service.EmployeeAssessmentService;
import com.agilecheckup.service.EmployeeAssessmentServiceV2;
import com.agilecheckup.service.PerformanceCycleService;
import com.agilecheckup.service.PerformanceCycleServiceLegacy;
import com.agilecheckup.service.QuestionService;
import com.agilecheckup.service.QuestionServiceV2;
import com.agilecheckup.service.TeamService;
import com.agilecheckup.service.TeamServiceLegacy;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class, AwsConfigModule.class, AwsConfigModuleV2.class, RepositoryModuleV2.class})
public interface ServiceComponent {
  QuestionService buildQuestionService();

  QuestionServiceV2 buildQuestionServiceV2();

  CompanyService buildCompanyService();

  CompanyServiceLegacy buildCompanyServiceLegacy();

  DepartmentService buildDepartmentService();

  TeamService buildTeamService();
  
  TeamServiceLegacy buildTeamServiceLegacy();

  PerformanceCycleService buildPerformanceCycleService();
  
  PerformanceCycleServiceLegacy buildPerformanceCycleServiceLegacy();


  AssessmentMatrixService buildAssessmentMatrixService();

  AssessmentMatrixServiceV2 buildAssessmentMatrixServiceV2();

  EmployeeAssessmentService buildEmployeeAssessmentService();

  EmployeeAssessmentServiceV2 buildEmployeeAssessmentServiceV2();

  AnswerService buildAnswerService();

  AnswerServiceV2 buildAnswerServiceV2();
  
  AssessmentNavigationService buildAssessmentNavigationService();

  AssessmentNavigationServiceV2 buildAssessmentNavigationServiceV2();

  DashboardAnalyticsService buildDashboardAnalyticsService();

  DashboardAnalyticsServiceV2 buildDashboardAnalyticsServiceV2();
}

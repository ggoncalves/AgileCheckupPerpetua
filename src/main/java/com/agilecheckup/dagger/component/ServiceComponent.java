package com.agilecheckup.dagger.component;

import com.agilecheckup.dagger.module.AwsConfigModuleV2;
import com.agilecheckup.dagger.module.RepositoryModuleV2;
import com.agilecheckup.dagger.module.ServiceModule;
import com.agilecheckup.service.AnswerServiceV2;
import com.agilecheckup.service.AssessmentMatrixServiceV2;
import com.agilecheckup.service.AssessmentNavigationServiceV2;
import com.agilecheckup.service.CompanyServiceV2;
import com.agilecheckup.service.DashboardAnalyticsServiceV2;
import com.agilecheckup.service.DepartmentServiceV2;
import com.agilecheckup.service.EmployeeAssessmentServiceV2;
import com.agilecheckup.service.PerformanceCycleServiceV2;
import com.agilecheckup.service.QuestionServiceV2;
import com.agilecheckup.service.TeamServiceV2;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class, AwsConfigModuleV2.class, RepositoryModuleV2.class})
public interface ServiceComponent {
  QuestionServiceV2 buildQuestionServiceV2();

  CompanyServiceV2 buildCompanyService();

  DepartmentServiceV2 buildDepartmentService();

  TeamServiceV2 buildTeamService();

  PerformanceCycleServiceV2 buildPerformanceCycleService();

  AssessmentMatrixServiceV2 buildAssessmentMatrixServiceV2();

  EmployeeAssessmentServiceV2 buildEmployeeAssessmentServiceV2();

  AnswerServiceV2 buildAnswerServiceV2();
  
  AssessmentNavigationServiceV2 buildAssessmentNavigationServiceV2();

  DashboardAnalyticsServiceV2 buildDashboardAnalyticsServiceV2();
}

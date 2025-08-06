package com.agilecheckup.dagger.module;

import com.agilecheckup.persistency.repository.AnswerRepositoryV2;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepositoryV2;
import com.agilecheckup.persistency.repository.CompanyRepositoryV2;
import com.agilecheckup.persistency.repository.DepartmentRepositoryV2;
import com.agilecheckup.persistency.repository.DashboardAnalyticsRepositoryV2;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepositoryV2;
import com.agilecheckup.persistency.repository.QuestionRepositoryV2;
import com.agilecheckup.persistency.repository.TeamRepositoryV2;
import com.agilecheckup.persistency.repository.PerformanceCycleRepositoryV2;
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
import dagger.Binds;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public abstract class ServiceModule {


  @Provides
  @Singleton
  static DepartmentServiceV2 provideDepartmentService(DepartmentRepositoryV2 departmentRepositoryV2,
                                                      CompanyServiceV2 companyServiceV2) {
    return new DepartmentServiceV2(departmentRepositoryV2, companyServiceV2);
  }

  @Provides
  @Singleton
  static TeamServiceV2 provideTeamService(TeamRepositoryV2 teamRepositoryV2) {
    return new TeamServiceV2(teamRepositoryV2);
  }


  @Provides
  @Singleton
  static PerformanceCycleServiceV2 providePerformanceCycleService(PerformanceCycleRepositoryV2 performanceCycleRepositoryV2, CompanyServiceV2 companyServiceV2) {
    return new PerformanceCycleServiceV2(performanceCycleRepositoryV2, companyServiceV2);
  }





  @Provides
  @Singleton  
  static DashboardAnalyticsServiceV2 provideDashboardAnalyticsServiceV2(
      DashboardAnalyticsRepositoryV2 dashboardAnalyticsRepositoryV2,
      AssessmentMatrixServiceV2 assessmentMatrixService,
      EmployeeAssessmentServiceV2 employeeAssessmentService,
      CompanyServiceV2 companyServiceV2,
      PerformanceCycleServiceV2 performanceCycleServiceV2,
      TeamRepositoryV2 teamRepository,
      AnswerRepositoryV2 answerRepository) {
    return new DashboardAnalyticsServiceV2(dashboardAnalyticsRepositoryV2, assessmentMatrixService, 
        employeeAssessmentService, companyServiceV2, performanceCycleServiceV2, teamRepository, answerRepository);
  }


  @Provides
  @Singleton
  static CompanyServiceV2 provideCompanyService(CompanyRepositoryV2 companyRepositoryV2) {
    return new CompanyServiceV2(companyRepositoryV2);
  }

  @Provides
  @Singleton
  static AssessmentMatrixServiceV2 provideAssessmentMatrixServiceV2(
      AssessmentMatrixRepositoryV2 assessmentMatrixRepositoryV2,
      PerformanceCycleServiceV2 performanceCycleServiceV2,
      Lazy<QuestionServiceV2> questionServiceV2,
      Lazy<EmployeeAssessmentServiceV2> employeeAssessmentServiceV2,
      Lazy<TeamServiceV2> teamServiceV2) {
    return new AssessmentMatrixServiceV2(assessmentMatrixRepositoryV2, performanceCycleServiceV2,
        questionServiceV2, employeeAssessmentServiceV2, teamServiceV2);
  }

  @Provides
  @Singleton  
  static EmployeeAssessmentServiceV2 provideEmployeeAssessmentServiceV2(
      EmployeeAssessmentRepositoryV2 employeeAssessmentRepositoryV2,
      AssessmentMatrixServiceV2 assessmentMatrixServiceV2,
      TeamServiceV2 teamService,
      AnswerRepositoryV2 answerRepository) {
    return new EmployeeAssessmentServiceV2(employeeAssessmentRepositoryV2, assessmentMatrixServiceV2,
        teamService, answerRepository);
  }

  @Provides
  @Singleton
  static QuestionServiceV2 provideQuestionServiceV2(
      QuestionRepositoryV2 questionRepositoryV2,
      AssessmentMatrixServiceV2 assessmentMatrixServiceV2) {
    return new QuestionServiceV2(questionRepositoryV2, assessmentMatrixServiceV2);
  }

  @Provides
  @Singleton
  static AnswerServiceV2 provideAnswerServiceV2(
      AnswerRepositoryV2 answerRepositoryV2,
      EmployeeAssessmentServiceV2 employeeAssessmentServiceV2,
      QuestionServiceV2 questionServiceV2,
      AssessmentMatrixServiceV2 assessmentMatrixServiceV2) {
    return new AnswerServiceV2(answerRepositoryV2, employeeAssessmentServiceV2, 
        questionServiceV2, assessmentMatrixServiceV2);
  }

  @Provides
  @Singleton
  static AssessmentNavigationServiceV2 provideAssessmentNavigationServiceV2(
      QuestionServiceV2 questionService,
      AnswerServiceV2 answerServiceV2,
      EmployeeAssessmentServiceV2 employeeAssessmentServiceV2,
      AssessmentMatrixServiceV2 assessmentMatrixServiceV2) {
    return new AssessmentNavigationServiceV2(questionService, answerServiceV2, 
        employeeAssessmentServiceV2, assessmentMatrixServiceV2);
  }

}

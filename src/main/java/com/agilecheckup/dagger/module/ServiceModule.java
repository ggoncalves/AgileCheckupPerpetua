package com.agilecheckup.dagger.module;

import com.agilecheckup.persistency.repository.AnswerRepositoryV2;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepositoryV2;
import com.agilecheckup.persistency.repository.CompanyRepositoryV2;
import com.agilecheckup.persistency.repository.DepartmentRepositoryV2;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepository;
import com.agilecheckup.persistency.repository.DashboardAnalyticsRepository;
import com.agilecheckup.persistency.repository.DashboardAnalyticsRepositoryV2;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepository;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepositoryV2;
import com.agilecheckup.persistency.repository.QuestionRepositoryV2;
import com.agilecheckup.persistency.repository.TeamRepository;
import com.agilecheckup.persistency.repository.TeamRepositoryV2;
import com.agilecheckup.persistency.repository.PerformanceCycleRepository;
import com.agilecheckup.persistency.repository.PerformanceCycleRepositoryV2;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.AnswerService;
import com.agilecheckup.service.AnswerServiceV2;
import com.agilecheckup.service.AssessmentMatrixService;
import com.agilecheckup.service.AssessmentMatrixServiceV2;
import com.agilecheckup.service.AssessmentNavigationServiceV2;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.CompanyServiceV2;
import com.agilecheckup.service.DashboardAnalyticsService;
import com.agilecheckup.service.DashboardAnalyticsServiceV2;
import com.agilecheckup.service.DepartmentServiceV2;
import com.agilecheckup.service.EmployeeAssessmentService;
import com.agilecheckup.service.EmployeeAssessmentServiceV2;
import com.agilecheckup.service.PerformanceCycleService;
import com.agilecheckup.service.PerformanceCycleServiceV2;
import com.agilecheckup.service.QuestionService;
import com.agilecheckup.service.QuestionServiceV2;
import com.agilecheckup.service.TeamService;
import com.agilecheckup.service.TeamServiceV2;
import dagger.Binds;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public abstract class ServiceModule {

  @Binds
  abstract AbstractCrudService provideQuestionService(QuestionService questionService);

  @Binds
  abstract AbstractCrudService provideCompanyServiceLegacy(CompanyService companyService);

  @Provides
  @Singleton
  static DepartmentServiceV2 provideDepartmentService(DepartmentRepositoryV2 departmentRepositoryV2,
                                                      CompanyService companyService) {
    return new DepartmentServiceV2(departmentRepositoryV2, companyService);
  }

  @Provides
  @Singleton
  static TeamServiceV2 provideTeamService(TeamRepositoryV2 teamRepositoryV2) {
    return new TeamServiceV2(teamRepositoryV2);
  }

  @Provides
  @Singleton
  static TeamService provideTeamServiceLegacy(TeamRepository teamRepository, DepartmentServiceV2 departmentServiceV2) {
    return new TeamService(teamRepository, departmentServiceV2);
  }

  @Provides
  @Singleton
  static PerformanceCycleServiceV2 providePerformanceCycleService(PerformanceCycleRepositoryV2 performanceCycleRepositoryV2, CompanyServiceV2 companyServiceV2) {
    return new PerformanceCycleServiceV2(performanceCycleRepositoryV2, companyServiceV2);
  }

  @Provides
  @Singleton
  static PerformanceCycleService providePerformanceCycleServiceLegacy(PerformanceCycleRepository performanceCycleRepository, CompanyService companyService) {
    return new PerformanceCycleService(performanceCycleRepository, companyService);
  }

  @Provides
  @Singleton
  static AssessmentMatrixService provideAssessmentMatrixService(
      AssessmentMatrixRepository assessmentMatrixRepository,
      PerformanceCycleService performanceCycleService,
      Lazy<QuestionService> questionService,
      Lazy<EmployeeAssessmentService> employeeAssessmentService,
      Lazy<TeamService> teamServiceLegacy) {
    return new AssessmentMatrixService(assessmentMatrixRepository, performanceCycleService, 
        questionService, employeeAssessmentService, teamServiceLegacy);
  }

  @Provides
  @Singleton  
  static EmployeeAssessmentService provideEmployeeAssessmentService(
      EmployeeAssessmentRepository employeeAssessmentRepository,
      AssessmentMatrixService assessmentMatrixService,
      TeamService teamService,
      AnswerRepository answerRepository) {
    return new EmployeeAssessmentService(employeeAssessmentRepository, assessmentMatrixService,
        teamService, answerRepository);
  }

  @Provides
  @Singleton  
  static DashboardAnalyticsService provideDashboardAnalyticsService(
      DashboardAnalyticsRepository dashboardAnalyticsRepository,
      AssessmentMatrixServiceV2 assessmentMatrixService,
      EmployeeAssessmentServiceV2 employeeAssessmentService,
      CompanyServiceV2 companyServiceV2,
      PerformanceCycleServiceV2 performanceCycleServiceV2,
      TeamRepositoryV2 teamRepository,
      AnswerRepositoryV2 answerRepository) {
    return new DashboardAnalyticsService(dashboardAnalyticsRepository, assessmentMatrixService, 
        employeeAssessmentService, companyServiceV2, performanceCycleServiceV2, teamRepository, answerRepository);
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

  @Binds
  abstract AbstractCrudService provideAnswerService(AnswerService answerService);

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
      Lazy<QuestionService> questionService,
      Lazy<EmployeeAssessmentServiceV2> employeeAssessmentServiceV2,
      Lazy<TeamService> teamServiceLegacy) {
    return new AssessmentMatrixServiceV2(assessmentMatrixRepositoryV2, performanceCycleServiceV2,
        questionService, employeeAssessmentServiceV2, teamServiceLegacy);
  }

  @Provides
  @Singleton  
  static EmployeeAssessmentServiceV2 provideEmployeeAssessmentServiceV2(
      EmployeeAssessmentRepositoryV2 employeeAssessmentRepositoryV2,
      AssessmentMatrixServiceV2 assessmentMatrixServiceV2,
      TeamService teamService,
      AnswerRepository answerRepository) {
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
      QuestionService questionService,
      AssessmentMatrixServiceV2 assessmentMatrixServiceV2) {
    return new AnswerServiceV2(answerRepositoryV2, employeeAssessmentServiceV2, 
        questionService, assessmentMatrixServiceV2);
  }

  @Provides
  @Singleton
  static AssessmentNavigationServiceV2 provideAssessmentNavigationServiceV2(
      QuestionService questionService,
      AnswerServiceV2 answerServiceV2,
      EmployeeAssessmentServiceV2 employeeAssessmentServiceV2,
      AssessmentMatrixServiceV2 assessmentMatrixServiceV2) {
    return new AssessmentNavigationServiceV2(questionService, answerServiceV2, 
        employeeAssessmentServiceV2, assessmentMatrixServiceV2);
  }

}

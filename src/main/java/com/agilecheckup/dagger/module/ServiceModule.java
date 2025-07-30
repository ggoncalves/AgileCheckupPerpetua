package com.agilecheckup.dagger.module;

import com.agilecheckup.persistency.repository.AssessmentMatrixRepositoryV2;
import com.agilecheckup.persistency.repository.CompanyRepositoryV2;
import com.agilecheckup.persistency.repository.DepartmentRepositoryV2;
import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepository;
import com.agilecheckup.persistency.repository.DashboardAnalyticsRepository;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepository;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepositoryV2;
import com.agilecheckup.persistency.repository.QuestionRepositoryV2;
import com.agilecheckup.persistency.repository.TeamRepository;
import com.agilecheckup.persistency.repository.TeamRepositoryV2;
import com.agilecheckup.persistency.repository.PerformanceCycleRepository;
import com.agilecheckup.persistency.repository.PerformanceCycleRepositoryV2;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.AnswerService;
import com.agilecheckup.service.AssessmentMatrixService;
import com.agilecheckup.service.AssessmentMatrixServiceV2;
import com.agilecheckup.service.DashboardAnalyticsService;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.CompanyServiceLegacy;
import com.agilecheckup.service.DepartmentService;
import com.agilecheckup.service.EmployeeAssessmentService;
import com.agilecheckup.service.EmployeeAssessmentServiceV2;
import com.agilecheckup.service.PerformanceCycleService;
import com.agilecheckup.service.PerformanceCycleServiceLegacy;
import com.agilecheckup.service.QuestionService;
import com.agilecheckup.service.QuestionServiceV2;
import com.agilecheckup.service.TeamService;
import com.agilecheckup.service.TeamServiceLegacy;
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
  abstract AbstractCrudService provideCompanyServiceLegacy(CompanyServiceLegacy companyServiceLegacy);

  @Provides
  @Singleton
  static DepartmentService provideDepartmentService(DepartmentRepositoryV2 departmentRepositoryV2, 
                                                    CompanyServiceLegacy companyServiceLegacy) {
    return new DepartmentService(departmentRepositoryV2, companyServiceLegacy);
  }

  @Provides
  @Singleton
  static TeamService provideTeamService(TeamRepositoryV2 teamRepositoryV2) {
    return new TeamService(teamRepositoryV2);
  }

  @Provides
  @Singleton
  static TeamServiceLegacy provideTeamServiceLegacy(TeamRepository teamRepository, DepartmentService departmentService) {
    return new TeamServiceLegacy(teamRepository, departmentService);
  }

  @Provides
  @Singleton
  static PerformanceCycleService providePerformanceCycleService(PerformanceCycleRepositoryV2 performanceCycleRepositoryV2, CompanyService companyService) {
    return new PerformanceCycleService(performanceCycleRepositoryV2, companyService);
  }

  @Provides
  @Singleton
  static PerformanceCycleServiceLegacy providePerformanceCycleServiceLegacy(PerformanceCycleRepository performanceCycleRepository, CompanyServiceLegacy companyService) {
    return new PerformanceCycleServiceLegacy(performanceCycleRepository, companyService);
  }

  @Provides
  @Singleton
  static AssessmentMatrixService provideAssessmentMatrixService(
      AssessmentMatrixRepository assessmentMatrixRepository,
      PerformanceCycleServiceLegacy performanceCycleService,
      Lazy<QuestionService> questionService,
      Lazy<EmployeeAssessmentService> employeeAssessmentService,
      Lazy<TeamServiceLegacy> teamServiceLegacy) {
    return new AssessmentMatrixService(assessmentMatrixRepository, performanceCycleService, 
        questionService, employeeAssessmentService, teamServiceLegacy);
  }

  @Provides
  @Singleton  
  static EmployeeAssessmentService provideEmployeeAssessmentService(
      EmployeeAssessmentRepository employeeAssessmentRepository,
      AssessmentMatrixService assessmentMatrixService,
      TeamServiceLegacy teamServiceLegacy,
      AnswerRepository answerRepository) {
    return new EmployeeAssessmentService(employeeAssessmentRepository, assessmentMatrixService, 
        teamServiceLegacy, answerRepository);
  }

  @Provides
  @Singleton  
  static DashboardAnalyticsService provideDashboardAnalyticsService(
      DashboardAnalyticsRepository dashboardAnalyticsRepository,
      AssessmentMatrixService assessmentMatrixService,
      EmployeeAssessmentService employeeAssessmentService,
      CompanyServiceLegacy companyService,
      PerformanceCycleServiceLegacy performanceCycleService,
      TeamRepository teamRepository,
      AnswerRepository answerRepository) {
    return new DashboardAnalyticsService(dashboardAnalyticsRepository, assessmentMatrixService, 
        employeeAssessmentService, companyService, performanceCycleService, teamRepository, answerRepository);
  }

  @Binds
  abstract AbstractCrudService provideAnswerService(AnswerService answerService);

  @Provides
  @Singleton
  static CompanyService provideCompanyService(CompanyRepositoryV2 companyRepositoryV2) {
    return new CompanyService(companyRepositoryV2);
  }

  @Provides
  @Singleton
  static AssessmentMatrixServiceV2 provideAssessmentMatrixServiceV2(
      AssessmentMatrixRepositoryV2 assessmentMatrixRepositoryV2,
      PerformanceCycleService performanceCycleService,
      Lazy<QuestionService> questionService,
      Lazy<EmployeeAssessmentService> employeeAssessmentService,
      Lazy<TeamServiceLegacy> teamServiceLegacy) {
    return new AssessmentMatrixServiceV2(assessmentMatrixRepositoryV2, performanceCycleService, 
        questionService, employeeAssessmentService, teamServiceLegacy);
  }

  @Provides
  @Singleton  
  static EmployeeAssessmentServiceV2 provideEmployeeAssessmentServiceV2(
      EmployeeAssessmentRepositoryV2 employeeAssessmentRepositoryV2,
      AssessmentMatrixServiceV2 assessmentMatrixServiceV2,
      TeamServiceLegacy teamServiceLegacy,
      AnswerRepository answerRepository) {
    return new EmployeeAssessmentServiceV2(employeeAssessmentRepositoryV2, assessmentMatrixServiceV2, 
        teamServiceLegacy, answerRepository);
  }

  @Provides
  @Singleton
  static QuestionServiceV2 provideQuestionServiceV2(
      QuestionRepositoryV2 questionRepositoryV2,
      AssessmentMatrixServiceV2 assessmentMatrixServiceV2) {
    return new QuestionServiceV2(questionRepositoryV2, assessmentMatrixServiceV2);
  }

}

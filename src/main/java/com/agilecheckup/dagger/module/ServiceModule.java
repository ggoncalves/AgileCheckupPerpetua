package com.agilecheckup.dagger.module;

import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepository;
import com.agilecheckup.persistency.repository.CompanyRepository;
import com.agilecheckup.persistency.repository.DashboardAnalyticsRepository;
import com.agilecheckup.persistency.repository.DepartmentRepository;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepository;
import com.agilecheckup.persistency.repository.QuestionRepository;
import com.agilecheckup.persistency.repository.TeamRepository;
import com.agilecheckup.persistency.repository.PerformanceCycleRepository;
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
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public abstract class ServiceModule {


  @Provides
  @Singleton
  static DepartmentService provideDepartmentService(DepartmentRepository departmentRepositoryV2,
                                                    CompanyService companyServiceV2) {
    return new DepartmentService(departmentRepositoryV2, companyServiceV2);
  }

  @Provides
  @Singleton
  static TeamService provideTeamService(TeamRepository teamRepositoryV2) {
    return new TeamService(teamRepositoryV2);
  }


  @Provides
  @Singleton
  static PerformanceCycleService providePerformanceCycleService(PerformanceCycleRepository performanceCycleRepositoryV2, CompanyService companyServiceV2) {
    return new PerformanceCycleService(performanceCycleRepositoryV2, companyServiceV2);
  }





  @Provides
  @Singleton  
  static DashboardAnalyticsService provideDashboardAnalyticsServiceV2(
      DashboardAnalyticsRepository dashboardAnalyticsRepository,
      AssessmentMatrixService assessmentMatrixService,
      EmployeeAssessmentService employeeAssessmentService,
      CompanyService companyServiceV2,
      PerformanceCycleService performanceCycleServiceV2,
      TeamRepository teamRepository,
      AnswerRepository answerRepository) {
    return new DashboardAnalyticsService(dashboardAnalyticsRepository, assessmentMatrixService,
        employeeAssessmentService, companyServiceV2, performanceCycleServiceV2, teamRepository, answerRepository);
  }


  @Provides
  @Singleton
  static CompanyService provideCompanyService(CompanyRepository companyRepositoryV2) {
    return new CompanyService(companyRepositoryV2);
  }

  @Provides
  @Singleton
  static AssessmentMatrixService provideAssessmentMatrixServiceV2(
      AssessmentMatrixRepository assessmentMatrixRepositoryV2,
      PerformanceCycleService performanceCycleServiceV2,
      Lazy<QuestionService> questionServiceV2,
      Lazy<EmployeeAssessmentService> employeeAssessmentServiceV2,
      Lazy<TeamService> teamServiceV2) {
    return new AssessmentMatrixService(assessmentMatrixRepositoryV2, performanceCycleServiceV2,
        questionServiceV2, employeeAssessmentServiceV2, teamServiceV2);
  }

  @Provides
  @Singleton  
  static EmployeeAssessmentService provideEmployeeAssessmentServiceV2(
      EmployeeAssessmentRepository employeeAssessmentRepositoryV2,
      AssessmentMatrixService assessmentMatrixServiceV2,
      TeamService teamService,
      AnswerRepository answerRepository) {
    return new EmployeeAssessmentService(employeeAssessmentRepositoryV2, assessmentMatrixServiceV2,
        teamService, answerRepository);
  }

  @Provides
  @Singleton
  static QuestionService provideQuestionServiceV2(
      QuestionRepository questionRepositoryV2,
      AssessmentMatrixService assessmentMatrixServiceV2) {
    return new QuestionService(questionRepositoryV2, assessmentMatrixServiceV2);
  }

  @Provides
  @Singleton
  static AnswerService provideAnswerServiceV2(
      AnswerRepository answerRepositoryV2,
      EmployeeAssessmentService employeeAssessmentServiceV2,
      QuestionService questionServiceV2,
      AssessmentMatrixService assessmentMatrixServiceV2) {
    return new AnswerService(answerRepositoryV2, employeeAssessmentServiceV2,
        questionServiceV2, assessmentMatrixServiceV2);
  }

  @Provides
  @Singleton
  static AssessmentNavigationService provideAssessmentNavigationServiceV2(
      QuestionService questionService,
      AnswerService answerServiceV2,
      EmployeeAssessmentService employeeAssessmentServiceV2,
      AssessmentMatrixService assessmentMatrixServiceV2) {
    return new AssessmentNavigationService(questionService, answerServiceV2,
        employeeAssessmentServiceV2, assessmentMatrixServiceV2);
  }

}

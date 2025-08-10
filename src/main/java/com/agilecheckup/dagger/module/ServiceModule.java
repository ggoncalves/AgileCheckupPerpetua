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
  static DepartmentService provideDepartmentService(DepartmentRepository departmentRepository,
                                                    CompanyService companyService) {
    return new DepartmentService(departmentRepository, companyService);
  }

  @Provides
  @Singleton
  static TeamService provideTeamService(TeamRepository teamRepository) {
    return new TeamService(teamRepository);
  }


  @Provides
  @Singleton
  static PerformanceCycleService providePerformanceCycleService(PerformanceCycleRepository performanceCycleRepository, CompanyService companyService) {
    return new PerformanceCycleService(performanceCycleRepository, companyService);
  }





  @Provides
  @Singleton  
  static DashboardAnalyticsService provideDashboardAnalyticsService(
      DashboardAnalyticsRepository dashboardAnalyticsRepository,
      AssessmentMatrixService assessmentMatrixService,
      EmployeeAssessmentService employeeAssessmentService,
      CompanyService companyService,
      PerformanceCycleService performanceCycleService,
      TeamRepository teamRepository,
      AnswerRepository answerRepository) {
    return new DashboardAnalyticsService(dashboardAnalyticsRepository, assessmentMatrixService,
        employeeAssessmentService, companyService, performanceCycleService, teamRepository, answerRepository);
  }


  @Provides
  @Singleton
  static CompanyService provideCompanyService(CompanyRepository companyRepository) {
    return new CompanyService(companyRepository);
  }

  @Provides
  @Singleton
  static AssessmentMatrixService provideAssessmentMatrixService(
      AssessmentMatrixRepository assessmentMatrixRepository,
      PerformanceCycleService performanceCycleService,
      Lazy<QuestionService> questionService,
      Lazy<EmployeeAssessmentService> employeeAssessmentService,
      Lazy<TeamService> teamService) {
    return new AssessmentMatrixService(assessmentMatrixRepository, performanceCycleService,
        questionService, employeeAssessmentService, teamService);
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
  static QuestionService provideQuestionService(
      QuestionRepository questionRepository,
      AssessmentMatrixService assessmentMatrixService) {
    return new QuestionService(questionRepository, assessmentMatrixService);
  }

  @Provides
  @Singleton
  static AnswerService provideAnswerService(
      AnswerRepository answerRepository,
      EmployeeAssessmentService employeeAssessmentService,
      QuestionService questionService,
      AssessmentMatrixService assessmentMatrixService) {
    return new AnswerService(answerRepository, employeeAssessmentService,
        questionService, assessmentMatrixService);
  }

  @Provides
  @Singleton
  static AssessmentNavigationService provideAssessmentNavigationService(
      QuestionService questionService,
      AnswerService answerService,
      EmployeeAssessmentService employeeAssessmentService,
      AssessmentMatrixService assessmentMatrixService) {
    return new AssessmentNavigationService(questionService, answerService,
        employeeAssessmentService, assessmentMatrixService);
  }

}

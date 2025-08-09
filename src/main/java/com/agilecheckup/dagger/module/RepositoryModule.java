package com.agilecheckup.dagger.module;

import com.agilecheckup.persistency.repository.AnswerRepository;
import com.agilecheckup.persistency.repository.AssessmentMatrixRepository;
import com.agilecheckup.persistency.repository.CompanyRepository;
import com.agilecheckup.persistency.repository.DashboardAnalyticsRepository;
import com.agilecheckup.persistency.repository.DepartmentRepository;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepository;
import com.agilecheckup.persistency.repository.PerformanceCycleRepository;
import com.agilecheckup.persistency.repository.QuestionRepository;
import com.agilecheckup.persistency.repository.TeamRepository;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import javax.inject.Singleton;

@Module
public class RepositoryModule {
    
    @Provides
    @Singleton
    public DepartmentRepository provideDepartmentRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new DepartmentRepository(enhancedClient);
    }

    @Provides
    @Singleton
    public CompanyRepository provideCompanyRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new CompanyRepository(enhancedClient);
    }

    @Provides
    @Singleton
    public TeamRepository provideTeamRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new TeamRepository(enhancedClient);
    }

    @Provides
    @Singleton
    public PerformanceCycleRepository providePerformanceCycleRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new PerformanceCycleRepository(enhancedClient);
    }

    @Provides
    @Singleton
    public AssessmentMatrixRepository provideAssessmentMatrixRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new AssessmentMatrixRepository(enhancedClient);
    }

    @Provides
    @Singleton
    public EmployeeAssessmentRepository provideEmployeeAssessmentRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new EmployeeAssessmentRepository(enhancedClient);
    }

    @Provides
    @Singleton
    public QuestionRepository provideQuestionRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new QuestionRepository(enhancedClient);
    }

    @Provides
    @Singleton
    public AnswerRepository provideAnswerRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new AnswerRepository(enhancedClient);
    }

    @Provides
    @Singleton
    public DashboardAnalyticsRepository provideDashboardAnalyticsRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new DashboardAnalyticsRepository(enhancedClient);
    }
}
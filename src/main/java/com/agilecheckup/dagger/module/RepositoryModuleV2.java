package com.agilecheckup.dagger.module;

import com.agilecheckup.persistency.repository.AssessmentMatrixRepositoryV2;
import com.agilecheckup.persistency.repository.CompanyRepositoryV2;
import com.agilecheckup.persistency.repository.DepartmentRepositoryV2;
import com.agilecheckup.persistency.repository.EmployeeAssessmentRepositoryV2;
import com.agilecheckup.persistency.repository.PerformanceCycleRepositoryV2;
import com.agilecheckup.persistency.repository.TeamRepositoryV2;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import javax.inject.Singleton;

@Module
public class RepositoryModuleV2 {
    
    @Provides
    @Singleton
    public DepartmentRepositoryV2 provideDepartmentRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new DepartmentRepositoryV2(enhancedClient);
    }

    @Provides
    @Singleton
    public CompanyRepositoryV2 provideCompanyRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new CompanyRepositoryV2(enhancedClient);
    }

    @Provides
    @Singleton
    public TeamRepositoryV2 provideTeamRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new TeamRepositoryV2(enhancedClient);
    }

    @Provides
    @Singleton
    public PerformanceCycleRepositoryV2 providePerformanceCycleRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new PerformanceCycleRepositoryV2(enhancedClient);
    }

    @Provides
    @Singleton
    public AssessmentMatrixRepositoryV2 provideAssessmentMatrixRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new AssessmentMatrixRepositoryV2(enhancedClient);
    }

    @Provides
    @Singleton
    public EmployeeAssessmentRepositoryV2 provideEmployeeAssessmentRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        return new EmployeeAssessmentRepositoryV2(enhancedClient);
    }
}
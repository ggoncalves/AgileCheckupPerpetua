package com.agilecheckup.dagger.module;

import com.agilecheckup.persistency.repository.DepartmentRepositoryV2;
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
}
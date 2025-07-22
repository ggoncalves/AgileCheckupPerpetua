package com.agilecheckup.dagger.component;

import com.agilecheckup.dagger.module.AwsConfigModuleV2;
import com.agilecheckup.dagger.module.RepositoryModuleV2;
import com.agilecheckup.persistency.repository.DepartmentRepositoryV2;
import dagger.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AwsConfigModuleV2.class, RepositoryModuleV2.class})
public interface ApplicationComponentV2 {

    // TODO: It seems that this class is unecessary and can be removed. Is this right?

    DynamoDbEnhancedClient dynamoDbEnhancedClient();
    
    DepartmentRepositoryV2 departmentRepositoryV2();
    
}
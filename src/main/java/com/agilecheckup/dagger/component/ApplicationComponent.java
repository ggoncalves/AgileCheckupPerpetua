package com.agilecheckup.dagger.component;

import javax.inject.Singleton;

import com.agilecheckup.dagger.module.AwsConfigModule;
import com.agilecheckup.dagger.module.RepositoryModule;
import com.agilecheckup.persistency.repository.DepartmentRepository;

import dagger.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@Singleton
@Component(modules = {AwsConfigModule.class, RepositoryModule.class})
public interface ApplicationComponent {

  // TODO: It seems that this class is unecessary and can be removed. Is this right?

  DynamoDbEnhancedClient dynamoDbEnhancedClient();

  DepartmentRepository departmentRepository();

}
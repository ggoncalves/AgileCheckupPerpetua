package com.agilecheckup.dagger.component;

import com.agilecheckup.dagger.module.AwsConfigModule;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AwsConfigModule.class})
public interface AwsConfigComponent {
  DynamoDBMapper buildDynamoDbMapper();
}

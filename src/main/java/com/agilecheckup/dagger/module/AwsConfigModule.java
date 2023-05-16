package com.agilecheckup.dagger.module;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class AwsConfigModule {

  @Provides
  @Singleton
  public DynamoDBMapper provideDynamoDbMapper() {
    return new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
        .withRegion(Regions.US_EAST_1)
        .build());
  }
}

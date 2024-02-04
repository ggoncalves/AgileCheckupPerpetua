package com.agilecheckup.dagger.module;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBLockClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBLockClientOptions;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Module
public class AwsConfigModule {

  @Provides
  @Singleton
  public DynamoDBMapper provideDynamoDbMapper() {
    return new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
        .withRegion(Regions.US_EAST_1)
        .build());
  }


  @Provides
  @Singleton
  public AmazonDynamoDBLockClient provideDynamoDBLockClient(AmazonDynamoDB amazonDynamoDB) {
    AmazonDynamoDBLockClientOptions options = AmazonDynamoDBLockClientOptions
        .builder(amazonDynamoDB, "DistributedLockTable")
        .withLeaseDuration(120L) // por quanto tempo o bloqueio durará
        .withHeartbeatPeriod(1L) // a frequência com que o cliente atualizará o registro de bloqueio
        .withTimeUnit(TimeUnit.SECONDS) // a unidade de tempo para acima
        .withPartitionKeyName("lockKey") // a chave de partição do registro de bloqueio na tabela de bloqueio
        .withCreateHeartbeatBackgroundThread(true)
        .build();

    return new AmazonDynamoDBLockClient(options);
  }


  @Provides
  @Singleton
  public AmazonDynamoDB provideAmazonDynamoDB() {
    return AmazonDynamoDBClientBuilder.standard()
        .withRegion(Regions.US_EAST_1) // ou qualquer outra região que você esteja usando
        .build();
  }
}

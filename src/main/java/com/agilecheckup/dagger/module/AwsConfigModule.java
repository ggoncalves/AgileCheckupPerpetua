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


  // TODO: Note that this method might be kept in here during SDK V2 migration. See details bellow:
  //
  // The current V1 implementation uses:
  //  - AmazonDynamoDBLockClient from AWS SDK V1
  //  - AmazonDynamoDB client
  //
  //  For V2, you would need to either:
  //
  //  1. Use a third-party library - Find a V2-compatible distributed locking library that works with DynamoDbClient
  //  2. Keep this specific dependency on V1 - Since distributed locking might not be critical path functionality, you could keep using the V1 lock client alongside your V2
  //  implementation temporarily
  //  3. Implement custom locking - Create your own distributed locking mechanism using V2's DynamoDbClient and conditional writes
  //  4. Wait for AWS - AWS might eventually provide a V2 equivalent, though there's no guarantee
  //
  //  The most practical approach during migration would be option 2 - keep the V1 lock client for now since it's likely used for specific operational scenarios rather than core
  //  business logic. This allows you to complete the main entity migration to V2 while deferring the locking mechanism upgrade to a later phase.
  //
  //  So yes, it's possible but will require additional architectural decisions about how to handle distributed locking in the V2 world.
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

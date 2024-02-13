package com.agilecheckup.persistency.repository;

import com.agilecheckup.dagger.component.DaggerAwsConfigComponent;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;

public abstract class AbstractCrudRepository<T>{

  @Getter
  protected final DynamoDBMapper dynamoDBMapper;

  private final Class<T> clazz;


  // TODO :See ChatGPT prompt https://chat.openai.com/c/e0e78642-75b6-490f-bd7b-c2cae182be95
  public AbstractCrudRepository(Class<T> clazz) {
    this(clazz, DaggerAwsConfigComponent.create().buildDynamoDbMapper());
  }

  @VisibleForTesting
  public AbstractCrudRepository(Class<T> clazz, DynamoDBMapper dynamoDBMapper) {
    this.dynamoDBMapper = dynamoDBMapper;
    this.clazz = clazz;
  }

  public T save(T t) {
    dynamoDBMapper.save(t);
    return t;
  }

  public void delete(T t) {
    dynamoDBMapper.delete(t);
  }

  public T findById(String id) {
    return dynamoDBMapper.load(clazz, id);
  }

  public PaginatedScanList<T> findAll() {
    return dynamoDBMapper.scan(clazz, new DynamoDBScanExpression());
  }
}

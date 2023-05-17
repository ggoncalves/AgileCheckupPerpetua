package com.agilecheckup.persistency.repository;

import com.agilecheckup.dagger.component.DaggerAwsConfigComponent;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.google.common.annotations.VisibleForTesting;

public abstract class AbstractCrudRepository<T>{

  private final DynamoDBMapper dynamoDBMapper;

  private final Class<T> clazz;

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

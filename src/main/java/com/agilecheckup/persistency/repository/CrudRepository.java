package com.agilecheckup.persistency.repository;

import com.agilecheckup.dagger.component.AwsConfigComponent;
import com.agilecheckup.dagger.component.DaggerAwsConfigComponent;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

public class CrudRepository<T>{

  private final DynamoDBMapper dynamoDBMapper;

  private final Class<T> clazz;

  public CrudRepository(Class<T> clazz) {
    AwsConfigComponent awsConfigComponent = DaggerAwsConfigComponent.create();
    this.dynamoDBMapper = awsConfigComponent.buildDynamoDbMapper();
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

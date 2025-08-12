package com.agilecheckup.persistency.repository;

import javax.inject.Inject;

import com.agilecheckup.persistency.entity.Company;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

public class CompanyRepository extends AbstractCrudRepository<Company> {

  @Inject
  public CompanyRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
    super(dynamoDbEnhancedClient, Company.class, "Company");
  }

  public CompanyRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient, String tableName) {
    super(dynamoDbEnhancedClient, Company.class, tableName);
  }
}
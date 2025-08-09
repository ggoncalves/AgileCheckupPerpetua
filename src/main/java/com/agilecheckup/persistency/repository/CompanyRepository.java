package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.Company;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import javax.inject.Inject;

public class CompanyRepository extends AbstractCrudRepository<Company> {

    @Inject
    public CompanyRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        super(dynamoDbEnhancedClient, Company.class, "Company");
    }

    public CompanyRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient, String tableName) {
        super(dynamoDbEnhancedClient, Company.class, tableName);
    }
}
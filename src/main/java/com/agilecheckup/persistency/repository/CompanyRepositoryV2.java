package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.CompanyV2;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import javax.inject.Inject;

public class CompanyRepositoryV2 extends AbstractCrudRepositoryV2<CompanyV2> {

    @Inject
    public CompanyRepositoryV2(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        super(dynamoDbEnhancedClient, CompanyV2.class, "Company");
    }

    public CompanyRepositoryV2(DynamoDbEnhancedClient dynamoDbEnhancedClient, String tableName) {
        super(dynamoDbEnhancedClient, CompanyV2.class, tableName);
    }
}
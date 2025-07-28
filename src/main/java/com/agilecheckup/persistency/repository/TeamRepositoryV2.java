package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.TeamV2;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class TeamRepositoryV2 extends AbstractCrudRepositoryV2<TeamV2> {

    @Inject
    public TeamRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, TeamV2.class, "Team");
    }

    public List<TeamV2> findByDepartmentId(String departmentId) {
        QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(departmentId)))
                .build();

        return getTable().index("departmentId-index")
                .query(queryRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    public List<TeamV2> findAllByTenantId(String tenantId) {
        return queryBySecondaryIndex("tenantId-index", "tenantId", tenantId);
    }
}
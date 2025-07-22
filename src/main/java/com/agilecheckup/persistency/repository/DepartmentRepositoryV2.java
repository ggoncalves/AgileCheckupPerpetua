package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.DepartmentV2;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Log4j2
public class DepartmentRepositoryV2 extends AbstractCrudRepositoryV2<DepartmentV2> {
    
    private static final String TABLE_NAME = "Department";
    private static final String TENANT_INDEX_NAME = "tenantId-index";
    
    @Inject
    public DepartmentRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, DepartmentV2.class, TABLE_NAME);
    }
    
    public List<DepartmentV2> findAllByTenantId(String tenantId) {
        log.info("DepartmentRepositoryV2.findAllByTenantId called with tenantId: {}", tenantId);
        List<DepartmentV2> results = queryBySecondaryIndex(TENANT_INDEX_NAME, "tenantId", tenantId);
        log.info("DepartmentRepositoryV2.findAllByTenantId returning {} results", results.size());
        return results;
    }
}
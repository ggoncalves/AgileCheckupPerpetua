package com.agilecheckup.persistency.repository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.agilecheckup.persistency.entity.Department;

import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@Singleton
@Log4j2
public class DepartmentRepository extends AbstractCrudRepository<Department> {

  private static final String TABLE_NAME = "Department";
  private static final String TENANT_INDEX_NAME = "tenantId-index";

  @Inject
  public DepartmentRepository(DynamoDbEnhancedClient enhancedClient) {
    super(enhancedClient, Department.class, TABLE_NAME);
  }

  public List<Department> findAllByTenantId(String tenantId) {
    log.info("DepartmentRepository.findAllByTenantId called with tenantId: {}", tenantId);
    List<Department> results = queryBySecondaryIndex(TENANT_INDEX_NAME, "tenantId", tenantId);
    log.info("DepartmentRepository.findAllByTenantId returning {} results", results.size());
    return results;
  }
}
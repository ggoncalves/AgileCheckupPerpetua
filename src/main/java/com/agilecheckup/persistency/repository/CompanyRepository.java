package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.Company;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;

public class CompanyRepository extends AbstractCrudRepository<Company> {

  @Inject
  public CompanyRepository() {
    super(Company.class);
  }

  @VisibleForTesting
  public CompanyRepository(DynamoDBMapper dynamoDBMapper) {
    super(Company.class, dynamoDBMapper);
  }

  public PaginatedQueryList<Company> findAllByTenantId(String tenantId) {
    return super.findAllByTenantId(tenantId);
  }
}

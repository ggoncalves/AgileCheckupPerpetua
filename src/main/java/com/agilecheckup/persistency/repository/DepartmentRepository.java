package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.Department;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;

public class DepartmentRepository extends AbstractCrudRepository<Department> {

  @Inject
  public DepartmentRepository() {
    super(Department.class);
  }

  @VisibleForTesting
  public DepartmentRepository(DynamoDBMapper dynamoDBMapper) {
    super(Department.class, dynamoDBMapper);
  }

}

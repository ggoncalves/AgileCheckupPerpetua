package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;

public class EmployeeAssessmentRepository extends AbstractCrudRepository<EmployeeAssessment> {

  @Inject
  public EmployeeAssessmentRepository() {
    super(EmployeeAssessment.class);
  }

  @VisibleForTesting
  public EmployeeAssessmentRepository(DynamoDBMapper dynamoDBMapper) {
    super(EmployeeAssessment.class, dynamoDBMapper);
  }

}

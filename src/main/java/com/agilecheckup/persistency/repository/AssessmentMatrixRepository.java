package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;

public class AssessmentMatrixRepository extends AbstractCrudRepository<AssessmentMatrix> {

  @Inject
  public AssessmentMatrixRepository() {
    super(AssessmentMatrix.class);
  }

  @VisibleForTesting
  public AssessmentMatrixRepository(DynamoDBMapper dynamoDBMapper) {
    super(AssessmentMatrix.class, dynamoDBMapper);
  }

}

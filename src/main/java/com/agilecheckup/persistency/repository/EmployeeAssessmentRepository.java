package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.EmployeeAssessment;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class EmployeeAssessmentRepository extends AbstractCrudRepository<EmployeeAssessment> {

  @Inject
  public EmployeeAssessmentRepository() {
    super(EmployeeAssessment.class);
  }

  @VisibleForTesting
  public EmployeeAssessmentRepository(DynamoDBMapper dynamoDBMapper) {
    super(EmployeeAssessment.class, dynamoDBMapper);
  }

  /**
   * Check if employee assessment already exists for the given assessment matrix and employee email.
   * Uses GSI for efficient querying instead of scanning the entire table.
   * 
   * @param assessmentMatrixId The assessment matrix ID
   * @param employeeEmail The employee email (will be normalized to lowercase)
   * @return true if employee assessment exists, false otherwise
   */
  public boolean existsByAssessmentMatrixAndEmployeeEmail(String assessmentMatrixId, String employeeEmail) {
    String normalizedEmail = employeeEmail.toLowerCase().trim();
    
    Map<String, AttributeValue> eav = new HashMap<>();
    eav.put(":assessmentMatrixId", new AttributeValue().withS(assessmentMatrixId));
    eav.put(":employeeEmail", new AttributeValue().withS(normalizedEmail));
    
    DynamoDBQueryExpression<EmployeeAssessment> queryExpression = new DynamoDBQueryExpression<EmployeeAssessment>()
        .withIndexName("assessmentMatrixId-employeeEmail-index")
        .withConsistentRead(false) // GSI doesn't support consistent reads
        .withKeyConditionExpression("assessmentMatrixId = :assessmentMatrixId AND employeeEmailNormalized = :employeeEmail")
        .withExpressionAttributeValues(eav)
        .withLimit(1); // We only need to know if any exists
    
    try {
      PaginatedQueryList<EmployeeAssessment> results = getDynamoDBMapper().query(EmployeeAssessment.class, queryExpression);
      return results != null && !results.isEmpty();
      
    } catch (Exception e) {
      // If GSI doesn't exist or there's a configuration error, fail fast
      throw new RuntimeException("GSI query failed. Please check if GSI 'assessmentMatrixId-employeeEmail-index' exists and is Active.", e);
    }
  }

}

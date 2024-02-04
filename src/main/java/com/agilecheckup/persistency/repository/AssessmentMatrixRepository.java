package com.agilecheckup.persistency.repository;


import com.agilecheckup.persistency.entity.AssessmentMatrix;
import com.amazonaws.services.dynamodbv2.AcquireLockOptions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBLockClient;
import com.amazonaws.services.dynamodbv2.LockItem;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.LockNotGrantedException;
import com.google.common.annotations.VisibleForTesting;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AssessmentMatrixRepository extends AbstractCrudRepository<AssessmentMatrix> {

  private AmazonDynamoDBLockClient lockClient;

  @Inject
  public AssessmentMatrixRepository(AmazonDynamoDBLockClient lockClient) {
    super(AssessmentMatrix.class);
    this.lockClient = lockClient;
  }

  public AssessmentMatrixRepository() {
    super(AssessmentMatrix.class);
  }

  @VisibleForTesting
  public AssessmentMatrixRepository(DynamoDBMapper dynamoDBMapper) {
    super(AssessmentMatrix.class, dynamoDBMapper);
  }

  public void performLocked(String matrixId, Runnable action) {
    Optional<LockItem> lockItemOption = Optional.empty();
    try {
      lockItemOption = lockClient.tryAcquireLock(AcquireLockOptions.builder("assessmentMatrix-" + matrixId)
          .withReplaceData(false)
          .withAdditionalTimeToWaitForLock(Long.MAX_VALUE)
          .withTimeUnit(TimeUnit.SECONDS)
          .build());

      if (lockItemOption.isPresent()) {
        try {
          action.run();
        }
        finally {
          lockClient.releaseLock(lockItemOption.get());
        }
      }
      else {
        throw new RuntimeException("Unable to acquire lock for assessmentMatrix " + matrixId);
      }
    }
    catch (LockNotGrantedException e) {
      throw new RuntimeException("Unable to acquire lock for assessmentMatrix " + matrixId, e);
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}

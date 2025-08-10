package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.AssessmentMatrix;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Log4j2
@Singleton
public class AssessmentMatrixRepository extends AbstractCrudRepository<AssessmentMatrix> {

    @Inject
    public AssessmentMatrixRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, AssessmentMatrix.class, "AssessmentMatrix");
    }

    public List<AssessmentMatrix> findAllByTenantId(String tenantId) {
        log.info("AssessmentMatrixRepository.findAllByTenantId called with tenantId: {}", tenantId);
        return queryBySecondaryIndex("tenantId-index", "tenantId", tenantId);
    }
}
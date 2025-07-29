package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Log4j2
@Singleton
public class AssessmentMatrixRepositoryV2 extends AbstractCrudRepositoryV2<AssessmentMatrixV2> {

    @Inject
    public AssessmentMatrixRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, AssessmentMatrixV2.class, "AssessmentMatrix");
    }

    public List<AssessmentMatrixV2> findAllByTenantId(String tenantId) {
        log.info("AssessmentMatrixRepositoryV2.findAllByTenantId called with tenantId: {}", tenantId);
        return queryBySecondaryIndex("tenantId-index", "tenantId", tenantId);
    }
}
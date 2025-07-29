package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.PerformanceCycleV2;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Singleton
public class PerformanceCycleRepositoryV2 extends AbstractCrudRepositoryV2<PerformanceCycleV2> {

    @Inject
    public PerformanceCycleRepositoryV2(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, PerformanceCycleV2.class, "PerformanceCycle");
    }

    public List<PerformanceCycleV2> findAllByTenantId(String tenantId) {
        log.info("PerformanceCycleRepositoryV2.findAllByTenantId called with tenantId: {}", tenantId);
        return queryBySecondaryIndex("tenantId-index", "tenantId", tenantId);
    }

    public List<PerformanceCycleV2> findByCompanyId(String companyId) {
        log.info("PerformanceCycleRepositoryV2.findByCompanyId called with companyId: {}", companyId);
        return queryBySecondaryIndex("companyId-index", "companyId", companyId);
    }

    public List<PerformanceCycleV2> findActiveByTenantId(String tenantId) {
        log.info("PerformanceCycleRepositoryV2.findActiveByTenantId called with tenantId: {}", tenantId);
        List<PerformanceCycleV2> allCycles = findAllByTenantId(tenantId);
        return allCycles.stream()
                .filter(cycle -> Boolean.TRUE.equals(cycle.getIsActive()))
                .collect(Collectors.toList());
    }
}
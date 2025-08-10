package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.PerformanceCycle;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Singleton
public class PerformanceCycleRepository extends AbstractCrudRepository<PerformanceCycle> {

    @Inject
    public PerformanceCycleRepository(DynamoDbEnhancedClient enhancedClient) {
        super(enhancedClient, PerformanceCycle.class, "PerformanceCycle");
    }

    public List<PerformanceCycle> findAllByTenantId(String tenantId) {
        log.info("PerformanceCycleRepository.findAllByTenantId called with tenantId: {}", tenantId);
        return queryBySecondaryIndex("tenantId-index", "tenantId", tenantId);
    }

    public List<PerformanceCycle> findByCompanyId(String companyId) {
        log.info("PerformanceCycleRepository.findByCompanyId called with companyId: {}", companyId);
        return queryBySecondaryIndex("companyId-index", "companyId", companyId);
    }

    public List<PerformanceCycle> findActiveByTenantId(String tenantId) {
        log.info("PerformanceCycleRepository.findActiveByTenantId called with tenantId: {}", tenantId);
        List<PerformanceCycle> allCycles = findAllByTenantId(tenantId);
        return allCycles.stream()
                .filter(cycle -> Boolean.TRUE.equals(cycle.getIsActive()))
                .collect(Collectors.toList());
    }
}
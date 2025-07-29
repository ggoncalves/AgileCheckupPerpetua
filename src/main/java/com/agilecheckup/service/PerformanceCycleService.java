package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.CompanyV2;
import com.agilecheckup.persistency.entity.PerformanceCycleV2;
import com.agilecheckup.persistency.repository.PerformanceCycleRepositoryV2;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Log4j2
@Singleton
public class PerformanceCycleService extends AbstractCrudServiceV2<PerformanceCycleV2, PerformanceCycleRepositoryV2> {

    private final PerformanceCycleRepositoryV2 performanceCycleRepository;
    private final CompanyService companyService;

    @Inject
    public PerformanceCycleService(PerformanceCycleRepositoryV2 performanceCycleRepository, CompanyService companyService) {
        this.performanceCycleRepository = performanceCycleRepository;
        this.companyService = companyService;
    }

    public Optional<PerformanceCycleV2> create(String tenantId, String name, String description, String companyId,
                                               Boolean isActive, Boolean isTimeSensitive, LocalDate startDate, LocalDate endDate) {
        log.info("PerformanceCycleService.create called with tenantId: {}, name: {}", tenantId, name);
        // Business rule: isTimeSensitive is true only if endDate is present
        Boolean calculatedIsTimeSensitive = (endDate != null);
        return super.create(createPerformanceCycle(tenantId, name, description, companyId, isActive, calculatedIsTimeSensitive, startDate, endDate));
    }

    public Optional<PerformanceCycleV2> update(String id, String tenantId, String name, String description, String companyId,
                                               Boolean isActive, Boolean isTimeSensitive, LocalDate startDate, LocalDate endDate) {
        log.info("PerformanceCycleService.update called with id: {}, tenantId: {}, name: {}", id, tenantId, name);
        Optional<PerformanceCycleV2> optionalPerformanceCycle = findById(id);
        if (optionalPerformanceCycle.isPresent()) {
            PerformanceCycleV2 performanceCycle = optionalPerformanceCycle.get();
            Optional<CompanyV2> company = companyService.findById(companyId);
            
            // Business rule: isTimeSensitive is true only if endDate is present
            Boolean calculatedIsTimeSensitive = (endDate != null);
            
            PerformanceCycleV2 updatedCycle = PerformanceCycleV2.builder()
                    .id(performanceCycle.getId())
                    .createdDate(performanceCycle.getCreatedDate())
                    .lastUpdatedDate(performanceCycle.getLastUpdatedDate())
                    .tenantId(tenantId)
                    .name(name)
                    .description(description)
                    .companyId(company.orElseThrow(() -> new InvalidIdReferenceException(companyId, "PerformanceCycle", "Company")).getId())
                    .isActive(isActive)
                    .isTimeSensitive(calculatedIsTimeSensitive)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
            return super.update(updatedCycle);
        } else {
            return Optional.empty();
        }
    }

    private PerformanceCycleV2 createPerformanceCycle(String tenantId, String name, String description, String companyId,
                                                      Boolean isActive, Boolean isTimeSensitive, LocalDate startDate, LocalDate endDate) {
        Optional<CompanyV2> company = companyService.findById(companyId);
        return PerformanceCycleV2.builder()
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .companyId(company.orElseThrow(() -> new InvalidIdReferenceException(companyId, "PerformanceCycle", "Company")).getId())
                .isActive(isActive)
                .isTimeSensitive(isTimeSensitive)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    public List<PerformanceCycleV2> findAllByTenantId(String tenantId) {
        log.info("PerformanceCycleService.findAllByTenantId called with tenantId: {}", tenantId);
        return performanceCycleRepository.findAllByTenantId(tenantId);
    }

    public List<PerformanceCycleV2> findByCompanyId(String companyId) {
        log.info("PerformanceCycleService.findByCompanyId called with companyId: {}", companyId);
        return performanceCycleRepository.findByCompanyId(companyId);
    }

    public List<PerformanceCycleV2> findActiveByTenantId(String tenantId) {
        log.info("PerformanceCycleService.findActiveByTenantId called with tenantId: {}", tenantId);
        return performanceCycleRepository.findActiveByTenantId(tenantId);
    }

    @Override
    PerformanceCycleRepositoryV2 getRepository() {
        return performanceCycleRepository;
    }
}
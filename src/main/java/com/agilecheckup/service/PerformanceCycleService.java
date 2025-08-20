package com.agilecheckup.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.repository.PerformanceCycleRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Singleton
public class PerformanceCycleService extends AbstractCrudService<PerformanceCycle, PerformanceCycleRepository> {

  private final PerformanceCycleRepository performanceCycleRepository;
  private final CompanyService companyService;

  @Inject
  public PerformanceCycleService(PerformanceCycleRepository performanceCycleRepository, CompanyService companyService) {
    this.performanceCycleRepository = performanceCycleRepository;
    this.companyService = companyService;
  }

  public Optional<PerformanceCycle> create(String tenantId, String name, String description, String companyId, Boolean isActive, Boolean isTimeSensitive, LocalDate startDate, LocalDate endDate) {
    log.info("PerformanceCycleService.create called with tenantId: {}, name: {}", tenantId, name);
    // Business rule: isTimeSensitive is true only if endDate is present
    Boolean calculatedIsTimeSensitive = (endDate != null);
    return super.create(createPerformanceCycle(tenantId, name, description, companyId, isActive, calculatedIsTimeSensitive, startDate, endDate));
  }

  public Optional<PerformanceCycle> update(String id, String tenantId, String name, String description, String companyId, Boolean isActive, Boolean isTimeSensitive, LocalDate startDate, LocalDate endDate) {
    log.info("PerformanceCycleService.update called with id: {}, tenantId: {}, name: {}", id, tenantId, name);
    Optional<PerformanceCycle> optionalPerformanceCycle = findById(id);
    if (optionalPerformanceCycle.isPresent()) {
      PerformanceCycle performanceCycle = optionalPerformanceCycle.get();
      Optional<Company> company = companyService.findById(companyId);

      // Business rule: isTimeSensitive is true only if endDate is present
      Boolean calculatedIsTimeSensitive = (endDate != null);

      PerformanceCycle updatedCycle = PerformanceCycle.builder()
                                                      .id(performanceCycle.getId())
                                                      .createdDate(performanceCycle.getCreatedDate())
                                                      .lastUpdatedDate(performanceCycle.getLastUpdatedDate())
                                                      .tenantId(tenantId)
                                                      .name(name)
                                                      .description(description)
                                                      .companyId(company.orElseThrow(() -> new InvalidIdReferenceException(companyId, "PerformanceCycle", "Company"))
                                                                        .getId())
                                                      .isActive(isActive)
                                                      .isTimeSensitive(calculatedIsTimeSensitive)
                                                      .startDate(startDate)
                                                      .endDate(endDate)
                                                      .build();
      return super.update(updatedCycle);
    }
    else {
      return Optional.empty();
    }
  }

  private PerformanceCycle createPerformanceCycle(String tenantId, String name, String description, String companyId, Boolean isActive, Boolean isTimeSensitive, LocalDate startDate, LocalDate endDate) {
    Optional<Company> company = companyService.findById(companyId);
    return PerformanceCycle.builder()
                           .tenantId(tenantId)
                           .name(name)
                           .description(description)
                           .companyId(company.orElseThrow(() -> new InvalidIdReferenceException(companyId, "PerformanceCycle", "Company"))
                                             .getId())
                           .isActive(isActive)
                           .isTimeSensitive(isTimeSensitive)
                           .startDate(startDate)
                           .endDate(endDate)
                           .build();
  }

  public List<PerformanceCycle> findAllByTenantId(String tenantId) {
    log.info("PerformanceCycleService.findAllByTenantId called with tenantId: {}", tenantId);
    return performanceCycleRepository.findAllByTenantId(tenantId);
  }

  public List<PerformanceCycle> findByCompanyId(String companyId) {
    log.info("PerformanceCycleService.findByCompanyId called with companyId: {}", companyId);
    return performanceCycleRepository.findByCompanyId(companyId);
  }

  public List<PerformanceCycle> findActiveByTenantId(String tenantId) {
    log.info("PerformanceCycleService.findActiveByTenantId called with tenantId: {}", tenantId);
    return performanceCycleRepository.findActiveByTenantId(tenantId);
  }

  @Override
  PerformanceCycleRepository getRepository() {
    return performanceCycleRepository;
  }
}
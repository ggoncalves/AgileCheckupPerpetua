package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.PerformanceCycleRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;

import javax.inject.Inject;
import java.util.Optional;

public class PerformanceCycleService extends AbstractCrudService<PerformanceCycle, AbstractCrudRepository<PerformanceCycle>> {

  private final PerformanceCycleRepository performanceCycleRepository;

  private final CompanyService companyService;

  @Inject
  public PerformanceCycleService(PerformanceCycleRepository performanceCycleRepository, CompanyService companyService) {
    this.performanceCycleRepository = performanceCycleRepository;
    this.companyService = companyService;
  }

  public Optional<PerformanceCycle> create(String name, String description, String tenantId, String companyId,
                                           Boolean isActive, Boolean isTimeSensitive) {
    return super.create(createPerformanceCycle(name, description, tenantId, companyId, isActive, isTimeSensitive));
  }

  private PerformanceCycle createPerformanceCycle(String name, String description, String tenantId, String companyId,
                                                  Boolean isActive, Boolean isTimeSensitive) {
    Optional<Company> company = companyService.findById(companyId);
    PerformanceCycle performanceCycle = PerformanceCycle.builder()
        .name(name)
        .description(description)
        .tenantId(tenantId)
        .company(company.orElseThrow(() -> new InvalidIdReferenceException(companyId, "PerformanceCycle", "Company")))
        .isActive(isActive)
        .isTimeSensitive(isTimeSensitive)
        .build();
    return setFixedIdIfConfigured(performanceCycle);
  }

  @Override
  AbstractCrudRepository<PerformanceCycle> getRepository() {
    return performanceCycleRepository;
  }
}
package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.PerformanceCycleRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;

import javax.inject.Inject;
import java.util.Date;
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
                                           Boolean isActive, Boolean isTimeSensitive, Date startDate, Date endDate) {
    // Business rule: isTimeSensitive is true only if endDate is present
    Boolean calculatedIsTimeSensitive = (endDate != null);
    return super.create(createPerformanceCycle(name, description, tenantId, companyId, isActive, calculatedIsTimeSensitive, startDate, endDate));
  }

  public Optional<PerformanceCycle> update(String id, String name, String description, String tenantId, String companyId,
                                           Boolean isActive, Boolean isTimeSensitive, Date startDate, Date endDate) {
    Optional<PerformanceCycle> optionalPerformanceCycle = findById(id);
    if (optionalPerformanceCycle.isPresent()) {
      PerformanceCycle performanceCycle = optionalPerformanceCycle.get();
      Optional<Company> company = companyService.findById(companyId);
      
      performanceCycle.setName(name);
      performanceCycle.setDescription(description);
      performanceCycle.setTenantId(tenantId);
      performanceCycle.setCompanyId(company.orElseThrow(() -> new InvalidIdReferenceException(companyId, "PerformanceCycle", "Company")).getId());
      performanceCycle.setIsActive(isActive);
      // Business rule: isTimeSensitive is true only if endDate is present
      performanceCycle.setIsTimeSensitive(endDate != null);
      performanceCycle.setStartDate(startDate);
      performanceCycle.setEndDate(endDate);
      return super.update(performanceCycle);
    } else {
      return Optional.empty();
    }
  }

  private PerformanceCycle createPerformanceCycle(String name, String description, String tenantId, String companyId,
                                                  Boolean isActive, Boolean isTimeSensitive, Date startDate, Date endDate) {
    Optional<Company> company = companyService.findById(companyId);
    return PerformanceCycle.builder()
        .name(name)
        .description(description)
        .tenantId(tenantId)
        .companyId(company.orElseThrow(() -> new InvalidIdReferenceException(companyId, "PerformanceCycle", "Company")).getId())
        .isActive(isActive)
        .isTimeSensitive(isTimeSensitive)
        .startDate(startDate)
        .endDate(endDate)
        .build();
  }

  @Override
  AbstractCrudRepository<PerformanceCycle> getRepository() {
    return performanceCycleRepository;
  }
}
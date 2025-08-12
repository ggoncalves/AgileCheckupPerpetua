package com.agilecheckup.service;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.Department;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.DepartmentRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;

import lombok.extern.log4j.Log4j2;

@Singleton
@Log4j2
public class DepartmentService extends AbstractCrudService<Department, AbstractCrudRepository<Department>> {

  private final DepartmentRepository departmentRepository;
  private final CompanyService companyService;

  @Inject
  public DepartmentService(DepartmentRepository departmentRepository, CompanyService companyService) {
    this.departmentRepository = departmentRepository;
    this.companyService = companyService;
  }

  public Optional<Department> create(String name, String description, String tenantId, String companyId) {
    return super.create(createDepartment(name, description, tenantId, companyId));
  }

  public Optional<Department> update(String id, String name, String description, String tenantId, String companyId) {
    Optional<Department> optionalDepartment = findById(id);
    if (optionalDepartment.isPresent()) {
      Department department = optionalDepartment.get();
      Optional<Company> company = companyService.findById(companyId);
      department.setName(name);
      department.setDescription(description);
      department.setTenantId(tenantId);
      department.setCompanyId(company.orElseThrow(
          () -> new InvalidIdReferenceException(companyId, "Department", "Company")
      ).getId());
      return super.update(department);
    }
    else {
      return Optional.empty();
    }
  }

  private Department createDepartment(String name, String description, String tenantId, String companyId) {
    Optional<Company> company = companyService.findById(companyId);
    Department department = new Department();
    department.setName(name);
    department.setDescription(description);
    department.setTenantId(tenantId);
    department.setCompanyId(company.orElseThrow(
        () -> new InvalidIdReferenceException(companyId, "Department", "Company")
    ).getId());
    return department;
  }

  public List<Department> findAllByTenantId(String tenantId) {
    log.info("DepartmentService.findAllByTenantId called with tenantId: {}", tenantId);
    List<Department> results = departmentRepository.findAllByTenantId(tenantId);
    log.info("DepartmentService.findAllByTenantId returning {} results", results.size());
    return results;
  }

  @Override
  AbstractCrudRepository<Department> getRepository() {
    return departmentRepository;
  }
}
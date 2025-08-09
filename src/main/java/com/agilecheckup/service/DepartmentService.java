package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.Department;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.DepartmentRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
@Log4j2
public class DepartmentService extends AbstractCrudService<Department, AbstractCrudRepository<Department>> {
    
    private final DepartmentRepository departmentRepositoryV2;
    private final CompanyService companyServiceV2;
    
    @Inject
    public DepartmentService(DepartmentRepository departmentRepositoryV2, CompanyService companyServiceV2) {
        this.departmentRepositoryV2 = departmentRepositoryV2;
        this.companyServiceV2 = companyServiceV2;
    }
    
    public Optional<Department> create(String name, String description, String tenantId, String companyId) {
        return super.create(createDepartment(name, description, tenantId, companyId));
    }
    
    public Optional<Department> update(String id, String name, String description, String tenantId, String companyId) {
        Optional<Department> optionalDepartment = findById(id);
        if (optionalDepartment.isPresent()) {
            Department department = optionalDepartment.get();
            Optional<Company> company = companyServiceV2.findById(companyId);
            department.setName(name);
            department.setDescription(description);
            department.setTenantId(tenantId);
            department.setCompanyId(company.orElseThrow(
                    () -> new InvalidIdReferenceException(companyId, "Department", "Company")
            ).getId());
            return super.update(department);
        } else {
            return Optional.empty();
        }
    }
    
    private Department createDepartment(String name, String description, String tenantId, String companyId) {
        Optional<Company> company = companyServiceV2.findById(companyId);
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
        List<Department> results = departmentRepositoryV2.findAllByTenantId(tenantId);
        log.info("DepartmentService.findAllByTenantId returning {} results", results.size());
        return results;
    }
    
    @Override
    AbstractCrudRepository<Department> getRepository() {
        return departmentRepositoryV2;
    }
}
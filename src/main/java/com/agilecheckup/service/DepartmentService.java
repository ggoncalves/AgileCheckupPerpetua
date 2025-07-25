package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.DepartmentV2;
import com.agilecheckup.persistency.repository.AbstractCrudRepositoryV2;
import com.agilecheckup.persistency.repository.DepartmentRepositoryV2;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
@Log4j2
public class DepartmentService extends AbstractCrudServiceV2<DepartmentV2, AbstractCrudRepositoryV2<DepartmentV2>> {
    
    private final DepartmentRepositoryV2 departmentRepositoryV2;
    private final CompanyServiceLegacy companyService;
    
    @Inject
    public DepartmentService(DepartmentRepositoryV2 departmentRepositoryV2, CompanyServiceLegacy companyService) {
        this.departmentRepositoryV2 = departmentRepositoryV2;
        this.companyService = companyService;
    }
    
    public Optional<DepartmentV2> create(String name, String description, String tenantId, String companyId) {
        return super.create(createDepartment(name, description, tenantId, companyId));
    }
    
    public Optional<DepartmentV2> update(String id, String name, String description, String tenantId, String companyId) {
        Optional<DepartmentV2> optionalDepartment = findById(id);
        if (optionalDepartment.isPresent()) {
            DepartmentV2 department = optionalDepartment.get();
            Optional<Company> company = companyService.findById(companyId);
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
    
    private DepartmentV2 createDepartment(String name, String description, String tenantId, String companyId) {
        Optional<Company> company = companyService.findById(companyId);
        DepartmentV2 department = new DepartmentV2();
        department.setName(name);
        department.setDescription(description);
        department.setTenantId(tenantId);
        department.setCompanyId(company.orElseThrow(
                () -> new InvalidIdReferenceException(companyId, "Department", "Company")
        ).getId());
        return department;
    }
    
    public List<DepartmentV2> findAllByTenantId(String tenantId) {
        log.info("DepartmentService.findAllByTenantId called with tenantId: {}", tenantId);
        List<DepartmentV2> results = departmentRepositoryV2.findAllByTenantId(tenantId);
        log.info("DepartmentService.findAllByTenantId returning {} results", results.size());
        return results;
    }
    
    @Override
    AbstractCrudRepositoryV2<DepartmentV2> getRepository() {
        return departmentRepositoryV2;
    }
}
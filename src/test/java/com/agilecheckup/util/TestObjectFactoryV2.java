package com.agilecheckup.util;

import com.agilecheckup.persistency.entity.DepartmentV2;
import lombok.NonNull;

import java.time.Instant;

public class TestObjectFactoryV2 {

  public static final String GENERIC_ID_1234 = "1234";
  public static final String GENERIC_TENANT_ID = "tenant-123";
  public static final String GENERIC_COMPANY_ID = "company-456";

  public static DepartmentV2 createMockedDepartmentV2() {
    DepartmentV2 department = new DepartmentV2();
    department.setName("DepartmentName");
    department.setDescription("Department description");
    department.setTenantId(GENERIC_TENANT_ID);
    department.setCompanyId(GENERIC_COMPANY_ID);
    department.setCreatedDate(Instant.now().minusSeconds(86400));
    department.setLastModifiedDate(Instant.now());
    return department;
  }

  public static DepartmentV2 createMockedDepartmentV2WithDependenciesId(String companyId) {
    DepartmentV2 department = createMockedDepartmentV2();
    department.setCompanyId(companyId);
    return department;
  }

  public static DepartmentV2 createMockedDepartmentV2(String id) {
    DepartmentV2 department = createMockedDepartmentV2();
    department.setId(id);
    return department;
  }

  public static DepartmentV2 createMockedDepartmentV2(@NonNull String name, @NonNull String description,
                                                      @NonNull String tenantId, @NonNull String companyId) {
    DepartmentV2 department = new DepartmentV2();
    department.setName(name);
    department.setDescription(description);
    department.setTenantId(tenantId);
    department.setCompanyId(companyId);
    department.setCreatedDate(Instant.now().minusSeconds(86400));
    department.setLastModifiedDate(Instant.now());
    return department;
  }

  public static DepartmentV2 copyDepartmentV2AndAddId(DepartmentV2 department, String id) {
    DepartmentV2 copy = new DepartmentV2();
    copy.setId(id);
    copy.setName(department.getName());
    copy.setDescription(department.getDescription());
    copy.setTenantId(department.getTenantId());
    copy.setCompanyId(department.getCompanyId());
    copy.setCreatedDate(department.getCreatedDate());
    copy.setLastModifiedDate(department.getLastModifiedDate());
    return copy;
  }
}
package com.agilecheckup.util;

import com.agilecheckup.persistency.entity.CompanyV2;
import com.agilecheckup.persistency.entity.CompanySize;
import com.agilecheckup.persistency.entity.DepartmentV2;
import com.agilecheckup.persistency.entity.Industry;
import com.agilecheckup.persistency.entity.person.AddressV2;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPersonV2;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import lombok.NonNull;

import java.time.Instant;

public class TestObjectFactoryV2 {

  public static final String GENERIC_ID_1234 = "1234";
  public static final String GENERIC_TENANT_ID = "tenant-123";
  public static final String GENERIC_COMPANY_ID = "company-456";
  public static final String DEFAULT_COMPANY_NAME = "Company Name";
  public static final String DEFAULT_COMPANY_EMAIL = "company@email.com";
  public static final String DEFAULT_COMPANY_DOCUMENT = "0001";

  // === Department V2 Factory Methods ===

  public static DepartmentV2 createMockedDepartmentV2() {
    DepartmentV2 department = new DepartmentV2();
    department.setName("DepartmentName");
    department.setDescription("Department description");
    department.setTenantId(GENERIC_TENANT_ID);
    department.setCompanyId(GENERIC_COMPANY_ID);
    department.setCreatedDate(Instant.now().minusSeconds(86400));
    department.setLastUpdatedDate(Instant.now());
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
    department.setLastUpdatedDate(Instant.now());
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
    copy.setLastUpdatedDate(department.getLastUpdatedDate());
    return copy;
  }

  // === Company V2 Factory Methods ===

  public static CompanyV2 createMockedCompanyV2() {
    return CompanyV2.builder()
        .name(DEFAULT_COMPANY_NAME)
        .email(DEFAULT_COMPANY_EMAIL)
        .description("Company description")
        .tenantId(GENERIC_TENANT_ID)
        .personDocumentType(PersonDocumentType.CNPJ)
        .documentNumber(DEFAULT_COMPANY_DOCUMENT)
        .size(CompanySize.MEDIUM)
        .industry(Industry.TECHNOLOGY)
        .website("https://www.company.com")
        .legalName("Company Legal Name Inc.")
        .contactPerson(createMockedNaturalPersonV2())
        .address(createMockedAddressV2())
        .createdDate(Instant.now().minusSeconds(86400))
        .lastUpdatedDate(Instant.now())
        .build();
  }

  public static CompanyV2 createMockedCompanyV2(String id) {
    CompanyV2 company = createMockedCompanyV2();
    company.setId(id);
    return company;
  }

  public static CompanyV2 createMockedCompanyV2(@NonNull String name, @NonNull String email, @NonNull String description,
                                                @NonNull String tenantId, @NonNull String documentNumber) {
    return CompanyV2.builder()
        .name(name)
        .email(email)
        .description(description)
        .tenantId(tenantId)
        .personDocumentType(PersonDocumentType.CNPJ)
        .documentNumber(documentNumber)
        .size(CompanySize.STARTUP)
        .industry(Industry.OTHER)
        .createdDate(Instant.now().minusSeconds(86400))
        .lastUpdatedDate(Instant.now())
        .build();
  }

  public static CompanyV2 copyCompanyV2AndAddId(CompanyV2 company, String id) {
    return CompanyV2.builder()
        .id(id)
        .name(company.getName())
        .email(company.getEmail())
        .description(company.getDescription())
        .tenantId(company.getTenantId())
        .personDocumentType(company.getPersonDocumentType())
        .documentNumber(company.getDocumentNumber())
        .size(company.getSize())
        .industry(company.getIndustry())
        .website(company.getWebsite())
        .legalName(company.getLegalName())
        .contactPerson(company.getContactPerson())
        .address(company.getAddress())
        .phone(company.getPhone())
        .createdDate(company.getCreatedDate())
        .lastUpdatedDate(company.getLastUpdatedDate())
        .build();
  }

  // === Address V2 Factory Methods ===

  public static AddressV2 createMockedAddressV2() {
    return AddressV2.builder()
        .street("123 Main Street")
        .city("São Paulo")
        .state("SP")
        .zipcode("01234-567")
        .country("Brazil")
        .build();
  }

  // === NaturalPerson V2 Factory Methods ===

  public static NaturalPersonV2 createMockedNaturalPersonV2() {
    return createMockedNaturalPersonV2("John Doe");
  }

  public static NaturalPersonV2 createMockedNaturalPersonV2(String name) {
    return NaturalPersonV2.builder()
        .name(name)
        .email("john.doe@company.com")
        .phone("+55 11 99999-9999")
        .personDocumentType(PersonDocumentType.CPF)
        .documentNumber("1234")
        .aliasName("Johnny")
        .gender(Gender.MALE)
        .genderPronoun(GenderPronoun.HE)
        .address(createMockedAddressV2())
        .createdDate(Instant.now().minusSeconds(86400))
        .lastUpdatedDate(Instant.now())
        .build();
  }
}
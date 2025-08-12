package com.agilecheckup.service;

import java.util.Optional;

import javax.inject.Inject;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.CompanySize;
import com.agilecheckup.persistency.entity.Industry;
import com.agilecheckup.persistency.entity.person.Address;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.repository.CompanyRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompanyService extends AbstractCrudService<Company, CompanyRepository> {

  private final CompanyRepository companyRepository;

  @Inject
  public CompanyService(CompanyRepository companyRepository) {
    this.companyRepository = companyRepository;
  }

  @Override
  CompanyRepository getRepository() {
    return companyRepository;
  }

  /**
   * Legacy create method - creates company with basic fields only
   */
  @Deprecated
  public Optional<Company> create(String documentNumber, String name, String email, String description, String tenantId) {
    Company company = createCompany(documentNumber, name, email, description, tenantId);
    return super.create(company);
  }

  /**
   * Create company with required fields (size and industry)
   */
  public Optional<Company> create(String documentNumber, String name, String email, String description, String tenantId, CompanySize size, Industry industry) {
    Company company = createCompany(documentNumber, name, email, description, tenantId, size, industry, null, null, null, null);
    return super.create(company);
  }

  /**
   * Create company with all fields
   */
  public Optional<Company> create(String documentNumber, String name, String email, String description, String tenantId, CompanySize size, Industry industry, String website, String legalName, NaturalPerson contactPerson, Address address) {
    Company company = createCompany(documentNumber, name, email, description, tenantId, size, industry, website, legalName, contactPerson, address);
    return super.create(company);
  }

  /**
   * Legacy update method - updates company with basic fields only
   */
  @Deprecated
  public Optional<Company> update(String id, String documentNumber, String name, String email, String description, String tenantId) {
    Optional<Company> optionalCompany = findById(id);
    if (optionalCompany.isPresent()) {
      Company company = optionalCompany.get();
      company.setDocumentNumber(documentNumber);
      company.setName(name);
      company.setEmail(email);
      company.setDescription(description);
      company.setTenantId(tenantId);
      return super.update(company);
    }
    return Optional.empty();
  }

  /**
   * Update company with required fields (size and industry)
   */
  public Optional<Company> update(String id, String documentNumber, String name, String email, String description, String tenantId, CompanySize size, Industry industry) {
    return updateCompany(id, documentNumber, name, email, description, tenantId, size, industry, null, null, null, null);
  }

  /**
   * Update company with all fields
   */
  public Optional<Company> update(String id, String documentNumber, String name, String email, String description, String tenantId, CompanySize size, Industry industry, String website, String legalName, NaturalPerson contactPerson, Address address) {
    return updateCompany(id, documentNumber, name, email, description, tenantId, size, industry, website, legalName, contactPerson, address);
  }


  /**
   * Create company with basic fields (legacy support)
   */
  private Company createCompany(String documentNumber, String name, String email, String description, String tenantId) {
    return Company.builder().personDocumentType(PersonDocumentType.CNPJ).documentNumber(documentNumber).name(name).email(email).description(description).tenantId(tenantId).build();
  }

  /**
   * Create company with all fields using builder pattern
   */
  private Company createCompany(String documentNumber, String name, String email, String description, String tenantId, CompanySize size, Industry industry, String website, String legalName, NaturalPerson contactPerson, Address address) {
    return Company.builder().personDocumentType(PersonDocumentType.CNPJ).documentNumber(documentNumber).name(name).email(email).description(description).tenantId(tenantId).size(size).industry(industry).website(website).legalName(legalName).contactPerson(contactPerson).address(address).build();
  }

  /**
   * Update company with all fields
   */
  private Optional<Company> updateCompany(String id, String documentNumber, String name, String email, String description, String tenantId, CompanySize size, Industry industry, String website, String legalName, NaturalPerson contactPerson, Address address) {
    Optional<Company> optionalCompany = findById(id);
    if (optionalCompany.isPresent()) {
      Company company = optionalCompany.get();

      company.setDocumentNumber(documentNumber);
      company.setName(name);
      company.setEmail(email);
      company.setDescription(description);
      company.setTenantId(tenantId);
      company.setSize(size);
      company.setIndustry(industry);
      company.setWebsite(website);
      company.setLegalName(legalName);
      company.setContactPerson(contactPerson);
      company.setAddress(address);

      return super.update(company);
    }
    return Optional.empty();
  }
}
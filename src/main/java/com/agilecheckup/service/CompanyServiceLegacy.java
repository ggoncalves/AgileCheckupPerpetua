package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.CompanySize;
import com.agilecheckup.persistency.entity.Industry;
import com.agilecheckup.persistency.entity.person.Address;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.CompanyRepository;

import javax.inject.Inject;
import java.util.Optional;

public class CompanyServiceLegacy extends AbstractCrudService<Company, AbstractCrudRepository<Company>> {

  private final CompanyRepository companyRepository;

  @Inject
  public CompanyServiceLegacy(CompanyRepository companyRepository) {
    this.companyRepository = companyRepository;
  }

  @Deprecated
  public Optional<Company> create(String documentNumber, String name, String email, String description, String tenantId) {
    return super.create(createCompany(documentNumber, name, email, description, tenantId));
  }

  public Optional<Company> create(String documentNumber, String name, String email, String description, 
                                  String tenantId, CompanySize size, Industry industry) {
    return super.create(createCompany(documentNumber, name, email, description, tenantId, size, industry, null, null, null, null));
  }

  public Optional<Company> create(String documentNumber, String name, String email, String description, 
                                  String tenantId, CompanySize size, Industry industry, String website, 
                                  String legalName, NaturalPerson contactPerson, Address address) {
    return super.create(createCompany(documentNumber, name, email, description, tenantId, size, industry, website, legalName, contactPerson, address));
  }

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
    } else {
      return Optional.empty();
    }
  }

  public Optional<Company> update(String id, String documentNumber, String name, String email, String description, 
                                  String tenantId, CompanySize size, Industry industry) {
    return updateCompany(id, documentNumber, name, email, description, tenantId, size, industry, null, null, null, null);
  }

  public Optional<Company> update(String id, String documentNumber, String name, String email, String description, 
                                  String tenantId, CompanySize size, Industry industry, String website, 
                                  String legalName, NaturalPerson contactPerson, Address address) {
    return updateCompany(id, documentNumber, name, email, description, tenantId, size, industry, website, legalName, contactPerson, address);
  }

  private Company createCompany(String documentNumber, String name, String email, String description, String tenantId) {
    return Company.builder()
        .personDocumentType(PersonDocumentType.CNPJ)
        .documentNumber(documentNumber)
        .name(name)
        .email(email)
        .description(description)
        .tenantId(tenantId).build();
  }

  private Company createCompany(String documentNumber, String name, String email, String description, 
                                String tenantId, CompanySize size, Industry industry, String website, 
                                String legalName, NaturalPerson contactPerson, Address address) {
    return Company.builder()
        .personDocumentType(PersonDocumentType.CNPJ)
        .documentNumber(documentNumber)
        .name(name)
        .email(email)
        .description(description)
        .tenantId(tenantId)
        .size(size)
        .industry(industry)
        .website(website)
        .legalName(legalName)
        .contactPerson(contactPerson)
        .address(address)
        .build();
  }

  private Optional<Company> updateCompany(String id, String documentNumber, String name, String email, 
                                          String description, String tenantId, CompanySize size, Industry industry, 
                                          String website, String legalName, NaturalPerson contactPerson, Address address) {
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
    } else {
      return Optional.empty();
    }
  }

  @Override
  AbstractCrudRepository<Company> getRepository() {
    return companyRepository;
  }
}
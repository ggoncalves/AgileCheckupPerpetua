package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.CompanyRepository;

import javax.inject.Inject;
import java.util.Optional;

public class CompanyService extends AbstractCrudService<Company, AbstractCrudRepository<Company>> {

  private final CompanyRepository companyRepository;

  @Inject
  public CompanyService(CompanyRepository companyRepository) {
    this.companyRepository = companyRepository;
  }

  public Optional<Company> create(String documentNumber, String name, String email, String description, String tenantId) {
    return super.create(createCompany(documentNumber, name, email, description, tenantId));
  }

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

  private Company createCompany(String documentNumber, String name, String email, String description, String tenantId) {
    return Company.builder()
        .personDocumentType(PersonDocumentType.CNPJ)
        .documentNumber(documentNumber)
        .name(name)
        .email(email)
        .description(description)
        .tenantId(tenantId).build();
  }

  @Override
  AbstractCrudRepository<Company> getRepository() {
    return companyRepository;
  }
}
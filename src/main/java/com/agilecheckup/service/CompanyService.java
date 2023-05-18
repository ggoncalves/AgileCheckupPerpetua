package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.PersonDocumentType;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.CompanyRepository;

import javax.inject.Inject;
import java.util.Optional;

public class CompanyService extends AbstractCrudService<Company, AbstractCrudRepository<Company>> {

  private CompanyRepository companyRepository;

  @Inject
  public CompanyService(CompanyRepository companyRepository) {
    this.companyRepository = companyRepository;
  }

  public Optional<Company> create(String documentNumber, String name, String email, String description, String tenantId) {
    return super.create(Company.builder()
        .personDocumentType(PersonDocumentType.CNPJ)
        .documentNumber(documentNumber)
        .name(name)
        .email(email)
        .description(description)
        .tenantId(tenantId)
        .build());
  }

  @Override
  AbstractCrudRepository<Company> getRepository() {
    return companyRepository;
  }
}
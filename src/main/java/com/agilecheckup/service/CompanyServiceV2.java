package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.CompanyV2;
import com.agilecheckup.persistency.entity.CompanySize;
import com.agilecheckup.persistency.entity.Industry;
import com.agilecheckup.persistency.entity.person.AddressV2;
import com.agilecheckup.persistency.entity.person.NaturalPersonV2;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.repository.CompanyRepositoryV2;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Optional;

@Slf4j
public class CompanyServiceV2 extends AbstractCrudServiceV2<CompanyV2, CompanyRepositoryV2> {

    private final CompanyRepositoryV2 companyRepository;

    @Inject
    public CompanyServiceV2(CompanyRepositoryV2 companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    CompanyRepositoryV2 getRepository() {
        return companyRepository;
    }

    /**
     * Legacy create method - creates company with basic fields only
     */
    @Deprecated
    public Optional<CompanyV2> create(String documentNumber, String name, String email, String description, String tenantId) {
        CompanyV2 company = createCompany(documentNumber, name, email, description, tenantId);
        return super.create(company);
    }

    /**
     * Create company with required fields (size and industry)
     */
    public Optional<CompanyV2> create(String documentNumber, String name, String email, String description, 
                                      String tenantId, CompanySize size, Industry industry) {
        CompanyV2 company = createCompany(documentNumber, name, email, description, tenantId, size, industry, null, null, null, null);
        return super.create(company);
    }

    /**
     * Create company with all fields
     */
    public Optional<CompanyV2> create(String documentNumber, String name, String email, String description, 
                                      String tenantId, CompanySize size, Industry industry, String website, 
                                      String legalName, NaturalPersonV2 contactPerson, AddressV2 address) {
        CompanyV2 company = createCompany(documentNumber, name, email, description, tenantId, size, industry, website, legalName, contactPerson, address);
        return super.create(company);
    }

    /**
     * Legacy update method - updates company with basic fields only
     */
    @Deprecated
    public Optional<CompanyV2> update(String id, String documentNumber, String name, String email, String description, String tenantId) {
        Optional<CompanyV2> optionalCompany = findById(id);
        if (optionalCompany.isPresent()) {
            CompanyV2 company = optionalCompany.get();
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
    public Optional<CompanyV2> update(String id, String documentNumber, String name, String email, String description, 
                                      String tenantId, CompanySize size, Industry industry) {
        return updateCompany(id, documentNumber, name, email, description, tenantId, size, industry, null, null, null, null);
    }

    /**
     * Update company with all fields
     */
    public Optional<CompanyV2> update(String id, String documentNumber, String name, String email, String description, 
                                      String tenantId, CompanySize size, Industry industry, String website, 
                                      String legalName, NaturalPersonV2 contactPerson, AddressV2 address) {
        return updateCompany(id, documentNumber, name, email, description, tenantId, size, industry, website, legalName, contactPerson, address);
    }


    /**
     * Create company with basic fields (legacy support)
     */
    private CompanyV2 createCompany(String documentNumber, String name, String email, String description, String tenantId) {
        return CompanyV2.builder()
            .personDocumentType(PersonDocumentType.CNPJ)
            .documentNumber(documentNumber)
            .name(name)
            .email(email)
            .description(description)
            .tenantId(tenantId)
            .build();
    }

    /**
     * Create company with all fields using builder pattern
     */
    private CompanyV2 createCompany(String documentNumber, String name, String email, String description, 
                                    String tenantId, CompanySize size, Industry industry, String website, 
                                    String legalName, NaturalPersonV2 contactPerson, AddressV2 address) {
        return CompanyV2.builder()
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

    /**
     * Update company with all fields
     */
    private Optional<CompanyV2> updateCompany(String id, String documentNumber, String name, String email, 
                                              String description, String tenantId, CompanySize size, Industry industry, 
                                              String website, String legalName, NaturalPersonV2 contactPerson, AddressV2 address) {
        Optional<CompanyV2> optionalCompany = findById(id);
        if (optionalCompany.isPresent()) {
            CompanyV2 company = optionalCompany.get();
            
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
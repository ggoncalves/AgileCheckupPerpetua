package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.CompanyV2;
import com.agilecheckup.persistency.entity.CompanySize;
import com.agilecheckup.persistency.entity.Industry;
import com.agilecheckup.persistency.entity.person.AddressV2;
import com.agilecheckup.persistency.entity.person.Gender;
import com.agilecheckup.persistency.entity.person.GenderPronoun;
import com.agilecheckup.persistency.entity.person.NaturalPersonV2;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.service.CompanyService;
import com.agilecheckup.service.CompanyServiceV2;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
public class CompanyTableRunner implements CrudRunner {

    private CompanyServiceV2 companyServiceV2;
    private CompanyService companyService;
    private final boolean shouldCleanAfterComplete;

    public CompanyTableRunner(boolean shouldCleanAfterComplete) {
        this.shouldCleanAfterComplete = shouldCleanAfterComplete;
    }

    @Override
    public void run() {
        log.info("========================================");
        log.info("Starting CompanyTableRunner - V1 to V2 Migration Test Suite");
        log.info("========================================");
        
        try {
            // 1. Test V2 Creation (New companies)
            log.info("\n1. Testing V2 Company Creation...");
            List<CompanyV2> v2Companies = testV2Creation();
            
            // 2. Test V2 Updates
            log.info("\n2. Testing V2 Company Updates...");
            testV2Updates(v2Companies);
            
            // 3. Fetch all existing companies (should include V1 companies)
            log.info("\n3. Fetching all existing companies (V1 + V2)...");
            testFetchAllCompanies();
            
            // 4. Test V1 to V2 Migration by updating existing V1 companies
            log.info("\n4. Testing V1 to V2 Migration (Update existing companies)...");
            testV1ToV2Migration();
            
            // 5. Test Complex V2 Operations
            log.info("\n5. Testing Complex V2 Operations...");
            testComplexV2Operations();
            
            // 6. Clean up if requested
            if (shouldCleanAfterComplete) {
                log.info("\n6. Cleaning up test companies...");
                cleanupTestCompanies(v2Companies);
            }
            
            log.info("\n========================================");
            log.info("CompanyTableRunner completed successfully!");
            log.info("========================================");
            
        } catch (Exception e) {
            log.error("CompanyTableRunner failed with error: {}", e.getMessage(), e);
            throw e;
        }
    }

    private List<CompanyV2> testV2Creation() {
        List<CompanyV2> companies = new ArrayList<>();
        
        log.info("Creating V2 companies with different field combinations...");
        
        // Test 1: Legacy method (basic fields only)
        try {
            log.info("Test 1: Creating company with legacy method (basic fields)");
            Optional<CompanyV2> company1 = getCompanyService().create(
                "12345678000199", 
                "Legacy Test Company V2", 
                "legacy@testcompanyv2.com", 
                "A legacy test company created via V2 service", 
                "test-tenant-v2-123"
            );
            
            if (company1.isPresent()) {
                companies.add(company1.get());
                log.info("✓ Created legacy company: {} (ID: {})", 
                    company1.get().getName(), company1.get().getId());
            } else {
                log.error("✗ Failed to create legacy company");
            }
        } catch (Exception e) {
            log.error("✗ Error creating legacy company: {}", e.getMessage(), e);
        }
        
        // Test 2: Modern method (with size and industry)
        try {
            log.info("Test 2: Creating company with size and industry");
            Optional<CompanyV2> company2 = getCompanyService().create(
                "98765432000188",
                "Modern Tech Startup V2",
                "contact@moderntechv2.com",
                "A modern technology startup with V2 features",
                "test-tenant-v2-123",
                CompanySize.STARTUP,
                Industry.TECHNOLOGY
            );
            
            if (company2.isPresent()) {
                companies.add(company2.get());
                log.info("✓ Created modern company: {} (ID: {}, Size: {}, Industry: {})", 
                    company2.get().getName(), company2.get().getId(),
                    company2.get().getSize(), company2.get().getIndustry());
            } else {
                log.error("✗ Failed to create modern company");
            }
        } catch (Exception e) {
            log.error("✗ Error creating modern company: {}", e.getMessage(), e);
        }
        
        // Test 3: Full method (all fields including contact person and address)
        try {
            log.info("Test 3: Creating company with all fields (contact person + address)");
            
            // Create contact person
            NaturalPersonV2 contactPerson = NaturalPersonV2.builder()
                .name("João Silva")
                .email("joao.silva@enterprise.com")
                .phone("+55 11 99999-9999")
                .documentNumber("12345678901")
                .personDocumentType(PersonDocumentType.CPF)
                .aliasName("João")
                .gender(Gender.MALE)
                .genderPronoun(GenderPronoun.HE)
                .build();
            
            // Create address
            AddressV2 address = AddressV2.builder()
                .street("Av. Paulista, 1000")
                .city("São Paulo")
                .state("SP")
                .zipcode("01310-100")
                .country("Brasil")
                .build();
            
            Optional<CompanyV2> company3 = getCompanyService().create(
                "11223344000177",
                "Enterprise Corp V2",
                "info@enterprisev2.com",
                "A large enterprise corporation with full V2 features",
                "test-tenant-v2-456",
                CompanySize.ENTERPRISE,
                Industry.FINANCE,
                "https://www.enterprisev2.com",
                "Enterprise Corporation Legal Ltda.",
                contactPerson,
                address
            );
            
            if (company3.isPresent()) {
                companies.add(company3.get());
                log.info("✓ Created full company: {} (ID: {}, Contact: {}, Address: {})", 
                    company3.get().getName(), company3.get().getId(),
                    company3.get().getContactPerson() != null ? company3.get().getContactPerson().getName() : "None",
                    company3.get().getAddress() != null ? company3.get().getAddress().getCity() : "None");
            } else {
                log.error("✗ Failed to create full company");
            }
        } catch (Exception e) {
            log.error("✗ Error creating full company: {}", e.getMessage(), e);
        }
        
        log.info("V2 Creation test completed. Created {} companies", companies.size());
        return companies;
    }

    private void testV2Updates(List<CompanyV2> companies) {
        if (companies.isEmpty()) {
            log.warn("No companies available for update testing");
            return;
        }
        
        CompanyV2 testCompany = companies.get(0);
        log.info("Testing V2 updates on company: {} (ID: {})", testCompany.getName(), testCompany.getId());
        
        try {
            // Test update with all fields
            log.info("Updating company with new data...");
            
            NaturalPersonV2 newContactPerson = NaturalPersonV2.builder()
                .name("Maria Santos")
                .email("maria.santos@updated.com")
                .phone("+55 11 88888-8888")
                .documentNumber("98765432101")
                .personDocumentType(PersonDocumentType.CPF)
                .aliasName("Maria")
                .gender(Gender.FEMALE)
                .genderPronoun(GenderPronoun.SHE)
                .build();
            
            AddressV2 newAddress = AddressV2.builder()
                .street("Rua Augusta, 500")
                .city("São Paulo")
                .state("SP")
                .zipcode("01305-000")
                .country("Brasil")
                .build();
            
            Optional<CompanyV2> updatedCompany = getCompanyService().update(
                testCompany.getId(),
                testCompany.getDocumentNumber(),
                testCompany.getName() + " - UPDATED",
                "updated@" + testCompany.getEmail(),
                testCompany.getDescription() + " [UPDATED]",
                testCompany.getTenantId(),
                CompanySize.MEDIUM,
                Industry.HEALTHCARE,
                "https://www.updated-company.com",
                "Updated Legal Name Ltda.",
                newContactPerson,
                newAddress
            );
            
            if (updatedCompany.isPresent()) {
                log.info("✓ Successfully updated company: {}", updatedCompany.get().getName());
                log.info("  - Size changed to: {}", updatedCompany.get().getSize());
                log.info("  - Industry changed to: {}", updatedCompany.get().getIndustry());
                log.info("  - Contact person: {}", 
                    updatedCompany.get().getContactPerson() != null ? 
                    updatedCompany.get().getContactPerson().getName() : "None");
            } else {
                log.error("✗ Failed to update company");
            }
            
        } catch (Exception e) {
            log.error("✗ Error updating company: {}", e.getMessage(), e);
        }
    }

    private void testFetchAllCompanies() {
        try {
            log.info("Fetching all companies from database...");
            List<CompanyV2> allCompanies = getCompanyService().findAll();
            log.info("Found {} companies total", allCompanies.size());
            
            int v1Count = 0;
            int v2Count = 0;
            
            for (CompanyV2 company : allCompanies) {
                // Check if company likely came from V1 (missing V2-specific fields)
                boolean isLikelyV1 = (company.getSize() == null && company.getIndustry() == null);
                
                if (isLikelyV1) {
                    v1Count++;
                    log.info("[V1 Origin] Company: {} - {} (Tenant: {})", 
                        company.getId(), 
                        company.getName(),
                        company.getTenantId());
                } else {
                    v2Count++;
                    log.info("[V2 Origin] Company: {} - {} (Size: {}, Industry: {}, Tenant: {})", 
                        company.getId(), 
                        company.getName(),
                        company.getSize(),
                        company.getIndustry(),
                        company.getTenantId());
                }
            }
            
            log.info("Summary: {} V1-origin companies, {} V2-origin companies", v1Count, v2Count);
            
        } catch (Exception e) {
            log.error("✗ Error fetching all companies: {}", e.getMessage(), e);
        }
    }

    private void testV1ToV2Migration() {
        try {
            log.info("Testing V1 to V2 migration by updating existing V1 companies...");
            
            // First, get all companies
            List<CompanyV2> allCompanies = getCompanyService().findAll();
            
            // Find V1-origin companies (those without size/industry)
            List<CompanyV2> v1Companies = allCompanies.stream()
                .filter(company -> company.getSize() == null && company.getIndustry() == null)
                .limit(2) // Test with max 2 companies to avoid too much noise
                .collect(Collectors.toList());
            
            if (v1Companies.isEmpty()) {
                log.info("No V1-origin companies found for migration testing");
                return;
            }
            
            log.info("Found {} V1-origin companies to test migration", v1Companies.size());
            
            for (CompanyV2 v1Company : v1Companies) {
                try {
                    log.info("Migrating V1 company: {} (ID: {})", v1Company.getName(), v1Company.getId());
                    
                    // Update with V2 fields
                    Optional<CompanyV2> migratedCompany = getCompanyService().update(
                        v1Company.getId(),
                        v1Company.getDocumentNumber(),
                        v1Company.getName(),
                        v1Company.getEmail(),
                        v1Company.getDescription(),
                        v1Company.getTenantId(),
                        CompanySize.SMALL, // Add missing V2 field
                        Industry.OTHER     // Add missing V2 field
                    );
                    
                    if (migratedCompany.isPresent()) {
                        log.info("✓ Successfully migrated company: {} - Added Size: {}, Industry: {}", 
                            migratedCompany.get().getName(),
                            migratedCompany.get().getSize(),
                            migratedCompany.get().getIndustry());
                    } else {
                        log.error("✗ Failed to migrate company: {}", v1Company.getName());
                    }
                    
                } catch (Exception e) {
                    log.error("✗ Error migrating company {}: {}", v1Company.getName(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("✗ Error in V1 to V2 migration test: {}", e.getMessage(), e);
        }
    }

    private void testComplexV2Operations() {
        try {
            log.info("Testing complex V2 operations...");
            
            // Test 1: Create and immediately update
            log.info("Test: Create and immediately update");
            Optional<CompanyV2> tempCompany = getCompanyService().create(
                "55544433000122",
                "Temp Complex Test Company",
                "temp@complex.com",
                "Temporary company for complex testing",
                "test-tenant-complex",
                CompanySize.STARTUP,
                Industry.TECHNOLOGY
            );
            
            if (tempCompany.isPresent()) {
                // Immediately update with more complex data
                NaturalPersonV2 complexContact = NaturalPersonV2.builder()
                    .name("Complex Test Contact")
                    .email("complex@test.com")
                    .phone("+55 11 77777-7777")
                    .documentNumber("11111111111")
                    .personDocumentType(PersonDocumentType.CPF)
                    .aliasName("Complex")
                    .gender(Gender.OTHER)
                    .genderPronoun(GenderPronoun.HE)
                    .build();
                
                AddressV2 complexAddress = AddressV2.builder()
                    .street("Complex Street, 123")
                    .city("Complex City")
                    .state("CC")
                    .zipcode("12345-678")
                    .country("Complex Country")
                    .build();
                
                Optional<CompanyV2> complexUpdated = getCompanyService().update(
                    tempCompany.get().getId(),
                    tempCompany.get().getDocumentNumber(),
                    "Complex Updated Company",
                    "complex-updated@test.com",
                    "Updated with complex data structures",
                    tempCompany.get().getTenantId(),
                    CompanySize.LARGE,
                    Industry.FINANCE,
                    "https://www.complex-updated.com",
                    "Complex Updated Legal Name",
                    complexContact,
                    complexAddress
                );
                
                if (complexUpdated.isPresent()) {
                    log.info("✓ Complex operation successful");
                    log.info("  - Contact person with complex data: {}", 
                        complexUpdated.get().getContactPerson().getName());
                    log.info("  - Address with complex data: {}", 
                        complexUpdated.get().getAddress().getStreet());
                    
                    // Clean up this test company
                    getCompanyService().deleteById(tempCompany.get().getId());
                    log.info("✓ Cleaned up complex test company");
                } else {
                    log.error("✗ Complex update failed");
                }
            } else {
                log.error("✗ Failed to create temp company for complex testing");
            }
            
        } catch (Exception e) {
            log.error("✗ Error in complex V2 operations: {}", e.getMessage(), e);
        }
    }

    private void cleanupTestCompanies(List<CompanyV2> companies) {
        log.info("Cleaning up {} test companies...", companies.size());
        
        for (CompanyV2 company : companies) {
            try {
                boolean deleted = getCompanyService().deleteById(company.getId());
                if (deleted) {
                    log.info("✓ Deleted test company: {}", company.getName());
                } else {
                    log.warn("✗ Failed to delete test company: {}", company.getName());
                }
            } catch (Exception e) {
                log.error("✗ Error deleting test company {}: {}", company.getName(), e.getMessage());
            }
        }
    }

    private CompanyServiceV2 getCompanyService() {
        if (companyServiceV2 == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            companyServiceV2 = serviceComponent.buildCompanyService();
        }
        return companyServiceV2;
    }

    private CompanyService getCompanyServiceLegacy() {
        if (companyService == null) {
            ServiceComponent serviceComponent = DaggerServiceComponent.create();
            companyService = serviceComponent.buildCompanyServiceLegacy();
        }
        return companyService;
    }
}
package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.CompanyV2;
import com.agilecheckup.persistency.entity.CompanySize;
import com.agilecheckup.persistency.entity.Industry;
import com.agilecheckup.persistency.entity.person.AddressV2;
import com.agilecheckup.persistency.entity.person.NaturalPersonV2;
import com.agilecheckup.persistency.repository.CompanyRepositoryV2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactoryV2.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest extends AbstractCrudServiceTestV2<CompanyV2, CompanyRepositoryV2> {

    @InjectMocks
    @Spy
    private CompanyService companyService;

    @Mock
    private CompanyRepositoryV2 companyRepository;

    private final CompanyV2 originalCompany = createMockedCompanyV2(DEFAULT_ID);

    @Test
    void createLegacy_shouldSucceedWithBasicFields() {
        // Prepare
        CompanyV2 expectedCompany = CompanyV2.builder()
            .name(DEFAULT_COMPANY_NAME)
            .email(DEFAULT_COMPANY_EMAIL)
            .description("Company description")
            .tenantId(GENERIC_TENANT_ID)
            .documentNumber(DEFAULT_COMPANY_DOCUMENT)
            .build();
        CompanyV2 savedCompany = copyCompanyV2AndAddId(expectedCompany, DEFAULT_ID);

        // Mock
        doReturn(Optional.of(savedCompany)).when(companyRepository).save(any(CompanyV2.class));

        // When
        Optional<CompanyV2> result = companyService.create(
            DEFAULT_COMPANY_DOCUMENT,
            DEFAULT_COMPANY_NAME,
            DEFAULT_COMPANY_EMAIL,
            "Company description",
            GENERIC_TENANT_ID
        );

        // Then
        assertTrue(result.isPresent());
        assertEquals(savedCompany, result.get());
        verify(companyRepository).save(any(CompanyV2.class));
    }

    @Test
    void createLegacy_nullName_shouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> companyService.create(
            DEFAULT_COMPANY_DOCUMENT,
            null,
            DEFAULT_COMPANY_EMAIL,
            "Company description",
            GENERIC_TENANT_ID
        ));
    }

    @Test
    void createWithRequiredFields_shouldSucceed() {
        // Prepare
        CompanyV2 expectedCompany = createMockedCompanyV2(
            "Tech Company",
            "contact@tech.com",
            "A technology company",
            "tenant123",
            "12345678000123"
        );
        expectedCompany.setSize(CompanySize.STARTUP);
        expectedCompany.setIndustry(Industry.TECHNOLOGY);
        CompanyV2 savedCompany = copyCompanyV2AndAddId(expectedCompany, DEFAULT_ID);

        // Mock
        doReturn(Optional.of(savedCompany)).when(companyRepository).save(any(CompanyV2.class));

        // When
        Optional<CompanyV2> result = companyService.create(
            "12345678000123",
            "Tech Company",
            "contact@tech.com",
            "A technology company",
            "tenant123",
            CompanySize.STARTUP,
            Industry.TECHNOLOGY
        );

        // Then
        assertTrue(result.isPresent());
        assertThat(result.get().getSize()).isEqualTo(CompanySize.STARTUP);
        assertThat(result.get().getIndustry()).isEqualTo(Industry.TECHNOLOGY);
        verify(companyRepository).save(any(CompanyV2.class));
    }

    @Test
    void createWithAllFields_shouldSucceed() {
        // Prepare
        NaturalPersonV2 contactPerson = createMockedNaturalPersonV2("John Doe");
        AddressV2 address = createMockedAddressV2();

        CompanyV2 expectedCompany = CompanyV2.builder()
            .name("Full Company")
            .email("contact@full.com")
            .description("Complete company")
            .tenantId("tenant123")
            .documentNumber("98765432000198")
            .size(CompanySize.LARGE)
            .industry(Industry.FINANCE)
            .website("https://www.full.com")
            .legalName("Full Company Legal Inc.")
            .contactPerson(contactPerson)
            .address(address)
            .build();
        CompanyV2 savedCompany = copyCompanyV2AndAddId(expectedCompany, DEFAULT_ID);

        // Mock
        doReturn(Optional.of(savedCompany)).when(companyRepository).save(any(CompanyV2.class));

        // When
        Optional<CompanyV2> result = companyService.create(
            "98765432000198",
            "Full Company",
            "contact@full.com",
            "Complete company",
            "tenant123",
            CompanySize.LARGE,
            Industry.FINANCE,
            "https://www.full.com",
            "Full Company Legal Inc.",
            contactPerson,
            address
        );

        // Then
        assertTrue(result.isPresent());
        assertThat(result.get())
            .satisfies(company -> {
                assertThat(company.getSize()).isEqualTo(CompanySize.LARGE);
                assertThat(company.getIndustry()).isEqualTo(Industry.FINANCE);
                assertThat(company.getWebsite()).isEqualTo("https://www.full.com");
                assertThat(company.getLegalName()).isEqualTo("Full Company Legal Inc.");
                assertThat(company.getContactPerson()).isEqualTo(contactPerson);
                assertThat(company.getAddress()).isEqualTo(address);
            });
        verify(companyRepository).save(any(CompanyV2.class));
    }

    @Test
    void updateLegacy_existingCompany_shouldSucceed() {
        // Prepare
        CompanyV2 existingCompany = createMockedCompanyV2(DEFAULT_ID);
        CompanyV2 updatedCompany = createMockedCompanyV2(DEFAULT_ID);
        updatedCompany.setName("Updated Company Name");
        updatedCompany.setEmail("updated.email@example.com");
        updatedCompany.setDescription("Updated Description");
        updatedCompany.setDocumentNumber("0002");
        updatedCompany.setTenantId("Updated Tenant Id");

        // Mock
        when(companyRepository.findById(DEFAULT_ID)).thenReturn(Optional.of(existingCompany));
        doReturn(Optional.of(updatedCompany)).when(companyRepository).save(any(CompanyV2.class));

        // When
        Optional<CompanyV2> result = companyService.update(
            DEFAULT_ID,
            "0002",
            "Updated Company Name",
            "updated.email@example.com",
            "Updated Description",
            "Updated Tenant Id"
        );

        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedCompany, result.get());
        verify(companyRepository).findById(DEFAULT_ID);
        verify(companyRepository).save(any(CompanyV2.class));
    }

    @Test
    void updateLegacy_nonExistingCompany_shouldReturnEmpty() {
        // Prepare
        String nonExistingId = "nonExistingId";

        // Mock
        when(companyRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // When
        Optional<CompanyV2> result = companyService.update(
            nonExistingId,
            "doc",
            "name",
            "email",
            "desc",
            "tenant"
        );

        // Then
        assertTrue(result.isEmpty());
        verify(companyRepository).findById(nonExistingId);
        verify(companyRepository, never()).save(any(CompanyV2.class));
    }

    @Test
    void updateWithAllFields_existingCompany_shouldSucceed() {
        // Prepare
        CompanyV2 existingCompany = createMockedCompanyV2(DEFAULT_ID);
        NaturalPersonV2 newContactPerson = createMockedNaturalPersonV2("Jane Smith");
        AddressV2 newAddress = AddressV2.builder()
            .street("456 New Street")
            .city("Rio de Janeiro")
            .state("RJ")
            .zipcode("98765-432")
            .country("Brazil")
            .build();

        CompanyV2 updatedCompany = CompanyV2.builder()
            .id(DEFAULT_ID)
            .name("Updated Full Company")
            .email("updated@full.com")
            .description("Updated complete company")
            .tenantId("tenant789")
            .documentNumber("22222222000222")
            .size(CompanySize.ENTERPRISE)
            .industry(Industry.MANUFACTURING)
            .website("https://www.updated-full.com")
            .legalName("Updated Full Company Legal Inc.")
            .contactPerson(newContactPerson)
            .address(newAddress)
            .build();

        // Mock
        when(companyRepository.findById(DEFAULT_ID)).thenReturn(Optional.of(existingCompany));
        doReturn(Optional.of(updatedCompany)).when(companyRepository).save(any(CompanyV2.class));

        // When
        Optional<CompanyV2> result = companyService.update(
            DEFAULT_ID,
            "22222222000222",
            "Updated Full Company",
            "updated@full.com",
            "Updated complete company",
            "tenant789",
            CompanySize.ENTERPRISE,
            Industry.MANUFACTURING,
            "https://www.updated-full.com",
            "Updated Full Company Legal Inc.",
            newContactPerson,
            newAddress
        );

        // Then
        assertTrue(result.isPresent());
        assertEquals(updatedCompany, result.get());
        verify(companyRepository).findById(DEFAULT_ID);
        verify(companyRepository).save(any(CompanyV2.class));
    }


    @Test
    void createWithOptionalNullFields_shouldSucceed() {
        // Prepare
        CompanyV2 expectedCompany = CompanyV2.builder()
            .name("Minimal Company")
            .email("minimal@company.com")
            .description("Minimal company description")
            .tenantId("tenant123")
            .documentNumber("33333333000333")
            .size(CompanySize.SMALL)
            .industry(Industry.OTHER)
            .website(null)
            .legalName(null)
            .contactPerson(null)
            .address(null)
            .build();
        CompanyV2 savedCompany = copyCompanyV2AndAddId(expectedCompany, DEFAULT_ID);

        // Mock
        doReturn(Optional.of(savedCompany)).when(companyRepository).save(any(CompanyV2.class));

        // When
        Optional<CompanyV2> result = companyService.create(
            "33333333000333",
            "Minimal Company",
            "minimal@company.com",
            "Minimal company description",
            "tenant123",
            CompanySize.SMALL,
            Industry.OTHER,
            null,
            null,
            null,
            null
        );

        // Then
        assertTrue(result.isPresent());
        assertThat(result.get())
            .satisfies(company -> {
                assertThat(company.getWebsite()).isNull();
                assertThat(company.getLegalName()).isNull();
                assertThat(company.getContactPerson()).isNull();
                assertThat(company.getAddress()).isNull();
            });
        verify(companyRepository).save(any(CompanyV2.class));
    }
}
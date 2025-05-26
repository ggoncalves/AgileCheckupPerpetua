package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.CompanySize;
import com.agilecheckup.persistency.entity.Industry;
import com.agilecheckup.persistency.entity.person.Address;
import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.copyCompanyAndAddId;
import static com.agilecheckup.util.TestObjectFactory.createMockedAddress;
import static com.agilecheckup.util.TestObjectFactory.createMockedCompany;
import static com.agilecheckup.util.TestObjectFactory.createMockedNaturalPerson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest extends AbstractCrudServiceTest<Company, AbstractCrudRepository<Company>> {

  @InjectMocks
  @Spy
  private CompanyService companyService;

  @Mock
  private CompanyRepository companyRepository;

  private final Company originalCompany = createMockedCompany(DEFAULT_ID);

  @Test
  void create() {
    // Create a legacy company with only basic fields
    Company legacyCompany = Company.builder()
        .personDocumentType(PersonDocumentType.CNPJ)
        .documentNumber(originalCompany.getDocumentNumber())
        .name(originalCompany.getName())
        .email(originalCompany.getEmail())
        .description(originalCompany.getDescription())
        .tenantId(originalCompany.getTenantId())
        .build();
    Company savedCompany = copyCompanyAndAddId(legacyCompany, DEFAULT_ID);

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedCompany, companyRepository);

    // When
    Optional<Company> companyOptional = companyService.create(
        originalCompany.getDocumentNumber(),
        originalCompany.getName(),
        originalCompany.getEmail(),
        originalCompany.getDescription(),
        originalCompany.getTenantId()
    );

    // Then
    assertTrue(companyOptional.isPresent());
    assertEquals(savedCompany, companyOptional.get());
    verify(companyRepository).save(any(Company.class));
    verify(companyService).create("0001",
        "Company Name",
        "company@email.com",
        "Company description",
        "Random Tenant Id");
  }

  @Test
  void create_NullCompany() {
    // When
    assertThrows(NullPointerException.class, () -> companyService.create(
        originalCompany.getDocumentNumber(),
        null,
        originalCompany.getEmail(),
        originalCompany.getDescription(),
        originalCompany.getTenantId()));
  }

  @Test
  void update_existingCompany_shouldSucceed() {
    // Prepare
    Company existingCompany = createMockedCompany(DEFAULT_ID);
    Company updatedCompanyDetails = createMockedCompany(DEFAULT_ID);
    updatedCompanyDetails.setName("Updated Company Name");
    updatedCompanyDetails.setEmail("updated.email@example.com");
    updatedCompanyDetails.setDescription("Updated Description");
    updatedCompanyDetails.setDocumentNumber("0002");
    updatedCompanyDetails.setTenantId("Updated Tenant Id");
    // Keep all the new fields from the existing company since legacy update preserves them
    updatedCompanyDetails.setSize(existingCompany.getSize());
    updatedCompanyDetails.setIndustry(existingCompany.getIndustry());
    updatedCompanyDetails.setWebsite(existingCompany.getWebsite());
    updatedCompanyDetails.setLegalName(existingCompany.getLegalName());
    updatedCompanyDetails.setContactPerson(existingCompany.getContactPerson());
    updatedCompanyDetails.setAddress(existingCompany.getAddress());

    // Mock repository calls
    when(companyRepository.findById(DEFAULT_ID)).thenReturn(existingCompany);
    doAnswerForUpdate(updatedCompanyDetails, companyRepository);

    // When
    Optional<Company> resultOptional = companyService.update(
        DEFAULT_ID,
        updatedCompanyDetails.getDocumentNumber(),
        updatedCompanyDetails.getName(),
        updatedCompanyDetails.getEmail(),
        updatedCompanyDetails.getDescription(),
        updatedCompanyDetails.getTenantId()
    );

    // Then
    assertTrue(resultOptional.isPresent());
    assertEquals(updatedCompanyDetails, resultOptional.get());
    verify(companyRepository).findById(DEFAULT_ID);
    verify(companyRepository).save(any(Company.class));
    verify(companyService).update(DEFAULT_ID,
        "0002",
        "Updated Company Name",
        "updated.email@example.com",
        "Updated Description",
        "Updated Tenant Id");
  }

  @Test
  void update_nonExistingCompany_shouldReturnEmpty() {
    // Prepare
    String nonExistingId = "nonExistingId";

    // Mock repository calls
    when(companyRepository.findById(nonExistingId)).thenReturn(null);

    // When
    Optional<Company> resultOptional = companyService.update(
        nonExistingId,
        "doc",
        "name",
        "email",
        "desc",
        "tenant"
    );

    // Then
    assertTrue(resultOptional.isEmpty());
    verify(companyRepository).findById(nonExistingId);
    verify(companyRepository, never()).save(any(Company.class));
    verify(companyService).update(nonExistingId, "doc", "name", "email", "desc", "tenant");
  }

  @Test
  void createWithRequiredFields_shouldSucceed() {
    // Prepare
    Company expectedCompany = Company.builder()
        .name("Tech Company")
        .description("A technology company")
        .tenantId("tenant123")
        .email("contact@tech.com")
        .documentNumber("12345678000123")
        .size(CompanySize.STARTUP)
        .industry(Industry.TECHNOLOGY)
        .build();
    Company savedCompany = copyCompanyAndAddId(expectedCompany, DEFAULT_ID);

    // Mock
    doAnswerForSaveWithRandomEntityId(savedCompany, companyRepository);

    // When
    Optional<Company> result = companyService.create(
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
    assertEquals(savedCompany, result.get());
    verify(companyRepository).save(any(Company.class));
  }

  @Test
  void createWithAllFields_shouldSucceed() {
    // Prepare
    NaturalPerson contactPerson = createMockedNaturalPerson("John Doe");
    Address address = createMockedAddress();
    
    Company expectedCompany = Company.builder()
        .name("Full Company")
        .description("Complete company")
        .tenantId("tenant123")
        .email("contact@full.com")
        .documentNumber("98765432000198")
        .size(CompanySize.LARGE)
        .industry(Industry.FINANCE)
        .website("https://www.full.com")
        .legalName("Full Company Legal Inc.")
        .contactPerson(contactPerson)
        .address(address)
        .build();
    Company savedCompany = copyCompanyAndAddId(expectedCompany, DEFAULT_ID);

    // Mock
    doAnswerForSaveWithRandomEntityId(savedCompany, companyRepository);

    // When
    Optional<Company> result = companyService.create(
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
    assertEquals(savedCompany, result.get());
    verify(companyRepository).save(any(Company.class));
  }

  @Test
  void createWithRequiredFields_nullSize_shouldThrowException() {
    // When & Then
    assertThrows(NullPointerException.class, () -> companyService.create(
        "12345678000123",
        "Tech Company",
        "contact@tech.com",
        "A technology company",
        "tenant123",
        null,
        Industry.TECHNOLOGY
    ));
  }

  @Test
  void createWithRequiredFields_nullIndustry_shouldThrowException() {
    // When & Then
    assertThrows(NullPointerException.class, () -> companyService.create(
        "12345678000123",
        "Tech Company", 
        "contact@tech.com",
        "A technology company",
        "tenant123",
        CompanySize.STARTUP,
        null
    ));
  }

  @Test
  void updateWithRequiredFields_existingCompany_shouldSucceed() {
    // Prepare
    Company existingCompany = createMockedCompany(DEFAULT_ID);
    Company updatedCompany = Company.builder()
        .id(DEFAULT_ID)
        .name("Updated Tech Company")
        .description("Updated description")
        .tenantId("tenant456")
        .email("updated@tech.com")
        .documentNumber("11111111000111")
        .size(CompanySize.MEDIUM)
        .industry(Industry.HEALTHCARE)
        .build();

    // Mock
    when(companyRepository.findById(DEFAULT_ID)).thenReturn(existingCompany);
    doAnswerForUpdate(updatedCompany, companyRepository);

    // When
    Optional<Company> result = companyService.update(
        DEFAULT_ID,
        "11111111000111",
        "Updated Tech Company",
        "updated@tech.com",
        "Updated description",
        "tenant456",
        CompanySize.MEDIUM,
        Industry.HEALTHCARE
    );

    // Then
    assertTrue(result.isPresent());
    assertEquals(updatedCompany, result.get());
    verify(companyRepository).findById(DEFAULT_ID);
    verify(companyRepository).save(any(Company.class));
  }

  @Test
  void updateWithAllFields_existingCompany_shouldSucceed() {
    // Prepare
    Company existingCompany = createMockedCompany(DEFAULT_ID);
    NaturalPerson newContactPerson = createMockedNaturalPerson("Jane Smith");
    Address newAddress = Address.builder()
        .street("456 New Street")
        .city("Rio de Janeiro")
        .state("RJ")
        .zipcode("98765-432")
        .country("Brazil")
        .build();
        
    Company updatedCompany = Company.builder()
        .id(DEFAULT_ID)
        .name("Updated Full Company")
        .description("Updated complete company")
        .tenantId("tenant789")
        .email("updated@full.com")
        .documentNumber("22222222000222")
        .size(CompanySize.ENTERPRISE)
        .industry(Industry.MANUFACTURING)
        .website("https://www.updated-full.com")
        .legalName("Updated Full Company Legal Inc.")
        .contactPerson(newContactPerson)
        .address(newAddress)
        .build();

    // Mock
    when(companyRepository.findById(DEFAULT_ID)).thenReturn(existingCompany);
    doAnswerForUpdate(updatedCompany, companyRepository);

    // When
    Optional<Company> result = companyService.update(
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
    verify(companyRepository).save(any(Company.class));
  }

  @Test
  void updateWithRequiredFields_nonExistingCompany_shouldReturnEmpty() {
    // Prepare
    String nonExistingId = "nonExistingId";

    // Mock
    when(companyRepository.findById(nonExistingId)).thenReturn(null);

    // When
    Optional<Company> result = companyService.update(
        nonExistingId,
        "11111111000111",
        "Updated Tech Company",
        "updated@tech.com",
        "Updated description",
        "tenant456",
        CompanySize.MEDIUM,
        Industry.HEALTHCARE
    );

    // Then
    assertTrue(result.isEmpty());
    verify(companyRepository).findById(nonExistingId);
    verify(companyRepository, never()).save(any(Company.class));
  }

  @Test
  void createWithOptionalNullFields_shouldSucceed() {
    // Prepare
    Company expectedCompany = Company.builder()
        .name("Minimal Company")
        .description("Minimal company description")
        .tenantId("tenant123")
        .email("minimal@company.com")
        .documentNumber("33333333000333")
        .size(CompanySize.SMALL)
        .industry(Industry.OTHER)
        .website(null)
        .legalName(null)
        .contactPerson(null)
        .address(null)
        .build();
    Company savedCompany = copyCompanyAndAddId(expectedCompany, DEFAULT_ID);

    // Mock
    doAnswerForSaveWithRandomEntityId(savedCompany, companyRepository);

    // When
    Optional<Company> result = companyService.create(
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
    assertEquals(savedCompany, result.get());
    verify(companyRepository).save(any(Company.class));
  }
}
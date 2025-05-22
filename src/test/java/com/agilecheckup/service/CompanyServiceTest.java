package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
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
import static com.agilecheckup.util.TestObjectFactory.createMockedCompany;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

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
    Company savedCompany = copyCompanyAndAddId(originalCompany, DEFAULT_ID);

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
    verify(companyRepository).save(savedCompany);
    verify(companyService).create("0001", "Company Name", "company@email.com", "Company description", "Random Tenant Id");
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


    // Mock repository calls
    when(companyRepository.findById(DEFAULT_ID)).thenReturn(Optional.of(existingCompany));
    doAnswerForUpdate(updatedCompanyDetails, companyRepository); // This mocks save indirectly

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
    verify(companyRepository).save(updatedCompanyDetails); // or any(Company.class)
    verify(companyService).update(DEFAULT_ID, "0002", "Updated Company Name", "updated.email@example.com", "Updated Description", "Updated Tenant Id");
  }

  @Test
  void update_nonExistingCompany_shouldReturnEmpty() {
    // Prepare
    String nonExistingId = "nonExistingId";

    // Mock repository calls
    when(companyRepository.findById(nonExistingId)).thenReturn(Optional.empty());

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
}
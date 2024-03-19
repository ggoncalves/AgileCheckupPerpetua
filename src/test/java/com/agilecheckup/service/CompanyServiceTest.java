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
}
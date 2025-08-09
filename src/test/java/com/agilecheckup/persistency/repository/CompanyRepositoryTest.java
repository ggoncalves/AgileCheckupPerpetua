package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.createMockedCompanyV2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompanyRepositoryTest extends AbstractRepositoryTest<Company> {

  private CompanyRepository companyRepository;

  @Override
  protected Class<Company> getEntityClass() {
    return Company.class;
  }

  @Override
  protected String getTableName() {
    return "Company";
  }

  @BeforeEach
  void setUp() {
    super.setUp();
    companyRepository = new CompanyRepository(mockEnhancedClient);
  }
  
  @Test
  @DisplayName("Should save company successfully")
  void shouldSaveCompanySuccessfully() {
    Company company = createMockedCompanyV2();
    mockTablePutItem(company);
    
    Optional<Company> result = companyRepository.save(company);
    
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(company);
    assertThat(result.get().getId()).isNotNull();
    verify(mockTable).putItem(company);
  }
  
  @Test
  @DisplayName("Should find company by id successfully")
  void shouldFindCompanyByIdSuccessfully() {
    Company company = createMockedCompanyV2("test-id");
    mockTableGetItem(company);
    
    Optional<Company> result = companyRepository.findById("test-id");
    
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(company);
    verify(mockTable).getItem(any(Key.class));
  }
  
  @Test
  @DisplayName("Should return empty when company not found")
  void shouldReturnEmptyWhenCompanyNotFound() {
    mockTableGetItemNull();
    
    Optional<Company> result = companyRepository.findById("non-existent-id");
    
    assertThat(result).isEmpty();
    verify(mockTable).getItem(any(Key.class));
  }
  
  @Test
  @DisplayName("Should delete company by id successfully")
  void shouldDeleteCompanyByIdSuccessfully() {
    boolean result = companyRepository.deleteById("test-id");
    
    assertThat(result).isTrue();
    verify(mockTable).deleteItem(any(Key.class));
  }
  
  @Test
  @DisplayName("Should check if company exists by id")
  void shouldCheckIfCompanyExistsById() {
    Company company = createMockedCompanyV2("test-id");
    mockTableGetItem(company);
    
    boolean result = companyRepository.existsById("test-id");
    
    assertThat(result).isTrue();
    verify(mockTable).getItem(any(Key.class));
  }
}
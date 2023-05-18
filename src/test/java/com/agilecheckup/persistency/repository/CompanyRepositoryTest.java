package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.Company;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.agilecheckup.util.TestObjectFactory.createMockedCompany;

@ExtendWith(MockitoExtension.class)
class CompanyRepositoryTest extends AbstractRepositoryTest<Company> {

  @InjectMocks
  @Spy
  private CompanyRepository companyRepository;

  @Override
  AbstractCrudRepository getRepository() {
    return companyRepository;
  }

  @Override
  Company createMockedT() {
    return createMockedCompany();
  }
}
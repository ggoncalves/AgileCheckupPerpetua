package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.CompanyService;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public class CompanyTableRunner extends AbstractEntityCrudRunner<Company> {

  private CompanyService companyService;

  public CompanyTableRunner(boolean shouldCleanAfterComplete) {
    super(shouldCleanAfterComplete);
  }

  @Override
  protected Collection<Supplier<Optional<Company>>> getCreateSupplier() {
    Collection<Supplier<Optional<Company>>> collection = new ArrayList<>();
    collection.add(() -> getCompanyService().create("0001", "CompanyName", "company@email.com", "Company Description", "Another TenantId"));
    return collection;
  }

  @Override
  protected AbstractCrudService<Company, AbstractCrudRepository<Company>> getCrudService() {
    return getCompanyService();
  }

  @Override
  protected void verifySavedEntity(Company savedEntity, Company fetchedEntity) {
    // Do nothing
  }

  private CompanyService getCompanyService() {
    if (companyService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      companyService = serviceComponent.buildCompanyService();
    }
    return companyService;
  }
}

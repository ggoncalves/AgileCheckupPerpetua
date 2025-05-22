package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.PerformanceCycleService;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public class PerformanceCycleTableRunner extends AbstractEntityCrudRunner<PerformanceCycle> {

  private PerformanceCycleService performanceCycleService;

  public PerformanceCycleTableRunner(boolean shouldCleanAfterComplete) {
    super(shouldCleanAfterComplete);
  }

  @Override
  protected Collection<Supplier<Optional<PerformanceCycle>>> getCreateSupplier() {
    Collection<Supplier<Optional<PerformanceCycle>>> collection = new ArrayList<>();
    collection.add(() -> getPerformanceCycleService().create(
        "PerformanceCycleName",
        "PerformanceCycle Description",
        "Another TenantId",
        "ca93d066-62af-4a49-aa46-0dd7f33ba9bc",
        true,
        false
    ));
    return collection;
  }

  @Override
  protected AbstractCrudService<PerformanceCycle, AbstractCrudRepository<PerformanceCycle>> getCrudService() {
    return getPerformanceCycleService();
  }

  @Override
  protected void verifySavedEntity(PerformanceCycle savedEntity, PerformanceCycle fetchedEntity) {
    // Do nothing
  }

  private PerformanceCycleService getPerformanceCycleService() {
    if (performanceCycleService == null) {
      ServiceComponent serviceComponent = DaggerServiceComponent.create();
      performanceCycleService = serviceComponent.buildPerformanceCycleService();
    }
    return performanceCycleService;
  }
}

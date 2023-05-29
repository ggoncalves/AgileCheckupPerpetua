package com.agilecheckup.main.runner;

import com.agilecheckup.dagger.component.DaggerServiceComponent;
import com.agilecheckup.dagger.component.ServiceComponent;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.agilecheckup.service.PerformanceCycleService;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public class PerformanceCycleTableRunner extends AbstractCommandRunner<PerformanceCycle> {

  private PerformanceCycleService performanceCycleService;

  public PerformanceCycleTableRunner(boolean mustDelete) {
    super(mustDelete);
  }

  @Override
  protected Supplier<Optional<PerformanceCycle>> getCreateSupplier() {
    return () -> getPerformanceCycleService().create(
        "PerformanceCycleName",
        "PerformanceCycle Description",
        "Another TenantId",
        "19bcdfcb-9162-4a0b-a5c3-c034702d0961",
        true,
        false
    );
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

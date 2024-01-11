package com.agilecheckup.main.runner;

import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public abstract class AbstractCrudRunner<T extends BaseEntity> implements CrudRunner {

  private final boolean shouldCleanAfterComplete;

  protected AbstractCrudRunner(boolean shouldCleanAfterComplete) {
    this.shouldCleanAfterComplete = shouldCleanAfterComplete;
  }

  protected AbstractCrudRunner() {
    this.shouldCleanAfterComplete = true;
  }

  @Override
  public void run() {
    Collection<T> entities = create();
    fetch(entities);
    invokeListAll();
    if (shouldCleanAfterComplete) delete(entities);
  }

  protected void invokeListAll() {
    PaginatedScanList<T> list = getCrudService().findAll();
    log.info("All the Entries");
    list.forEach(log::info);
  }

  protected abstract Collection<Supplier<Optional<T>>> getCreateSupplier();

  protected abstract AbstractCrudService<T, AbstractCrudRepository<T>> getCrudService();

  protected abstract void verifySavedEntity(T savedEntity, T fetchedEntity);

  protected abstract Collection<T> create();

  protected abstract void fetch(Collection<T> entities);

  protected abstract void delete(Collection<T> entities);

}

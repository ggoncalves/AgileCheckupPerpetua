package com.agilecheckup.main.runner;

import com.agilecheckup.persistency.entity.base.AuditableEntity;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.service.AbstractCrudService;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;
import java.util.function.Supplier;

@Log4j2
public abstract class AbstractCommandRunner<T extends AuditableEntity> implements CommandTableRunner {

  private final boolean mustDelete;

  protected AbstractCommandRunner(boolean mustDelete) {
    this.mustDelete = mustDelete;
  }

  protected AbstractCommandRunner() {
    this.mustDelete = true;
  }

  protected abstract Supplier<Optional<T>> getCreateSupplier();

  protected abstract AbstractCrudService<T, AbstractCrudRepository<T>> getCrudService();

  protected abstract void verifySavedEntity(T savedEntity, T fetchedEntity);

  @Override
  public void run() {
    T createdEntity = createEntity();
    listAll();
    fetchEntity(createdEntity);
    deleteEntity(createdEntity);
  }

  private T createEntity() {
    Optional<T> createdEntity = getCreateSupplier().get();
    log.info("Created: " + createdEntity.get());
    return createdEntity.get();
  }

  private void listAll() {
    PaginatedScanList<T> list = getCrudService().findAll();
    log.info("All the Entries");
    list.forEach(log::info);
  }

  private void fetchEntity(T createdEntity) {
    log.info("Fetching: " + createdEntity.getId() + " from " + createdEntity.getClass());
    T fetchedEntity = getCrudService().findById(createdEntity.getId()).get();
    log.info("IsEquals = " + createdEntity.equals(fetchedEntity));
    log.info("Is Date Okay? " + createdEntity.getCreatedDate() + " and " + fetchedEntity.getLastUpdatedDate());
    verifySavedEntity(createdEntity, fetchedEntity);
  }

  private void deleteEntity(T createdEntity) {
    if (mustDelete) {
      log.info("Deleting: " + createdEntity.getClass() + " - " + createdEntity.getId());
      getCrudService().delete(createdEntity);
    }
  }

}

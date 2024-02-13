package com.agilecheckup.main.runner;

import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Log4j2
public abstract class AbstractEntityCrudRunner<T extends BaseEntity> extends AbstractCrudRunner<T> {

  public AbstractEntityCrudRunner(boolean shouldCleanAfterComplete) {
    super(shouldCleanAfterComplete);
  }

  protected AbstractEntityCrudRunner() {
    super(true);
  }

  protected void invokeListAll() {
    PaginatedScanList<T> list = getCrudService().findAll();
    log.info("All the Entries");
    list.forEach(log::info);
  }

  @Override
  protected Collection<T> create() {
    Collection<T> entities = new ArrayList<>();
    getCreateSupplier().forEach(supplier -> {
      Optional<T> createdEntity = supplier.get();
      log.info("Created: " + createdEntity.get());
      entities.add(createdEntity.get());
    });
    return entities;
  }

  @Override
  protected void fetch(Collection<T> entities) {
    entities.forEach(entity -> {
      log.info("Fetching: " + entity.getId() + " from " + entity.getClass());
      T fetchedEntity = getCrudService().findById(entity.getId()).get();
      log.info("IsEquals = " + entity.equals(fetchedEntity));
      verifySavedEntity(entity, fetchedEntity);
    });
  }

  @Override
  protected void delete(Collection<T> entities) {
    entities.forEach(entity -> {
      log.info("Deleting: " + entity.getClass() + " - " + entity.getId());
      getCrudService().delete(entity);
    });
  }

  @Override
  protected void postCreate(Collection<T> entities) {

  }

  @Override
  protected void deleteDependencies() {

  }
}

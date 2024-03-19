package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@Log4j2
public abstract class AbstractCrudService<T extends BaseEntity, V extends AbstractCrudRepository<T>> {

  abstract V getRepository();

  public Optional<T> create(T t) {
    T saved = getRepository().save(t);
    internalPostCreate(saved);
    return Optional.of(saved);
  }

  public void internalPostCreate(T saved) {
    log.info("Entity created successfully: " + saved);
    postCreate(saved);
  }

  public void postCreate(T saved) {
    // Do nothing, must be override
  }

  public PaginatedScanList<T> findAll() {
    return getRepository().findAll();
  }

  public Optional<T> findById(String id) {
    return Optional.ofNullable(getRepository().findById(id));
  }

  public Optional<T> fetchAndCompare(String id, T t) {
    log.debug("Fetching " + id);
    Optional<T> fetched = findById(id);
    assert t.equals(fetched);
    return fetched;
  }

  public void delete(T t) {
    getRepository().delete(t);
  }
}

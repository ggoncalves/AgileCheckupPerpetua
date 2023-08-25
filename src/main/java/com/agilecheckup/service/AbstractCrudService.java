package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@Log4j2
public abstract class AbstractCrudService<T extends BaseEntity, V extends AbstractCrudRepository<T>> {

  private String fixedId;

  abstract V getRepository();

  public Optional<T> create(T t) {
    T saved = getRepository().save(t);
    log.debug("Saved " + saved);
    return Optional.of(saved);
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

  @VisibleForTesting
  void setFixedId(final String fixedId) {
    this.fixedId = fixedId;
  }

  @VisibleForTesting
  void unsetFixedId() {
    this.fixedId = null;
  }

  private String getFixedId() {
    return this.fixedId;
  }

  // TODO: Remove me ASAP
  T setFixedIdIfConfigured(T baseEntity) {
    if (hasFixedId()) {
      baseEntity.setId(getFixedId());
    }
    return baseEntity;
  }

  private boolean hasFixedId() {
    return (this.fixedId != null);
  }
}

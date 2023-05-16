package com.agilecheckup.service;

import com.agilecheckup.persistency.repository.CrudRepository;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

import java.util.Optional;

public abstract class AbstractCrudService<T, V extends CrudRepository<T>> {

  abstract V getRepository();

  public Optional<T> create(T t) {
    T saved = getRepository().save(t);
    System.out.println("Saved = " + saved);
    return Optional.of(saved);
  }

  public PaginatedScanList<T> findAll() {
    return getRepository().findAll();
  }

  public Optional<T> findById(String id) {
    return Optional.of(getRepository().findById(id));
  }

  public Optional<T> fetchAndCompare(String id, T t) {
    System.out.println("Fetching: " + id);
    Optional<T> fetched = findById(id);
    assert t.equals(fetched);
    return fetched;
  }

  public void delete(T t) {
    getRepository().delete(t);
  }
}

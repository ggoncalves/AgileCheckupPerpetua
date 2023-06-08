package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

abstract class AbstractCrudServiceTest<T extends BaseEntity, V extends AbstractCrudRepository<T>> {

  static final String DEFAULT_ID = "1234";


  @BeforeEach
  void setUp() {
    getCrudServiceSpy().setFixedId(DEFAULT_ID);
  }

  @AfterEach
  void tearDown() {
    getCrudServiceSpy().unsetFixedId();
  }

  abstract AbstractCrudService<T, V> getCrudServiceSpy();

}
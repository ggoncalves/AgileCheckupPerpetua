package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.doAnswer;

abstract class AbstractCrudServiceTest<T extends BaseEntity, V extends AbstractCrudRepository<T>> {

  static final String DEFAULT_ID = "1234";

  void doAnswerForSaveWithRandomEntityId(T returnBaseEntity, V repository) {
    @SuppressWarnings("unchecked") final ArgumentCaptor<T> argumentCaptor = ArgumentCaptor.forClass((Class<T>) returnBaseEntity.getClass());

    doAnswer(invocation -> {
      T arg = invocation.getArgument(0);
      returnBaseEntity.setId(arg.getId());
      return returnBaseEntity;
    }).when(repository).save(argumentCaptor.capture());
  }

  protected void doAnswerForUpdate(T returnBaseEntity, V repository) {
    @SuppressWarnings("unchecked") final ArgumentCaptor<T> argumentCaptor = ArgumentCaptor.forClass((Class<T>) returnBaseEntity.getClass());

    doAnswer(invocation -> {
      T arg = invocation.getArgument(0);
      returnBaseEntity.setId(arg.getId()); // Ensure ID is preserved for updates
      return returnBaseEntity;
    }).when(repository).save(argumentCaptor.capture());
  }
}
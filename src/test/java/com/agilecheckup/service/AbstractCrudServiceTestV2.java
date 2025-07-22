package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.base.BaseEntityV2;
import com.agilecheckup.persistency.repository.AbstractCrudRepositoryV2;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.mockito.Mockito.doReturn;

abstract class AbstractCrudServiceTestV2<T extends BaseEntityV2, V extends AbstractCrudRepositoryV2<T>> {
    
    static final String DEFAULT_ID = "1234";
    
    void doReturnForSaveWithRandomEntityId(T returnBaseEntity, V repository) {
        @SuppressWarnings("unchecked") 
        final ArgumentCaptor<T> argumentCaptor = ArgumentCaptor.forClass((Class<T>) returnBaseEntity.getClass());
        
        doReturn(Optional.of(returnBaseEntity))
                .when(repository).save(argumentCaptor.capture());
    }
    
    protected void doReturnForUpdate(T returnBaseEntity, V repository) {
        @SuppressWarnings("unchecked") 
        final ArgumentCaptor<T> argumentCaptor = ArgumentCaptor.forClass((Class<T>) returnBaseEntity.getClass());
        
        doReturn(Optional.of(returnBaseEntity))
                .when(repository).save(argumentCaptor.capture());
    }
    
    protected void doReturnForFindById(T returnEntity, V repository) {
        doReturn(Optional.of(returnEntity))
                .when(repository).findById(returnEntity.getId());
    }
    
    protected void doReturnEmptyForFindById(V repository, String id) {
        doReturn(Optional.empty())
                .when(repository).findById(id);
    }
}
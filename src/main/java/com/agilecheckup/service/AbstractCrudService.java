package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractCrudService<T extends BaseEntity, V extends AbstractCrudRepository<T>> {
    
    abstract V getRepository();
    
    public Optional<T> create(T entity) {
        Optional<T> saved = getRepository().save(entity);
        saved.ifPresent(this::internalPostCreate);
        return saved;
    }
    
    public void internalPostCreate(T saved) {
        log.info("Entity created successfully: {}", saved);
        postCreate(saved);
    }
    
    public Optional<T> update(T entity) {
        Optional<T> saved = getRepository().save(entity);
        saved.ifPresent(this::internalPostUpdate);
        return saved;
    }
    
    public void internalPostUpdate(T saved) {
        log.info("Entity updated successfully: {}", saved);
        postUpdate(saved);
    }
    
    public List<T> findAll() {
        return getRepository().findAll();
    }
    
    public Optional<T> findById(String id) {
        return getRepository().findById(id);
    }
    
    public boolean deleteById(String id) {
        boolean deleted = getRepository().deleteById(id);
        if (deleted) {
            log.info("Entity deleted successfully with id: {}", id);
            postDelete(id);
        }
        return deleted;
    }
    
    public boolean existsById(String id) {
        return getRepository().existsById(id);
    }
    
    protected void postCreate(T entity) {
        // Hook for subclasses
    }
    
    protected void postUpdate(T entity) {
        // Hook for subclasses
    }
    
    protected void postDelete(String id) {
        // Hook for subclasses
    }
}
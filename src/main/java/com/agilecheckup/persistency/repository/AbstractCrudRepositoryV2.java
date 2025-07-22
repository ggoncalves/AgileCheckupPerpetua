package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.base.AuditableEntityV2;
import com.agilecheckup.persistency.entity.base.BaseEntityV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCrudRepositoryV2<T extends BaseEntityV2> {
    
    protected final DynamoDbEnhancedClient enhancedClient;
    protected final Class<T> entityClass;
    protected final String tableName;
    
    protected DynamoDbTable<T> getTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(entityClass));
    }
    
    public Optional<T> save(T entity) {
        try {
            if (entity.getId() == null) {
                entity.generateId();
            }
            
            if (entity instanceof AuditableEntityV2) {
                ((AuditableEntityV2) entity).updateTimestamps();
            }
            
            getTable().putItem(entity);
            log.debug("Successfully saved entity with id: {}", entity.getId());
            return Optional.of(entity);
        } catch (Exception e) {
            log.error("Error saving entity: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    public Optional<T> findById(String id) {
        try {
            Key key = Key.builder()
                    .partitionValue(id)
                    .build();
            
            T item = getTable().getItem(key);
            return Optional.ofNullable(item);
        } catch (Exception e) {
            log.error("Error finding entity by id {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    public List<T> findAll() {
        try {
            SdkIterable<Page<T>> pages = getTable().scan();
            return pages.stream()
                    .flatMap(page -> page.items().stream())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error finding all entities: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public boolean deleteById(String id) {
        try {
            Key key = Key.builder()
                    .partitionValue(id)
                    .build();
            
            getTable().deleteItem(key);
            log.debug("Successfully deleted entity with id: {}", id);
            return true;
        } catch (Exception e) {
            log.error("Error deleting entity with id {}: {}", id, e.getMessage(), e);
            return false;
        }
    }
    
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }
    
    protected List<T> queryBySecondaryIndex(String indexName, String partitionKeyAttributeName, String partitionKeyValue) {
        log.info("Querying index {} for tenantId: {}", indexName, partitionKeyValue);
        
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(partitionKeyValue)
                        .build()
        );
        
        QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .consistentRead(false)  // GSI queries cannot use consistent reads
                .build();
        
        SdkIterable<Page<T>> pages = getTable().index(indexName).query(queryRequest);
        List<T> results = pages.stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
        
        log.info("Query returned {} results", results.size());
        return results;
    }
}
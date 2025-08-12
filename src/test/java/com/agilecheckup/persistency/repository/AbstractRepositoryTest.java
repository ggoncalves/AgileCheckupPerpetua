package com.agilecheckup.persistency.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agilecheckup.persistency.entity.base.BaseEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractRepositoryTest<T extends BaseEntity> {

  @Mock
  protected DynamoDbEnhancedClient mockEnhancedClient;

  @Mock
  protected DynamoDbTable<T> mockTable;

  protected abstract Class<T> getEntityClass();

  protected abstract String getTableName();

  @BeforeEach
  void setUp() {
    lenient().when(mockEnhancedClient.table(anyString(), any(TableSchema.class))).thenReturn(mockTable);
  }

  protected void mockTableGetItem(T entity) {
        when(mockTable.getItem(any(Key.class))).thenReturn(entity);
    }

  protected void mockTableGetItemNull() {
        when(mockTable.getItem(any(Key.class))).thenReturn(null);
    }

  protected void mockTablePutItem(T entity) {
    lenient().doNothing().when(mockTable).putItem(any(getEntityClass()));
  }
}
package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.PerformanceCycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceCycleRepositoryTest {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<PerformanceCycle> table;

    @Mock
    private DynamoDbIndex<PerformanceCycle> tenantIndex;

    @Mock
    private DynamoDbIndex<PerformanceCycle> companyIndex;

    @Mock
    private PageIterable<PerformanceCycle> pageIterable;

    @Mock
    private Page<PerformanceCycle> page;

    private PerformanceCycleRepository repository;

    @BeforeEach
    void setUp() {
        when(enhancedClient.table(eq("PerformanceCycle"), any())).thenReturn((DynamoDbTable) table);
        repository = new PerformanceCycleRepository(enhancedClient);
    }

    @Test
    void shouldSavePerformanceCycle() {
        // Given
        PerformanceCycle cycle = createTestPerformanceCycle("cycle-1", "tenant-123", "Q1 2024", "company-123");

        // When
        Optional<PerformanceCycle> result = repository.save(cycle);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(cycle);
        verify(table).putItem(cycle);
    }

    @Test
    void shouldFindPerformanceCycleById() {
        // Given
        String id = "cycle-1";
        PerformanceCycle cycle = createTestPerformanceCycle(id, "tenant-123", "Q1 2024", "company-123");
        
        when(table.getItem(any(Key.class))).thenReturn(cycle);

        // When
        Optional<PerformanceCycle> result = repository.findById(id);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(cycle);
        verify(table).getItem(any(Key.class));
    }

    @Test
    void shouldDeletePerformanceCycleById() {
        // Given
        String id = "cycle-1";

        // When
        boolean result = repository.deleteById(id);

        // Then
        assertThat(result).isTrue();
        verify(table).deleteItem(any(Key.class));
    }

    @Test
    void shouldFindAllPerformanceCyclesByTenantId() {
        // Given
        String tenantId = "tenant-123";
        List<PerformanceCycle> expectedCycles = Arrays.asList(
                createTestPerformanceCycle("cycle-1", tenantId, "Q1 2024", "company-123"),
                createTestPerformanceCycle("cycle-2", tenantId, "Q2 2024", "company-123")
        );

        when(table.index("tenantId-index")).thenReturn(tenantIndex);
        when(tenantIndex.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(expectedCycles);

        // When
        List<PerformanceCycle> results = repository.findAllByTenantId(tenantId);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(PerformanceCycle::getId).containsExactly("cycle-1", "cycle-2");
        verify(table).index("tenantId-index");
        verify(tenantIndex).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void shouldFindPerformanceCyclesByCompanyId() {
        // Given
        String companyId = "company-123";
        List<PerformanceCycle> expectedCycles = Arrays.asList(
                createTestPerformanceCycle("cycle-1", "tenant-123", "Q1 2024", companyId),
                createTestPerformanceCycle("cycle-2", "tenant-456", "Q2 2024", companyId)
        );

        when(table.index("companyId-index")).thenReturn(companyIndex);
        when(companyIndex.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(expectedCycles);

        // When
        List<PerformanceCycle> results = repository.findByCompanyId(companyId);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(cycle -> cycle.getCompanyId().equals(companyId));
        verify(table).index("companyId-index");
        verify(companyIndex).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void shouldFindActivePerformanceCyclesByTenantId() {
        // Given
        String tenantId = "tenant-123";
        List<PerformanceCycle> allCycles = Arrays.asList(
                createActiveTestPerformanceCycle("cycle-1", tenantId, "Active Q1", "company-123", true),
                createActiveTestPerformanceCycle("cycle-2", tenantId, "Active Q2", "company-123", true),
                createActiveTestPerformanceCycle("cycle-3", tenantId, "Inactive Q3", "company-123", false)
        );

        when(table.index("tenantId-index")).thenReturn(tenantIndex);
        when(tenantIndex.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(allCycles);

        // When
        List<PerformanceCycle> results = repository.findActiveByTenantId(tenantId);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(cycle -> cycle.getIsActive().equals(true));
        verify(table).index("tenantId-index");
        verify(tenantIndex).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoPerformanceCyclesFound() {
        // Given
        String tenantId = "non-existent-tenant";
        when(table.index("tenantId-index")).thenReturn(tenantIndex);
        when(tenantIndex.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.empty());

        // When
        List<PerformanceCycle> results = repository.findAllByTenantId(tenantId);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenPerformanceCycleNotFound() {
        // Given
        String id = "non-existent-id";
        when(table.getItem(any(Key.class))).thenReturn(null);

        // When
        Optional<PerformanceCycle> result = repository.findById(id);

        // Then
        assertThat(result).isEmpty();
    }

    private PerformanceCycle createTestPerformanceCycle(String id, String tenantId, String name, String companyId) {
        return PerformanceCycle.builder()
                .id(id)
                .tenantId(tenantId)
                .name(name)
                .description(name + " Description")
                .companyId(companyId)
                .isActive(true)
                .isTimeSensitive(true)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 3, 31))
                .build();
    }

    private PerformanceCycle createActiveTestPerformanceCycle(String id, String tenantId, String name, String companyId, boolean isActive) {
        return PerformanceCycle.builder()
                .id(id)
                .tenantId(tenantId)
                .name(name)
                .description(name + " Description")
                .companyId(companyId)
                .isActive(isActive)
                .isTimeSensitive(true)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 3, 31))
                .build();
    }
}
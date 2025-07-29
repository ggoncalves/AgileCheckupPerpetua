package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.AssessmentMatrixV2;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentMatrixRepositoryV2Test {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<AssessmentMatrixV2> table;

    @Mock
    private DynamoDbIndex<AssessmentMatrixV2> tenantIndex;

    @Mock
    private PageIterable<AssessmentMatrixV2> pageIterable;

    @Mock
    private Page<AssessmentMatrixV2> page;

    private AssessmentMatrixRepositoryV2 repository;

    @BeforeEach
    void setUp() {
        when(enhancedClient.table(eq("AssessmentMatrix"), any())).thenReturn((DynamoDbTable) table);
        repository = new AssessmentMatrixRepositoryV2(enhancedClient);
    }

    @Test
    void shouldSaveAssessmentMatrix() {
        // Given
        AssessmentMatrixV2 matrix = createTestAssessmentMatrix("matrix-1", "tenant-123");

        // When
        Optional<AssessmentMatrixV2> result = repository.save(matrix);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(matrix);
        verify(table).putItem(matrix);
    }

    @Test
    void shouldFindAssessmentMatrixById() {
        // Given
        String id = "matrix-1";
        AssessmentMatrixV2 matrix = createTestAssessmentMatrix(id, "tenant-123");
        
        when(table.getItem(any(Key.class))).thenReturn(matrix);

        // When
        Optional<AssessmentMatrixV2> result = repository.findById(id);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(matrix);
        verify(table).getItem(any(Key.class));
    }

    @Test
    void shouldFindAllAssessmentMatricesByTenantId() {
        // Given
        String tenantId = "tenant-123";
        List<AssessmentMatrixV2> expectedMatrices = Arrays.asList(
                createTestAssessmentMatrix("matrix-1", tenantId),
                createTestAssessmentMatrix("matrix-2", tenantId)
        );

        when(table.index("tenantId-index")).thenReturn(tenantIndex);
        when(tenantIndex.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(expectedMatrices);

        // When
        List<AssessmentMatrixV2> results = repository.findAllByTenantId(tenantId);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(AssessmentMatrixV2::getId).containsExactly("matrix-1", "matrix-2");
        verify(table).index("tenantId-index");
        verify(tenantIndex).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void shouldReturnEmptyOptionalWhenAssessmentMatrixNotFound() {
        // Given
        String id = "non-existent-id";
        when(table.getItem(any(Key.class))).thenReturn(null);

        // When
        Optional<AssessmentMatrixV2> result = repository.findById(id);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldDeleteAssessmentMatrixById() {
        // Given
        String id = "matrix-1";

        // When
        boolean result = repository.deleteById(id);

        // Then
        assertThat(result).isTrue();
        verify(table).deleteItem(any(Key.class));
    }

    private AssessmentMatrixV2 createTestAssessmentMatrix(String id, String tenantId) {
        return AssessmentMatrixV2.builder()
                .id(id)
                .name("Test Matrix " + id)
                .description("Test Description for " + id)
                .tenantId(tenantId)
                .performanceCycleId("cycle-123")
                .questionCount(5)
                .build();
    }
}
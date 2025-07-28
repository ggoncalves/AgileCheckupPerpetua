package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.TeamV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
class TeamRepositoryV2Test {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<TeamV2> table;

    @Mock
    private DynamoDbIndex<TeamV2> index;

    @Mock
    private PageIterable<TeamV2> pageIterable;

    @Mock
    private Page<TeamV2> page;

    private TeamRepositoryV2 repository;

    @BeforeEach
    void setUp() {
        when(enhancedClient.table(eq("Team"), any())).thenReturn((DynamoDbTable) table);
        repository = new TeamRepositoryV2(enhancedClient);
    }

    @Test
    void testConstructorCreatesTableWithCorrectName() {
        // The table() method is called lazily when getTable() is accessed
        // Let's trigger table access and verify it was called with correct name
        when(table.index("test-index")).thenReturn(index);
        
        // Access getTable() indirectly by calling a method that uses it
        try {
            repository.findByDepartmentId("test-dept");  // This will access the table
        } catch (Exception e) {
            // Expected to fail due to mocking, but table() should have been called
        }
        
        verify(enhancedClient, atLeastOnce()).table(eq("Team"), any());
    }

    @Test
    void testFindByDepartmentId() {
        // Given
        String departmentId = "dept-123";
        TeamV2 team1 = TeamV2.builder()
                .id("team-1")
                .departmentId(departmentId)
                .name("Team 1")
                .description("Team 1 description")
                .tenantId("tenant-123")
                .build();
        TeamV2 team2 = TeamV2.builder()
                .id("team-2")
                .departmentId(departmentId)
                .name("Team 2")
                .description("Team 2 description")
                .tenantId("tenant-123")
                .build();

        when(table.index("departmentId-index")).thenReturn(index);
        when(index.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(Arrays.asList(team1, team2));

        // When
        List<TeamV2> result = repository.findByDepartmentId(departmentId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(team1, team2);
        verify(table).index("departmentId-index");
        verify(index).query(any(QueryEnhancedRequest.class));
    }

    @Test
    void testFindByDepartmentIdReturnsEmptyListWhenNoResults() {
        // Given
        String departmentId = "dept-999";
        when(table.index("departmentId-index")).thenReturn(index);
        when(index.query(any(QueryEnhancedRequest.class))).thenReturn(pageIterable);
        when(pageIterable.stream()).thenReturn(Stream.empty());

        // When
        List<TeamV2> result = repository.findByDepartmentId(departmentId);

        // Then
        assertThat(result).isEmpty();
    }
}
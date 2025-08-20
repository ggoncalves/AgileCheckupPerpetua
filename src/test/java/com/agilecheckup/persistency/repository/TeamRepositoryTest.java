package com.agilecheckup.persistency.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agilecheckup.persistency.entity.Team;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@ExtendWith(MockitoExtension.class)
class TeamRepositoryTest {

  @Mock
  private DynamoDbEnhancedClient enhancedClient;

  @Mock
  private DynamoDbTable<Team> table;

  @Mock
  private DynamoDbIndex<Team> index;

  @Mock
  private PageIterable<Team> pageIterable;

  @Mock
  private Page<Team> page;

  private TeamRepository repository;

  @BeforeEach
    void setUp() {
        when(enhancedClient.table(eq("Team"), any())).thenReturn((DynamoDbTable) table);
        repository = new TeamRepository(enhancedClient);
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
    Team team1 = Team.builder()
                     .id("team-1")
                     .departmentId(departmentId)
                     .name("Team 1")
                     .description("Team 1 description")
                     .tenantId("tenant-123")
                     .build();
    Team team2 = Team.builder()
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
    List<Team> result = repository.findByDepartmentId(departmentId);

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
    List<Team> result = repository.findByDepartmentId(departmentId);

    // Then
    assertThat(result).isEmpty();
  }
}
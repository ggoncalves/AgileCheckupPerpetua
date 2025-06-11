package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.Team;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static com.agilecheckup.util.TestObjectFactory.createMockedTeam;
import static com.agilecheckup.util.TestObjectFactory.createMockedTeamWithDependenciesId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamRepositoryTest extends AbstractRepositoryTest<Team> {

  @InjectMocks
  @Spy
  private TeamRepository teamRepository;

  @Test
  void findByDepartmentId_shouldQueryGSIByDepartment() {
    // Given
    String tenantId = "tenant-123";
    String departmentId = "dept-123";
    
    PaginatedQueryList<Team> mockPaginatedList = mock(PaginatedQueryList.class);
    
    when(dynamoDBMapperMock.query(eq(Team.class), any(DynamoDBQueryExpression.class)))
        .thenReturn(mockPaginatedList);
    
    // When
    List<Team> result = teamRepository.findByDepartmentId(departmentId, tenantId);
    
    // Then
    assertEquals(mockPaginatedList, result);
    // Verify DynamoDB query was called correctly
    verify(dynamoDBMapperMock).query(eq(Team.class), any(DynamoDBQueryExpression.class));
  }

  @Test
  void findByDepartmentId_shouldReturnEmptyListWhenNoDepartmentMatches() {
    // Given
    String tenantId = "tenant-123";
    String departmentId = "dept-123";
    
    PaginatedQueryList<Team> mockPaginatedList = mock(PaginatedQueryList.class);
    
    when(dynamoDBMapperMock.query(eq(Team.class), any(DynamoDBQueryExpression.class)))
        .thenReturn(mockPaginatedList);
    
    // When
    List<Team> result = teamRepository.findByDepartmentId(departmentId, tenantId);
    
    // Then
    assertEquals(mockPaginatedList, result);
  }

  @Override
  AbstractCrudRepository getRepository() {
    return teamRepository;
  }

  @Override
  Team createMockedT() {
    return createMockedTeam();
  }

  @Override
  Class getMockedClass() {
    return Team.class;
  }
}
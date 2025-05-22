package com.agilecheckup.persistency.repository;

import com.agilecheckup.persistency.entity.Company;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.agilecheckup.util.TestObjectFactory.createMockedCompany;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyRepositoryTest extends AbstractRepositoryTest<Company> {

  @Mock
  private DynamoDBMapper dynamoDBMapper; // Mocked, not spied, for full control

  @InjectMocks
  private CompanyRepository companyRepository; // companyRepository will use the mocked dynamoDBMapper

  // Removed @Spy from companyRepository as we are now injecting a pure mock of DynamoDBMapper
  // If companyRepository had other dependencies or methods needing real behavior,
  // then @Spy and potentially a different setup for dynamoDBMapper would be needed.

  @Override
  AbstractCrudRepository<Company> getRepository() { // Specified generic type for clarity
    return companyRepository;
  }

  @Override
  Company createMockedT() {
    return createMockedCompany();
  }

  @Test
  void testFindAllByTenantId_shouldCallMapperWithCorrectQueryExpression() {
    // Arrange
    String tenantId = "test-tenant-id";
    PaginatedQueryList<Company> expectedResult = mock(PaginatedQueryList.class); // Use Mockito.mock

    // Mock the behavior of dynamoDBMapper.query
    when(dynamoDBMapper.query(eq(Company.class), any(DynamoDBQueryExpression.class)))
        .thenReturn(expectedResult);

    // Act
    PaginatedQueryList<Company> actualResult = companyRepository.findAllByTenantId(tenantId);

    // Assert
    // Verify that dynamoDBMapper.query was called once
    ArgumentCaptor<DynamoDBQueryExpression<Company>> queryExpressionCaptor =
        ArgumentCaptor.forClass(DynamoDBQueryExpression.class);
    verify(dynamoDBMapper).query(eq(Company.class), queryExpressionCaptor.capture());

    DynamoDBQueryExpression<Company> capturedExpression = queryExpressionCaptor.getValue();

    // Assertions on the captured query expression
    assertEquals("tenantId-index", capturedExpression.getIndexName());
    assertEquals("tenantId = :tenantId", capturedExpression.getKeyConditionExpression());
    assertFalse(capturedExpression.isConsistentRead());

    Map<String, AttributeValue> expressionAttributeValues = capturedExpression.getExpressionAttributeValues();
    assertEquals(1, expressionAttributeValues.size());
    assertEquals(tenantId, expressionAttributeValues.get(":tenantId").getS());

    // Assert that the result of companyRepository.findAllByTenantId(...) is the same as the mocked PaginatedQueryList
    assertSame(expectedResult, actualResult);
  }

  // Helper method for mocking, if needed elsewhere, or use Mockito.mock directly
  @SuppressWarnings("unchecked")
  private <T> PaginatedQueryList<T> mock(Class<T> type) {
    return org.mockito.Mockito.mock(PaginatedQueryList.class);
  }

  @Test
  void testSave_shouldUpdateExistingEntity() {
    // Arrange
    Company company = createMockedCompany(); // Helper method from existing test structure
    company.setId("company-123");
    company.setName("Initial Name");
    company.setTenantId("tenant-abc");

    // Act & Assert - First save (creation)
    companyRepository.save(company);
    verify(dynamoDBMapper).save(eq(company)); // Verifies it's called once with this company

    // Modify the company
    String updatedName = "Updated Name";
    company.setName(updatedName);

    // Act & Assert - Second save (update)
    companyRepository.save(company);

    // Verify save was called a second time
    // We can capture the argument to ensure the modified company was passed
    ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
    verify(dynamoDBMapper, org.mockito.Mockito.times(2)).save(companyCaptor.capture());

    Company savedCompany = companyCaptor.getValue(); // Get the company passed to the second save
    assertEquals(updatedName, savedCompany.getName());
    assertEquals("company-123", savedCompany.getId()); // Ensure ID remains the same
    assertEquals("tenant-abc", savedCompany.getTenantId()); // Ensure other properties are still there

    // Additionally, assert that the same instance was passed if that's the expected behavior,
    // or that key properties match if a new instance might be created internally before saving.
    // For this test, we assume the repository passes the modified instance directly.
    assertSame(company, savedCompany, "The same company instance should be passed to the mapper on update.");
  }
}
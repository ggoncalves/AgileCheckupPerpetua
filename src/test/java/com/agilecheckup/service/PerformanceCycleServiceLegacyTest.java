package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.PerformanceCycleRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.cloneWithId;
import static com.agilecheckup.util.TestObjectFactory.createMockedCompany;
import static com.agilecheckup.util.TestObjectFactory.createMockedPerformanceCycleWithDependenciesId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceCycleServiceLegacyTest extends AbstractCrudServiceTest<PerformanceCycle, AbstractCrudRepository<PerformanceCycle>> {

  @InjectMocks
  @Spy
  private PerformanceCycleServiceLegacy performanceCycleService;

  @Mock
  private PerformanceCycleRepository mockPerformanceCycleRepository;

  @Mock
  private CompanyServiceLegacy mockCompanyService;

  private static PerformanceCycle originalPerformanceCycle;

  private final Company company = createMockedCompany(DEFAULT_ID);

  @BeforeAll
  static void beforeAll() {
    originalPerformanceCycle = createMockedPerformanceCycleWithDependenciesId(DEFAULT_ID);
    originalPerformanceCycle = cloneWithId(originalPerformanceCycle, DEFAULT_ID);
  }

  @Test
  void create() {
    PerformanceCycle savedPerformanceCycle = cloneWithId(originalPerformanceCycle, DEFAULT_ID);
    Date startDate = new Date();
    Date endDate = new Date(startDate.getTime() + 86400000); // +1 day
    savedPerformanceCycle.setStartDate(startDate);
    savedPerformanceCycle.setEndDate(endDate);
    savedPerformanceCycle.setIsTimeSensitive(true); // Should be true when endDate is present

    // Prevent/Stub
    doAnswerForSaveWithRandomEntityId(savedPerformanceCycle, mockPerformanceCycleRepository);
    doReturn(Optional.of(company)).when(mockCompanyService).findById(originalPerformanceCycle.getCompanyId());

    // When
    Optional<PerformanceCycle> performanceCycleOptional = performanceCycleService.create(
        originalPerformanceCycle.getName(),
        originalPerformanceCycle.getDescription(),
        originalPerformanceCycle.getTenantId(),
        originalPerformanceCycle.getCompanyId(),
        originalPerformanceCycle.getIsActive(),
        originalPerformanceCycle.getIsTimeSensitive(), // This will be overridden by business rule
        startDate,
        endDate
    );

    // Then
    assertTrue(performanceCycleOptional.isPresent());
    assertEquals(savedPerformanceCycle, performanceCycleOptional.get());
    verify(mockPerformanceCycleRepository).save(savedPerformanceCycle);
    verify(performanceCycleService).create(
        originalPerformanceCycle.getName(),
        originalPerformanceCycle.getDescription(),
        originalPerformanceCycle.getTenantId(),
        originalPerformanceCycle.getCompanyId(),
        originalPerformanceCycle.getIsActive(),
        originalPerformanceCycle.getIsTimeSensitive(),
        startDate,
        endDate
    );
    verify(mockCompanyService).findById(originalPerformanceCycle.getCompanyId());
  }

  @Test
  void createInvalidCompanyId() {
    // Prevent/Stub
    doReturn(Optional.empty()).when(mockCompanyService).findById(originalPerformanceCycle.getCompanyId());

    // When
    assertThrows(InvalidIdReferenceException.class, () -> performanceCycleService.create(
        originalPerformanceCycle.getName(),
        originalPerformanceCycle.getDescription(),
        originalPerformanceCycle.getTenantId(),
        originalPerformanceCycle.getCompanyId(),
        originalPerformanceCycle.getIsActive(),
        originalPerformanceCycle.getIsTimeSensitive(),
        new Date(),
        new Date()
    ));
  }

  @Test
  void create_NullPerformanceCycleName() {
    // When
    assertThrows(NullPointerException.class, () -> performanceCycleService.create(
        null,
        originalPerformanceCycle.getDescription(),
        originalPerformanceCycle.getTenantId(),
        originalPerformanceCycle.getCompanyId(),
        originalPerformanceCycle.getIsActive(),
        originalPerformanceCycle.getIsTimeSensitive(),
        new Date(),
        new Date()
    ));
  }

  @Test
  void update_existingPerformanceCycle_shouldSucceed() {
    // Prepare
    PerformanceCycle existingPerformanceCycle = createMockedPerformanceCycleWithDependenciesId(DEFAULT_ID);
    existingPerformanceCycle = cloneWithId(existingPerformanceCycle, DEFAULT_ID);
    PerformanceCycle updatedPerformanceCycleDetails = createMockedPerformanceCycleWithDependenciesId("updatedCompanyId");
    updatedPerformanceCycleDetails.setName("Updated Performance Cycle Name");
    updatedPerformanceCycleDetails.setDescription("Updated Description");
    updatedPerformanceCycleDetails.setTenantId("Updated Tenant Id");
    updatedPerformanceCycleDetails.setIsActive(false);
    updatedPerformanceCycleDetails.setIsTimeSensitive(true);  // Should be true when isActive is false
    Date startDate = new Date();
    Date endDate = new Date(startDate.getTime() + 86400000);
    updatedPerformanceCycleDetails.setStartDate(startDate);
    updatedPerformanceCycleDetails.setEndDate(endDate);

    Company updatedCompany = createMockedCompany("updatedCompanyId");

    // Mock repository calls
    doReturn(existingPerformanceCycle).when(mockPerformanceCycleRepository).findById(DEFAULT_ID);
    doAnswerForUpdate(updatedPerformanceCycleDetails, mockPerformanceCycleRepository);
    doReturn(Optional.of(updatedCompany)).when(mockCompanyService).findById("updatedCompanyId");

    // When
    Optional<PerformanceCycle> resultOptional = performanceCycleService.update(
        DEFAULT_ID,
        "Updated Performance Cycle Name",
        "Updated Description",
        "Updated Tenant Id",
        "updatedCompanyId",
        false,
        false,
        startDate,
        endDate
    );

    // Then
    assertTrue(resultOptional.isPresent());
    assertEquals(updatedPerformanceCycleDetails, resultOptional.get());
    verify(mockPerformanceCycleRepository).findById(DEFAULT_ID);
    verify(mockPerformanceCycleRepository).save(updatedPerformanceCycleDetails);
    verify(mockCompanyService).findById("updatedCompanyId");
    verify(performanceCycleService).update(DEFAULT_ID,
        "Updated Performance Cycle Name",
        "Updated Description",
        "Updated Tenant Id",
        "updatedCompanyId",
        false,
        false,
        startDate,
        endDate);
  }

  @Test
  void update_nonExistingPerformanceCycle_shouldReturnEmpty() {
    // Prepare
    String nonExistingId = "nonExistingId";

    // Mock repository calls
    doReturn(null).when(mockPerformanceCycleRepository).findById(nonExistingId);

    // When
    Optional<PerformanceCycle> resultOptional = performanceCycleService.update(
        nonExistingId,
        "name",
        "desc",
        "tenant",
        "companyId",
        true,
        true,
        new Date(),
        new Date()
    );

    // Then
    assertTrue(resultOptional.isEmpty());
    verify(mockPerformanceCycleRepository).findById(nonExistingId);
    verify(performanceCycleService).update(eq(nonExistingId), eq("name"), eq("desc"), eq("tenant"), eq("companyId"), eq(true), eq(true), 
        org.mockito.ArgumentMatchers.any(Date.class), org.mockito.ArgumentMatchers.any(Date.class));
  }

  @Test
  void create_withEndDate_shouldSetIsTimeSensitiveTrue() {
    // Prepare
    PerformanceCycle savedPerformanceCycle = cloneWithId(originalPerformanceCycle, DEFAULT_ID);
    Date endDate = new Date();
    savedPerformanceCycle.setEndDate(endDate);
    savedPerformanceCycle.setIsTimeSensitive(true); // Should be automatically set to true

    // Mock
    doAnswerForSaveWithRandomEntityId(savedPerformanceCycle, mockPerformanceCycleRepository);
    doReturn(Optional.of(company)).when(mockCompanyService).findById(originalPerformanceCycle.getCompanyId());

    // When
    Optional<PerformanceCycle> result = performanceCycleService.create(
        originalPerformanceCycle.getName(),
        originalPerformanceCycle.getDescription(),
        originalPerformanceCycle.getTenantId(),
        originalPerformanceCycle.getCompanyId(),
        originalPerformanceCycle.getIsActive(),
        false, // Pass false, but should be overridden to true
        null,
        endDate
    );

    // Then
    assertTrue(result.isPresent());
    assertTrue(result.get().getIsTimeSensitive());
  }

  @Test
  void create_withoutEndDate_shouldSetIsTimeSensitiveFalse() {
    // Prepare
    PerformanceCycle savedPerformanceCycle = cloneWithId(originalPerformanceCycle, DEFAULT_ID);
    savedPerformanceCycle.setIsTimeSensitive(false); // Should be automatically set to false

    // Mock
    doAnswerForSaveWithRandomEntityId(savedPerformanceCycle, mockPerformanceCycleRepository);
    doReturn(Optional.of(company)).when(mockCompanyService).findById(originalPerformanceCycle.getCompanyId());

    // When
    Optional<PerformanceCycle> result = performanceCycleService.create(
        originalPerformanceCycle.getName(),
        originalPerformanceCycle.getDescription(),
        originalPerformanceCycle.getTenantId(),
        originalPerformanceCycle.getCompanyId(),
        originalPerformanceCycle.getIsActive(),
        true, // Pass true, but should be overridden to false
        null,
        null
    );

    // Then
    assertTrue(result.isPresent());
    assertFalse(result.get().getIsTimeSensitive());
  }

  @Test
  void update_withEndDate_shouldSetIsTimeSensitiveTrue() {
    // Prepare
    PerformanceCycle existingCycle = createMockedPerformanceCycleWithDependenciesId(DEFAULT_ID);
    existingCycle = cloneWithId(existingCycle, DEFAULT_ID);
    Date endDate = new Date();
    
    PerformanceCycle updatedCycle = cloneWithId(existingCycle, DEFAULT_ID);
    updatedCycle.setEndDate(endDate);
    updatedCycle.setIsTimeSensitive(true);

    // Mock
    doReturn(existingCycle).when(mockPerformanceCycleRepository).findById(DEFAULT_ID);
    doAnswerForUpdate(updatedCycle, mockPerformanceCycleRepository);
    doReturn(Optional.of(company)).when(mockCompanyService).findById(existingCycle.getCompanyId());

    // When
    Optional<PerformanceCycle> result = performanceCycleService.update(
        DEFAULT_ID,
        existingCycle.getName(),
        existingCycle.getDescription(),
        existingCycle.getTenantId(),
        existingCycle.getCompanyId(),
        existingCycle.getIsActive(),
        false, // Pass false, but should be overridden to true
        null,
        endDate
    );

    // Then
    assertTrue(result.isPresent());
    assertTrue(result.get().getIsTimeSensitive());
  }

  @Test
  void findAllByTenantId_shouldReturnListForTenant() {
    // Prepare
    String tenantId = "tenant123";
    PerformanceCycle cycle1 = createMockedPerformanceCycleWithDependenciesId("cycle1");
    cycle1.setTenantId(tenantId);
    PerformanceCycle cycle2 = createMockedPerformanceCycleWithDependenciesId("cycle2");
    cycle2.setTenantId(tenantId);
    List<PerformanceCycle> expectedCycles = Arrays.asList(cycle1, cycle2);
    
    PaginatedQueryList<PerformanceCycle> mockPaginatedList = mock(PaginatedQueryList.class);
    when(mockPaginatedList.stream()).thenReturn(expectedCycles.stream());
    
    // Mock
    when(mockPerformanceCycleRepository.findAllByTenantId(tenantId)).thenReturn(mockPaginatedList);
    
    // When
    List<PerformanceCycle> result = performanceCycleService.findAllByTenantId(tenantId);
    
    // Then
    assertEquals(2, result.size());
    assertEquals(expectedCycles, result);
    verify(mockPerformanceCycleRepository).findAllByTenantId(tenantId);
  }
}
package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.repository.PerformanceCycleRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.agilecheckup.util.TestObjectFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PerformanceCycleServiceTest {

  private static final String PerformanceCycle_ID = "1234";

  @InjectMocks
  @Spy
  private PerformanceCycleService performanceCycleService;

  @Mock
  private PerformanceCycleRepository mockPerformanceCycleRepository;

  @Mock
  private CompanyService mockCompanyService;

  private final PerformanceCycle originalPerformanceCycle = createMockedPerformanceCycleWithDependenciesId(GENERIC_ID_1234);

  private final Company company = createMockedCompany(GENERIC_ID_1234);

  @Test
  void create() {
    PerformanceCycle savedPerformanceCycle = cloneWithId(originalPerformanceCycle, PerformanceCycle_ID);

    // Prevent/Stub
    doReturn(savedPerformanceCycle).when(mockPerformanceCycleRepository).save(originalPerformanceCycle);
    doReturn(Optional.of(company)).when(mockCompanyService).findById(originalPerformanceCycle.getCompany().getId());

    // When
    Optional<PerformanceCycle> performanceCycleOptional = performanceCycleService.create(
        originalPerformanceCycle.getName(),
        originalPerformanceCycle.getDescription(),
        originalPerformanceCycle.getTenantId(),
        originalPerformanceCycle.getCompany().getId(),
        originalPerformanceCycle.getIsActive(),
        originalPerformanceCycle.getIsTimeSensitive()
    );

    // Then
    assertTrue(performanceCycleOptional.isPresent());
    assertEquals(savedPerformanceCycle, performanceCycleOptional.get());
    verify(mockPerformanceCycleRepository).save(originalPerformanceCycle);
    verify(performanceCycleService).create(
        originalPerformanceCycle.getName(),
        originalPerformanceCycle.getDescription(),
        originalPerformanceCycle.getTenantId(),
        originalPerformanceCycle.getCompany().getId(),
        originalPerformanceCycle.getIsActive(),
        originalPerformanceCycle.getIsTimeSensitive()
    );
    verify(mockCompanyService).findById(originalPerformanceCycle.getCompany().getId());
  }

  @Test
  void createInvalidCompanyId() {
    PerformanceCycle savedPerformanceCycle = cloneWithId(originalPerformanceCycle, PerformanceCycle_ID);

    // Prevent/Stub
    doReturn(Optional.empty()).when(mockCompanyService).findById(originalPerformanceCycle.getCompany().getId());

    // When
    assertThrows(InvalidIdReferenceException.class, () -> {
      performanceCycleService.create(
          originalPerformanceCycle.getName(),
          originalPerformanceCycle.getDescription(),
          originalPerformanceCycle.getTenantId(),
          originalPerformanceCycle.getCompany().getId(),
          originalPerformanceCycle.getIsActive(),
          originalPerformanceCycle.getIsTimeSensitive()
      );
    });
  }

  @Test
  void create_NullPerformanceCycleName() {
    // When
    assertThrows(NullPointerException.class, () -> {
      performanceCycleService.create(
          null,
          originalPerformanceCycle.getDescription(),
          originalPerformanceCycle.getTenantId(),
          originalPerformanceCycle.getCompany().getId(),
          originalPerformanceCycle.getIsActive(),
          originalPerformanceCycle.getIsTimeSensitive()
      );
    });
  }
}
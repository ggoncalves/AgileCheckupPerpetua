package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.repository.AbstractCrudRepository;
import com.agilecheckup.persistency.repository.PerformanceCycleRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import org.junit.jupiter.api.BeforeAll;
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
class PerformanceCycleServiceTest extends AbstractCrudServiceTest<PerformanceCycle, AbstractCrudRepository<PerformanceCycle>> {

  @InjectMocks
  @Spy
  private PerformanceCycleService performanceCycleService;

  @Mock
  private PerformanceCycleRepository mockPerformanceCycleRepository;

  @Mock
  private CompanyService mockCompanyService;

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
        originalPerformanceCycle.getIsTimeSensitive()
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
        originalPerformanceCycle.getIsTimeSensitive()
    );
    verify(mockCompanyService).findById(originalPerformanceCycle.getCompanyId());
  }

  @Test
  void createInvalidCompanyId() {
    // Prevent/Stub
    doReturn(Optional.empty()).when(mockCompanyService).findById(originalPerformanceCycle.getCompanyId());

    // When
    assertThrows(InvalidIdReferenceException.class, () -> {
      performanceCycleService.create(
          originalPerformanceCycle.getName(),
          originalPerformanceCycle.getDescription(),
          originalPerformanceCycle.getTenantId(),
          originalPerformanceCycle.getCompanyId(),
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
          originalPerformanceCycle.getCompanyId(),
          originalPerformanceCycle.getIsActive(),
          originalPerformanceCycle.getIsTimeSensitive()
      );
    });
  }
}
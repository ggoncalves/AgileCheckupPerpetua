package com.agilecheckup.service;

import com.agilecheckup.persistency.entity.Company;
import com.agilecheckup.persistency.entity.PerformanceCycle;
import com.agilecheckup.persistency.repository.PerformanceCycleRepository;
import com.agilecheckup.service.exception.InvalidIdReferenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceCycleServiceTest {

    @Mock
    private PerformanceCycleRepository performanceCycleRepositoryV2;

    @Mock
    private CompanyService companyServiceV2;

    private PerformanceCycleService performanceCycleServiceV2;

    @BeforeEach
    void setUp() {
        performanceCycleServiceV2 = new PerformanceCycleService(performanceCycleRepositoryV2, companyServiceV2);
    }

    @Test
    void shouldCreatePerformanceCycleSuccessfully() {
        // Given
        String tenantId = "tenant-123";
        String name = "Q1 2024 Performance Cycle";
        String description = "First quarter performance review";
        String companyId = "company-123";
        Boolean isActive = true;
        Boolean isTimeSensitive = true;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);

        Company mockCompany = createMockCompany(companyId);
        PerformanceCycle expectedCycle = createMockPerformanceCycle(tenantId, name, description, companyId,
                isActive, true, startDate, endDate); // isTimeSensitive calculated as true because endDate is present

        doReturn(Optional.of(mockCompany)).when(companyServiceV2).findById(companyId);
        doReturn(Optional.of(expectedCycle)).when(performanceCycleRepositoryV2).save(any(PerformanceCycle.class));

        // When
        Optional<PerformanceCycle> result = performanceCycleServiceV2.create(tenantId, name, description, companyId,
                isActive, isTimeSensitive, startDate, endDate);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(name);
        assertThat(result.get().getDescription()).isEqualTo(description);
        assertThat(result.get().getTenantId()).isEqualTo(tenantId);
        assertThat(result.get().getCompanyId()).isEqualTo(companyId);
        assertThat(result.get().getIsActive()).isEqualTo(isActive);
        assertThat(result.get().getIsTimeSensitive()).isTrue(); // Calculated as true because endDate is present
        assertThat(result.get().getStartDate()).isEqualTo(startDate);
        assertThat(result.get().getEndDate()).isEqualTo(endDate);

        verify(companyServiceV2).findById(companyId);
        verify(performanceCycleRepositoryV2).save(any(PerformanceCycle.class));
    }

    @Test
    void shouldCreatePerformanceCycleWithTimeSensitiveFalseWhenEndDateIsNull() {
        // Given
        String tenantId = "tenant-123";
        String name = "Ongoing Performance Cycle";
        String description = "Continuous performance review";
        String companyId = "company-123";
        Boolean isActive = true;
        Boolean isTimeSensitive = true; // User passes true, but should be calculated as false
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = null; // No end date

        Company mockCompany = createMockCompany(companyId);
        PerformanceCycle expectedCycle = createMockPerformanceCycle(tenantId, name, description, companyId,
                isActive, false, startDate, endDate); // isTimeSensitive calculated as false because endDate is null

        doReturn(Optional.of(mockCompany)).when(companyServiceV2).findById(companyId);
        doReturn(Optional.of(expectedCycle)).when(performanceCycleRepositoryV2).save(any(PerformanceCycle.class));

        // When
        Optional<PerformanceCycle> result = performanceCycleServiceV2.create(tenantId, name, description, companyId,
                isActive, isTimeSensitive, startDate, endDate);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getIsTimeSensitive()).isFalse(); // Business rule applied
        verify(companyServiceV2).findById(companyId);
        verify(performanceCycleRepositoryV2).save(any(PerformanceCycle.class));
    }

    @Test
    void shouldThrowExceptionWhenCompanyNotFoundDuringCreate() {
        // Given
        String tenantId = "tenant-123";
        String name = "Q1 2024";
        String description = "Test cycle";
        String companyId = "non-existent-company";
        Boolean isActive = true;
        Boolean isTimeSensitive = false;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);

        doReturn(Optional.empty()).when(companyServiceV2).findById(companyId);

        // When/Then
        assertThatThrownBy(() -> performanceCycleServiceV2.create(tenantId, name, description, companyId,
                isActive, isTimeSensitive, startDate, endDate))
                .isInstanceOf(InvalidIdReferenceException.class)
                .hasMessageContaining(companyId)
                .hasMessageContaining("PerformanceCycle")
                .hasMessageContaining("Company");

        verify(companyServiceV2).findById(companyId);
    }

    @Test
    void shouldUpdatePerformanceCycleSuccessfully() {
        // Given
        String id = "cycle-123";
        String tenantId = "tenant-123";
        String name = "Q1 2024 - Updated";
        String description = "Updated description";
        String companyId = "company-123";
        Boolean isActive = false;
        Boolean isTimeSensitive = true;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);

        PerformanceCycle existingCycle = createMockPerformanceCycle(tenantId, "Old Name", "Old Description",
                companyId, true, false, startDate, null);
        Company mockCompany = createMockCompany(companyId);
        PerformanceCycle updatedCycle = createMockPerformanceCycle(tenantId, name, description, companyId,
                isActive, true, startDate, endDate); // isTimeSensitive calculated as true

        doReturn(Optional.of(existingCycle)).when(performanceCycleRepositoryV2).findById(id);
        doReturn(Optional.of(mockCompany)).when(companyServiceV2).findById(companyId);
        doReturn(Optional.of(updatedCycle)).when(performanceCycleRepositoryV2).save(any(PerformanceCycle.class));

        // When
        Optional<PerformanceCycle> result = performanceCycleServiceV2.update(id, tenantId, name, description,
                companyId, isActive, isTimeSensitive, startDate, endDate);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(name);
        assertThat(result.get().getDescription()).isEqualTo(description);
        assertThat(result.get().getIsActive()).isEqualTo(isActive);
        assertThat(result.get().getIsTimeSensitive()).isTrue(); // Business rule applied

        verify(performanceCycleRepositoryV2).findById(id);
        verify(companyServiceV2).findById(companyId);
        verify(performanceCycleRepositoryV2).save(any(PerformanceCycle.class));
    }

    @Test
    void shouldReturnEmptyWhenUpdatingNonExistentPerformanceCycle() {
        // Given
        String id = "non-existent-cycle";
        doReturn(Optional.empty()).when(performanceCycleRepositoryV2).findById(id);

        // When
        Optional<PerformanceCycle> result = performanceCycleServiceV2.update(id, "tenant-123", "Name",
                "Description", "company-123", true, false, LocalDate.now(), null);

        // Then
        assertThat(result).isEmpty();
        verify(performanceCycleRepositoryV2).findById(id);
    }

    @Test
    void shouldFindAllPerformanceCyclesByTenantId() {
        // Given
        String tenantId = "tenant-123";
        List<PerformanceCycle> expectedCycles = Arrays.asList(
                createMockPerformanceCycle(tenantId, "Q1 2024", "First quarter", "company-123", true, true, 
                        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31)),
                createMockPerformanceCycle(tenantId, "Q2 2024", "Second quarter", "company-123", true, true, 
                        LocalDate.of(2024, 4, 1), LocalDate.of(2024, 6, 30))
        );

        when(performanceCycleRepositoryV2.findAllByTenantId(tenantId)).thenReturn(expectedCycles);

        // When
        List<PerformanceCycle> result = performanceCycleServiceV2.findAllByTenantId(tenantId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(PerformanceCycle::getName).containsExactly("Q1 2024", "Q2 2024");
        verify(performanceCycleRepositoryV2).findAllByTenantId(tenantId);
    }

    @Test
    void shouldFindPerformanceCyclesByCompanyId() {
        // Given
        String companyId = "company-123";
        List<PerformanceCycle> expectedCycles = Arrays.asList(
                createMockPerformanceCycle("tenant-123", "Q1 2024", "First quarter", companyId, true, true, 
                        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31)),
                createMockPerformanceCycle("tenant-456", "Q1 2024", "First quarter", companyId, true, true, 
                        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31))
        );

        when(performanceCycleRepositoryV2.findByCompanyId(companyId)).thenReturn(expectedCycles);

        // When
        List<PerformanceCycle> result = performanceCycleServiceV2.findByCompanyId(companyId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(cycle -> cycle.getCompanyId().equals(companyId));
        verify(performanceCycleRepositoryV2).findByCompanyId(companyId);
    }

    @Test
    void shouldFindActivePerformanceCyclesByTenantId() {
        // Given
        String tenantId = "tenant-123";
        List<PerformanceCycle> expectedCycles = Arrays.asList(
                createMockPerformanceCycle(tenantId, "Active Q1", "Active first quarter", "company-123", true, true, 
                        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31)),
                createMockPerformanceCycle(tenantId, "Active Q2", "Active second quarter", "company-123", true, true, 
                        LocalDate.of(2024, 4, 1), LocalDate.of(2024, 6, 30))
        );

        when(performanceCycleRepositoryV2.findActiveByTenantId(tenantId)).thenReturn(expectedCycles);

        // When
        List<PerformanceCycle> result = performanceCycleServiceV2.findActiveByTenantId(tenantId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(cycle -> cycle.getIsActive().equals(true));
        verify(performanceCycleRepositoryV2).findActiveByTenantId(tenantId);
    }

    @Test
    void shouldFindPerformanceCycleById() {
        // Given
        String id = "cycle-123";
        PerformanceCycle expectedCycle = createMockPerformanceCycle("tenant-123", "Q1 2024", "First quarter",
                "company-123", true, true, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

        when(performanceCycleRepositoryV2.findById(id)).thenReturn(Optional.of(expectedCycle));

        // When
        Optional<PerformanceCycle> result = performanceCycleServiceV2.findById(id);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Q1 2024");
        verify(performanceCycleRepositoryV2).findById(id);
    }

    @Test
    void shouldDeletePerformanceCycleById() {
        // Given
        String id = "cycle-123";

        // When
        performanceCycleServiceV2.deleteById(id);

        // Then
        verify(performanceCycleRepositoryV2).deleteById(id);
    }

    // Helper methods
    private Company createMockCompany(String companyId) {
        return Company.builder()
                .id(companyId)
                .name("Test Company")
                .email("test@company.com")
                .description("Test Company Description")
                .tenantId("tenant-123")
                .build();
    }

    private PerformanceCycle createMockPerformanceCycle(String tenantId, String name, String description,
                                                        String companyId, Boolean isActive, Boolean isTimeSensitive,
                                                        LocalDate startDate, LocalDate endDate) {
        return PerformanceCycle.builder()
                .id("cycle-" + System.nanoTime())
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .companyId(companyId)
                .isActive(isActive)
                .isTimeSensitive(isTimeSensitive)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
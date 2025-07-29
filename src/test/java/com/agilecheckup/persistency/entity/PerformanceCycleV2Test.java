package com.agilecheckup.persistency.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PerformanceCycleV2Test {

    @Test
    void shouldCreatePerformanceCycleV2WithBuilder() {
        // Given
        String id = "cycle-123";
        String tenantId = "tenant-123";
        String name = "Q1 2024 Performance Cycle";
        String description = "First quarter performance review cycle";
        String companyId = "company-123";
        Boolean isActive = true;
        Boolean isTimeSensitive = true;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);
        Instant now = Instant.now();

        // When
        PerformanceCycleV2 cycle = PerformanceCycleV2.builder()
                .id(id)
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .companyId(companyId)
                .isActive(isActive)
                .isTimeSensitive(isTimeSensitive)
                .startDate(startDate)
                .endDate(endDate)
                .createdDate(now)
                .lastUpdatedDate(now)
                .build();

        // Then
        assertThat(cycle.getId()).isEqualTo(id);
        assertThat(cycle.getTenantId()).isEqualTo(tenantId);
        assertThat(cycle.getName()).isEqualTo(name);
        assertThat(cycle.getDescription()).isEqualTo(description);
        assertThat(cycle.getCompanyId()).isEqualTo(companyId);
        assertThat(cycle.getIsActive()).isEqualTo(isActive);
        assertThat(cycle.getIsTimeSensitive()).isEqualTo(isTimeSensitive);
        assertThat(cycle.getStartDate()).isEqualTo(startDate);
        assertThat(cycle.getEndDate()).isEqualTo(endDate);
        assertThat(cycle.getCreatedDate()).isEqualTo(now);
        assertThat(cycle.getLastUpdatedDate()).isEqualTo(now);
    }

    @Test
    void shouldCreatePerformanceCycleV2WithoutOptionalFields() {
        // Given
        String tenantId = "tenant-123";
        String name = "Ongoing Performance Cycle";
        String description = "Continuous performance review";
        String companyId = "company-123";
        Boolean isActive = true;
        Boolean isTimeSensitive = false;

        // When
        PerformanceCycleV2 cycle = PerformanceCycleV2.builder()
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .companyId(companyId)
                .isActive(isActive)
                .isTimeSensitive(isTimeSensitive)
                .build();

        // Then
        assertThat(cycle.getTenantId()).isEqualTo(tenantId);
        assertThat(cycle.getName()).isEqualTo(name);
        assertThat(cycle.getDescription()).isEqualTo(description);
        assertThat(cycle.getCompanyId()).isEqualTo(companyId);
        assertThat(cycle.getIsActive()).isEqualTo(isActive);
        assertThat(cycle.getIsTimeSensitive()).isEqualTo(isTimeSensitive);
        assertThat(cycle.getStartDate()).isNull();
        assertThat(cycle.getEndDate()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenRequiredFieldsAreNull() {
        // When/Then - companyId is required
        assertThrows(NullPointerException.class, () -> 
            PerformanceCycleV2.builder()
                .tenantId("tenant-123")
                .name("Test Cycle")
                .description("Test Description")
                .companyId(null) // Required field
                .isActive(true)
                .isTimeSensitive(false)
                .build()
        );

        // When/Then - isActive is required
        assertThrows(NullPointerException.class, () -> 
            PerformanceCycleV2.builder()
                .tenantId("tenant-123")
                .name("Test Cycle")
                .description("Test Description")
                .companyId("company-123")
                .isActive(null) // Required field
                .isTimeSensitive(false)
                .build()
        );

        // When/Then - isTimeSensitive is required
        assertThrows(NullPointerException.class, () -> 
            PerformanceCycleV2.builder()
                .tenantId("tenant-123")
                .name("Test Cycle")
                .description("Test Description")
                .companyId("company-123")
                .isActive(true)
                .isTimeSensitive(null) // Required field
                .build()
        );
    }

    @Test
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        String id = "cycle-123";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 31);

        PerformanceCycleV2 cycle1 = PerformanceCycleV2.builder()
                .id(id)
                .tenantId("tenant-123")
                .name("Q1 2024")
                .description("First quarter")
                .companyId("company-123")
                .isActive(true)
                .isTimeSensitive(true)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        PerformanceCycleV2 cycle2 = PerformanceCycleV2.builder()
                .id(id)
                .tenantId("tenant-123")
                .name("Q1 2024")
                .description("First quarter")
                .companyId("company-123")
                .isActive(true)
                .isTimeSensitive(true)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // When/Then
        assertThat(cycle1).isEqualTo(cycle2);
        assertThat(cycle1.hashCode()).isEqualTo(cycle2.hashCode());
    }

    @Test
    void shouldImplementToStringCorrectly() {
        // Given
        PerformanceCycleV2 cycle = PerformanceCycleV2.builder()
                .id("cycle-123")
                .tenantId("tenant-123")
                .name("Q1 2024")
                .description("First quarter")
                .companyId("company-123")
                .isActive(true)
                .isTimeSensitive(true)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 3, 31))
                .build();

        // When
        String toString = cycle.toString();

        // Then
        assertThat(toString).contains("PerformanceCycleV2");
        assertThat(toString).contains("Q1 2024");
        assertThat(toString).contains("company-123");
        assertThat(toString).contains("2024-01-01");
    }

    @Test
    void shouldSupportBuilderBasedUpdates() {
        // Given
        PerformanceCycleV2 originalCycle = PerformanceCycleV2.builder()
                .id("cycle-123")
                .tenantId("tenant-123")
                .name("Q1 2024")
                .description("First quarter")
                .companyId("company-123")
                .isActive(true)
                .isTimeSensitive(false)
                .startDate(LocalDate.of(2024, 1, 1))
                .build();

        // When
        PerformanceCycleV2 modifiedCycle = PerformanceCycleV2.builder()
                .id(originalCycle.getId())
                .tenantId(originalCycle.getTenantId())
                .name("Q1 2024 - Updated")
                .description(originalCycle.getDescription())
                .companyId(originalCycle.getCompanyId())
                .isActive(originalCycle.getIsActive())
                .isTimeSensitive(true)
                .startDate(originalCycle.getStartDate())
                .endDate(LocalDate.of(2024, 3, 31))
                .build();

        // Then
        assertThat(modifiedCycle.getId()).isEqualTo(originalCycle.getId());
        assertThat(modifiedCycle.getTenantId()).isEqualTo(originalCycle.getTenantId());
        assertThat(modifiedCycle.getName()).isEqualTo("Q1 2024 - Updated");
        assertThat(modifiedCycle.getIsTimeSensitive()).isTrue();
        assertThat(modifiedCycle.getEndDate()).isEqualTo(LocalDate.of(2024, 3, 31));
        assertThat(modifiedCycle.getStartDate()).isEqualTo(originalCycle.getStartDate());
    }
}
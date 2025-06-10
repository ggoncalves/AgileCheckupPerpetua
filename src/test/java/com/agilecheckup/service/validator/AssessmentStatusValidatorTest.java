package com.agilecheckup.service.validator;

import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.service.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AssessmentStatusValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "INVITED, CONFIRMED",
        "INVITED, IN_PROGRESS",
        "CONFIRMED, IN_PROGRESS",
        "IN_PROGRESS, COMPLETED"
    })
    void shouldAllowValidTransitions(AssessmentStatus from, AssessmentStatus to) {
        assertDoesNotThrow(() -> AssessmentStatusValidator.validateTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
        "INVITED, COMPLETED",
        "CONFIRMED, COMPLETED",
        "CONFIRMED, INVITED",
        "IN_PROGRESS, INVITED",
        "IN_PROGRESS, CONFIRMED",
        "COMPLETED, INVITED",
        "COMPLETED, CONFIRMED",
        "COMPLETED, IN_PROGRESS",
        "COMPLETED, COMPLETED"
    })
    void shouldRejectInvalidTransitions(AssessmentStatus from, AssessmentStatus to) {
        assertThatThrownBy(() -> AssessmentStatusValidator.validateTransition(from, to))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Invalid status transition from " + from + " to " + to);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVITED", "CONFIRMED", "IN_PROGRESS", "COMPLETED"})
    void shouldAcceptValidStatusNames(String statusName) {
        assertDoesNotThrow(() -> AssessmentStatusValidator.validateStatus(statusName));
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "PENDING", "STARTED", "", "invited", "completed"})
    void shouldRejectInvalidStatusNames(String statusName) {
        assertThatThrownBy(() -> AssessmentStatusValidator.validateStatus(statusName))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Invalid assessment status: " + statusName);
    }

    @Test
    void shouldThrowExceptionForNullCurrentStatus() {
        assertThatThrownBy(() -> AssessmentStatusValidator.validateTransition(null, AssessmentStatus.INVITED))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionForNullNewStatus() {
        assertThatThrownBy(() -> AssessmentStatusValidator.validateTransition(AssessmentStatus.INVITED, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionForNullStatusName() {
        assertThatThrownBy(() -> AssessmentStatusValidator.validateStatus(null))
            .isInstanceOf(NullPointerException.class);
    }
}
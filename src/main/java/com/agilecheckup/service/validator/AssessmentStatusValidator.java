package com.agilecheckup.service.validator;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.agilecheckup.persistency.entity.AssessmentStatus;
import com.agilecheckup.service.exception.ValidationException;

import lombok.NonNull;

public class AssessmentStatusValidator {

  private static final Set<String> VALID_STATUS_NAMES = Arrays.stream(AssessmentStatus.values()).map(Enum::name).collect(Collectors.toSet());

  public static void validateTransition(@NonNull AssessmentStatus currentStatus, @NonNull AssessmentStatus newStatus) {
    if (!isValidTransition(currentStatus, newStatus)) {
      throw new ValidationException(
          String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
      );
    }
  }

  public static void validateStatus(@NonNull String statusName) {
    if (!VALID_STATUS_NAMES.contains(statusName)) {
      throw new ValidationException(
          String.format("Invalid assessment status: %s. Valid values are: %s", statusName, String.join(", ", VALID_STATUS_NAMES))
      );
    }
  }

  private static boolean isValidTransition(AssessmentStatus from, AssessmentStatus to) {
    switch (from) {
      case INVITED:
        return to == AssessmentStatus.CONFIRMED || to == AssessmentStatus.IN_PROGRESS;
      case CONFIRMED:
        return to == AssessmentStatus.IN_PROGRESS;
      case IN_PROGRESS:
        return to == AssessmentStatus.COMPLETED;
      case COMPLETED:
        return false; // No transitions allowed from COMPLETED
      default:
        return false;
    }
  }
}
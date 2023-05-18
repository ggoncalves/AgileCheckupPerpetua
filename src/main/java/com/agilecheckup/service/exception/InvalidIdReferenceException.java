package com.agilecheckup.service.exception;

public class InvalidIdReferenceException extends RuntimeException {
  public InvalidIdReferenceException(String id, String className, String idClassName) {
    this("Invalid " + idClassName + " id: " + id + " to be added into " + className);
  }

  public InvalidIdReferenceException(String message) {
    super(message);
  }
}

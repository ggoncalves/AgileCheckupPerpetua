package com.agilecheckup.service.exception;

public class InvalidIdReferenceException extends RuntimeException {
  public InvalidIdReferenceException(String id, String className, String idClassName) {
    this("Invalid - Not Found " + idClassName + " id: " + id + " in " + className);
  }

  public InvalidIdReferenceException(String id, String idClassName) {
    this("Invalid - Not Found " + idClassName + " id: " + id);
  }

  public InvalidIdReferenceException(String message) {
    super(message);
  }
}

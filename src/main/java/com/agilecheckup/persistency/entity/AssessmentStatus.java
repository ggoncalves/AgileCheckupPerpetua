package com.agilecheckup.persistency.entity;

public enum AssessmentStatus {

  // (Default) Assessment created but yet to be confirmed by User
  INVITED,

  // Clicked, opened and saved by User
  CONFIRMED,

  // User started the assessment by answering the questions
  IN_PROGRESS,

  // User finished the assessment
  COMPLETED
}

package com.agilecheckup.service.dto;

import com.agilecheckup.persistency.entity.question.Answer;
import com.agilecheckup.persistency.entity.question.Question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response containing the next unanswered question along with progress information.
 * Used in the employee assessment answering workflow to provide context about
 * the current state of the assessment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerWithProgressResponse {
  /**
   * The next unanswered question to be answered
   */
  private Question question;

  /**
   * Existing answer for this question, if any.
   * 
   * Currently, this field will always be null as the system only returns
   * truly unanswered questions. In future versions, this field may contain
   * partially saved answers (draft answers) that were started but not yet
   * submitted, allowing users to resume incomplete answers.
   */
  private Answer existingAnswer;

  /**
   * The number of questions already answered in this assessment
   */
  private Integer currentProgress;

  /**
   * The total number of questions in the assessment matrix
   */
  private Integer totalQuestions;
}
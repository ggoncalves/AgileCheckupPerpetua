package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.score.PillarScore;
import com.agilecheckup.persistency.entity.score.Scorable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@EqualsAndHashCode
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmployeeAssessmentScore implements Scorable {

  private Map<String, PillarScore> pillarIdToPillarScoreMap;
  private Double score;

}
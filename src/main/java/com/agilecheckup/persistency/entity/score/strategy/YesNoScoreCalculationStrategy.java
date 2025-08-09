package com.agilecheckup.persistency.entity.score.strategy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class YesNoScoreCalculationStrategy extends BooleanScoreCalculationStrategy {


}

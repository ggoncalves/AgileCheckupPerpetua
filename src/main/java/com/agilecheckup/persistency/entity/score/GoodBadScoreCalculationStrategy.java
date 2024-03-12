package com.agilecheckup.persistency.entity.score;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class GoodBadScoreCalculationStrategy extends BooleanScoreCalculationStrategy {


}

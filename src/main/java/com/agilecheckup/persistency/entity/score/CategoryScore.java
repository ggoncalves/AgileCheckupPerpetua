package com.agilecheckup.persistency.entity.score;

import com.agilecheckup.persistency.entity.base.BaseEntity;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class CategoryScore extends BaseEntity {

  @DynamoDBAttribute
  private String categoryId;

  @DynamoDBAttribute
  private String categoryName;

  @DynamoDBAttribute
  private List<QuestionScore> questionScores;

  @DynamoDBAttribute
  private Integer maxCategoryScore;
}

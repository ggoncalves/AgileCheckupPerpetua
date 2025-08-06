package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.DescribableEntityV2;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
@DynamoDBDocument
public class Category extends DescribableEntityV2 {


}

package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.DescribableEntityV2;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
@DynamoDbBean
public class CategoryV2 extends DescribableEntityV2 {

}
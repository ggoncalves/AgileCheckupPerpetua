package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.TenantDescribableEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public class PerformanceCycle extends TenantDescribableEntity {

    @NonNull
    @Getter(onMethod_ = @__({@DynamoDbAttribute("companyId"), @DynamoDbSecondaryPartitionKey(indexNames = "companyId-index")}))
    private String companyId;

    @NonNull
    @Getter(onMethod_ = @__(@DynamoDbAttribute("isTimeSensitive")))
    private Boolean isTimeSensitive;

    @NonNull
    @Getter(onMethod_ = @__(@DynamoDbAttribute("isActive")))
    private Boolean isActive;

    @Getter(onMethod_ = @__(@DynamoDbAttribute("startDate")))
    private LocalDate startDate;

    @Getter(onMethod_ = @__(@DynamoDbAttribute("endDate")))
    private LocalDate endDate;
}
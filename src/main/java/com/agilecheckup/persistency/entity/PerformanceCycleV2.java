package com.agilecheckup.persistency.entity;

import com.agilecheckup.persistency.entity.base.TenantDescribableEntityV2;
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
public class PerformanceCycleV2 extends TenantDescribableEntityV2 {

    @NonNull
    private String companyId;

    @NonNull
    private Boolean isTimeSensitive;

    @NonNull
    private Boolean isActive;

    private LocalDate startDate;

    private LocalDate endDate;

    @DynamoDbAttribute("companyId")
    @DynamoDbSecondaryPartitionKey(indexNames = "companyId-index")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbAttribute("isTimeSensitive")
    public Boolean getIsTimeSensitive() {
        return isTimeSensitive;
    }

    @DynamoDbAttribute("isActive")
    public Boolean getIsActive() {
        return isActive;
    }

    @DynamoDbAttribute("startDate")
    public LocalDate getStartDate() {
        return startDate;
    }

    @DynamoDbAttribute("endDate")
    public LocalDate getEndDate() {
        return endDate;
    }
}
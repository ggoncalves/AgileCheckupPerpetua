package com.agilecheckup.persistency.entity.person;

import com.agilecheckup.persistency.entity.base.BaseEntityV2;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DynamoDbBean
public class AddressV2 extends BaseEntityV2 {

    @NonNull
    @Getter(onMethod_=@__(@DynamoDbAttribute("street")))
    private String street;

    @NonNull
    @Getter(onMethod_=@__(@DynamoDbAttribute("city")))
    private String city;

    @NonNull
    @Getter(onMethod_=@__(@DynamoDbAttribute("state")))
    private String state;

    @NonNull
    @Getter(onMethod_=@__(@DynamoDbAttribute("zipcode")))
    private String zipcode;

    @NonNull
    @Getter(onMethod_=@__(@DynamoDbAttribute("country")))
    private String country;
}
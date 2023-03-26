package com.example.storemonitoring.model.store_status.primary_key;


import lombok.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StoreStatusPrimaryKey implements Serializable {

    private String storeId;
    private String timestampUTC;

}

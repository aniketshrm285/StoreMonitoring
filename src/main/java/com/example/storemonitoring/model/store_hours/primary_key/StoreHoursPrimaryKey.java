package com.example.storemonitoring.model.store_hours.primary_key;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StoreHoursPrimaryKey implements Serializable {

    private String storeId;
    private Integer day;

}

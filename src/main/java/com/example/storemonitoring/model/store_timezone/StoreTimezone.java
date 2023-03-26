package com.example.storemonitoring.model.store_timezone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "STORE_TIMEZONE")
public class StoreTimezone {
    @Id
    @Column(name = "store_id")
    private String storeId;

    @Column(name = "timezone_str")
    private String timezoneStr;

}

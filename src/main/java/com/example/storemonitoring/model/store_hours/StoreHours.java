package com.example.storemonitoring.model.store_hours;

import com.example.storemonitoring.model.store_hours.primary_key.StoreHoursPrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "STORE_HOURS")
@IdClass(StoreHoursPrimaryKey.class)
public class StoreHours {
    @Id
    @Column(name = "store_id")
    private String storeId;

    @Id
    @Column(name = "day")
    private Integer day;

    @Column(name = "start_time_local")
    private String startTimeLocal;

    @Column(name = "end_time_local")
    private String endTimeLocal;
}

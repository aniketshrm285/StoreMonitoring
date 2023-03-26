package com.example.storemonitoring.model.store_status;

import com.example.storemonitoring.model.store_status.primary_key.StoreStatusPrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "STORE_STATUS")
@IdClass(StoreStatusPrimaryKey.class)
public class StoreStatus {

    @Id
    @Column(name = "store_id")
    private String storeId;

    @Column(name = "status")
    private String status;

    @Id
    @Column(name = "timestamp_utc")
    private String timestampUTC;
}

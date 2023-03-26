package com.example.storemonitoring.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ReportModel {
    @CsvBindByName(column = "store_id", required = true)
    @CsvBindByPosition(position = 0)
    private String storeId;
    @CsvBindByName(column = "uptime_last_hour", required = true)
    @CsvBindByPosition(position = 1)
    private int uptimeLastHour;
    @CsvBindByName(column = "uptime_last_day", required = true)
    @CsvBindByPosition(position = 2)
    private int uptimeLastDay;
    @CsvBindByName(column = "update_last_week", required = true)
    @CsvBindByPosition(position = 3)
    private int uptimeLastWeek;
    @CsvBindByName(column = "downtime_last_hour", required = true)
    @CsvBindByPosition(position = 4)
    private int downtimeLastHour;
    @CsvBindByName(column = "downtime_last_day", required = true)
    @CsvBindByPosition(position = 5)
    private int downtimeLastDay;
    @CsvBindByName(column = "downtime_last_week", required = true)
    @CsvBindByPosition(position = 6)
    private int downtimeLastWeek;

    public ReportModel(String storeId, int uptimeLastHour, int uptimeLastDay, int uptimeLastWeek, int downtimeLastHour, int downtimeLastDay, int downtimeLastWeek) {
        this.storeId = storeId;
        this.uptimeLastHour = uptimeLastHour;
        this.uptimeLastDay = uptimeLastDay;
        this.uptimeLastWeek = uptimeLastWeek;
        this.downtimeLastHour = downtimeLastHour;
        this.downtimeLastDay = downtimeLastDay;
        this.downtimeLastWeek = downtimeLastWeek;
    }
}

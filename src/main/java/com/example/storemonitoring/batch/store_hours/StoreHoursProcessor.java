package com.example.storemonitoring.batch.store_hours;

import com.example.storemonitoring.model.store_hours.StoreHours;
import org.springframework.batch.item.ItemProcessor;

public class StoreHoursProcessor implements ItemProcessor<StoreHours, StoreHours> {
    @Override
    public StoreHours process(StoreHours storeHours) throws Exception {
        return storeHours;
    }
}

package com.example.storemonitoring.batch.store_timezone;

import com.example.storemonitoring.model.store_timezone.StoreTimezone;
import org.springframework.batch.item.ItemProcessor;

public class StoreTimezoneProcessor implements ItemProcessor<StoreTimezone, StoreTimezone> {
    @Override
    public StoreTimezone process(StoreTimezone storeTimezone) throws Exception {
        return storeTimezone;
    }
}

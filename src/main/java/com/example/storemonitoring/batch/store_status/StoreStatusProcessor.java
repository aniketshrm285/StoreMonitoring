package com.example.storemonitoring.batch.store_status;

import com.example.storemonitoring.model.store_status.StoreStatus;
import org.springframework.batch.item.ItemProcessor;

public class StoreStatusProcessor implements ItemProcessor<StoreStatus, StoreStatus> {
    @Override
    public StoreStatus process(StoreStatus storeStatus) throws Exception {
        return storeStatus;
    }
}

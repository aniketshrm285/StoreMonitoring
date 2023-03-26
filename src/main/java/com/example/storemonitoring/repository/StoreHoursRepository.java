package com.example.storemonitoring.repository;

import com.example.storemonitoring.model.store_hours.StoreHours;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreHoursRepository extends JpaRepository<StoreHours, String> {
    StoreHours findByStoreIdAndDay(String storeId, Integer day);
}

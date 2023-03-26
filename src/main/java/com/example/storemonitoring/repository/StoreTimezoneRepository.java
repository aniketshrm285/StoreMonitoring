package com.example.storemonitoring.repository;

import com.example.storemonitoring.model.store_timezone.StoreTimezone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreTimezoneRepository extends JpaRepository<StoreTimezone, String> {
    StoreTimezone findByStoreId(String storeId);
}

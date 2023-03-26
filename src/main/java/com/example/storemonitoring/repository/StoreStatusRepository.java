package com.example.storemonitoring.repository;

import com.example.storemonitoring.model.store_status.StoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StoreStatusRepository extends JpaRepository<StoreStatus, String> {
    List<StoreStatus> findByTimestampUTCGreaterThanEqualOrderByTimestampUTCDesc(String timestampUTC);
}

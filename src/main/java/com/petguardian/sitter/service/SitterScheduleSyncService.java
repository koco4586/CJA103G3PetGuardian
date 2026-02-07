package com.petguardian.sitter.service;

import java.util.Map;

public interface SitterScheduleSyncService {

    /**
     * 同步所有訂單至排程表
     * 處理狀態 0, 1, 2, 5 為 Booked
     * 處理狀態 3, 4 為 Free
     * 
     * @return 包含同步結果的 Map (message, processedOrders, totalOrders)
     */
    Map<String, Object> syncSchedule();
}

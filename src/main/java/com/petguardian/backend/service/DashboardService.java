package com.petguardian.backend.service;

import java.util.Map;

/**
 * 後台儀表板統計 Service 介面
 * 提供後台首頁所需的統計數據
 */
public interface DashboardService {

    /**
     * 取得儀表板統計數據
     * 回傳包含 11 項關鍵指標的 Map
     */
    Map<String, Object> getDashboardStatistics();
}
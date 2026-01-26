package com.petguardian.backend.service;

import java.util.Map;

/**
 * 後台儀表板統計 Service 介面
 * 提供後台首頁所需的統計數據
 */
public interface DashboardService {

    /**
     * 取得儀表板統計數據
     * 包含: 會員總數、待審保母、待處理退款、待處理評價
     */
    Map<String, Object> getDashboardStatistics();
}
package com.petguardian.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 保母端操作服務
 * 職責：處理保母對訂單的操作（接單、拒單、完成服務等）
 */
@Service
@Transactional
public class BookingSitterOperationService {

    @Autowired
    private BookingScheduleInternalService scheduleInternalService;

    /**
     * 保母更新訂單狀態
     * 處理流程：
     * 1. 委託給 scheduleInternalService 處理狀態更新
     * 2. scheduleInternalService 會同步更新保母的排程狀態
     * 常見狀態：
     * - 0: 待確認
     * - 1: 進行中（已接單）
     * - 2: 已完成服務
     * - 3: 申請退款中（保母拒單）
     */
    public void updateOrderStatusBySitter(Integer orderId, Integer newStatus) {
        // 委託給排程內部服務處理
        // 該服務會同時處理訂單狀態更新和保母排程更新
        scheduleInternalService.updateOrderStatusBySitter(orderId, newStatus);
    }
}
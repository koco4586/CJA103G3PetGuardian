package com.petguardian.sitter.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.petguardian.booking.model.BookingScheduleVO;
import com.petguardian.sitter.model.SitterVO;

public interface SitterScheduleService {

    /**
     * 更新保姆服務時間（營業時間設定）
     * 
     * @param sitterId    保姆編號
     * @param serviceTime 服務時間（24字元字串，0=不可預約, 1=可預約）
     * @return SitterVO 更新後的保姆物件
     */
    SitterVO updateServiceTime(Integer sitterId, String serviceTime);

    /**
     * 取得會員(保姆)的月行程
     * 
     * @param memId 會員編號
     * @param year  年份
     * @param month 月份
     * @return List<BookingScheduleVO> 該月行程
     */
    List<BookingScheduleVO> getScheduleByMember(Integer memId, int year, int month);

    /**
     * 更新會員(保姆)的單日行程
     * 
     * @param memId  會員編號
     * @param date   日期
     * @param status 24小時狀態字串
     */
    void updateScheduleForMember(Integer memId, LocalDate date, String status);

    /**
     * 更新保姆的一週行程 (從前端傳來的複雜 JSON 資料解析並儲存)
     * 
     * @param sitterId     保姆編號
     * @param scheduleData 前端傳來的一週行程資料
     */
    void updateWeeklySchedule(Integer sitterId, Map<String, Map<String, String>> scheduleData);
}

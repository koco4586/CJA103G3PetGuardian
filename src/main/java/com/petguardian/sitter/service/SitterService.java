package com.petguardian.sitter.service;

import java.util.List;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.sitter.model.SitterMemberVO;
import com.petguardian.sitter.model.SitterSearchCriteria;
import com.petguardian.sitter.model.SitterSearchDTO;
import com.petguardian.sitter.model.SitterDashboardDTO;
import com.petguardian.booking.model.BookingScheduleVO;
import com.petguardian.booking.model.BookingOrderVO;
import java.time.LocalDate;

public interface SitterService {

    /**
     * 新增保姆
     * 
     * @param memId      會員編號
     * @param sitterName 保姆姓名
     * @param sitterAdd  服務地址
     * @return SitterVO 新增的保姆物件
     */
    SitterVO createSitter(Integer memId, String sitterName, String sitterAdd);

    /**
     * 依會員編號查詢保姆 (會員查詢自己是否為保姆)
     * 
     * @param memId 會員編號
     * @return SitterVO 該會員的保姆資料
     */
    SitterVO getSitterByMemId(Integer memId);

    /**
     * 依保姆編號查詢 (會員查看特定保姆詳情)
     * 
     * @param sitterId 保姆編號
     * @return SitterVO
     */
    SitterVO getSitterById(Integer sitterId);

    /**
     * 查詢所有保姆 (會員瀏覽保姆列表)
     * 
     * @return List<SitterVO>
     */
    List<SitterVO> getAllSitters();

    /**
     * 依狀態查詢保姆 (會員查詢啟用中的保姆)
     * 
     * @param status 保姆狀態 (0:啟用, 1:停用)
     * @return List<SitterVO>
     */
    List<SitterVO> getSittersByStatus(Byte status);

    /**
     * 更新保姆狀態 (管理員或保姆自己)
     * 
     * @param sitterId 保姆編號
     * @param status   新狀態
     * @return SitterVO 更新後的保姆物件
     */
    SitterVO updateSitterStatus(Integer sitterId, Byte status);

    /**
     * 更新保姆資訊 (保姆自己)
     * 
     * @param sitterId   保姆編號
     * @param sitterName 保姆姓名
     * @param sitterAdd  服務地址
     * @return SitterVO 更新後的保姆物件
     */
    SitterVO updateSitterInfo(Integer sitterId, String sitterName, String sitterAdd);

    /**
     * 更新保姆服務時間（營業時間設定）
     * 
     * @param sitterId    保姆編號
     * @param serviceTime 服務時間（24字元字串，0=不可預約, 1=可預約）
     * @return SitterVO 更新後的保姆物件
     */
    SitterVO updateServiceTime(Integer sitterId, String serviceTime);

    // ========== 排程相關功能 (透過會員 ID) ==========

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

    // ========== 以下為會員搜尋保姆功能新增的方法 ==========

    /**
     * 根據條件搜尋保姆
     * 
     * @param criteria 搜尋條件
     * @return List<SitterSearchDTO> 符合條件的保姆列表
     */
    List<SitterSearchDTO> searchSitters(SitterSearchCriteria criteria);

    /**
     * 取得所有啟用中的保姆（用於無篩選條件時）
     * 
     * @return List<SitterSearchDTO> 所有啟用中的保姆列表
     */
    List<SitterSearchDTO> getAllActiveSitters();

    /**
     * 依會員 ID 查詢會員資訊 (用於保姆模組)
     * 分離 Repository 與 Controller 的相依性
     * 
     * @param memId 會員編號
     * @return SitterMemberVO
     */
    SitterMemberVO getSitterMemberById(Integer memId);

    /**
     * 更新保姆的一週行程 (從前端傳來的複雜 JSON 資料解析並儲存)
     * 
     * @param sitterId     保姆編號
     * @param scheduleData 前端傳來的一週行程資料
     */
    void updateWeeklySchedule(Integer sitterId, java.util.Map<String, java.util.Map<String, String>> scheduleData);

    /**
     * [Refactor] 取得保姆儀表板所需的整合資料
     * 
     * @param memId 會員編號
     * @return SitterDashboardDTO 整合後的資料物件 (若非保姆則回傳 null)
     */
    SitterDashboardDTO getDashboardData(Integer memId);

    /**
     * 取得保姆的歷史評價 (僅包含有文字評論的訂單)
     * 
     * @param sitterId 保姆編號
     * @return List&lt;BookingOrderVO&gt; 評價列表
     */
    List<BookingOrderVO> getSitterReviews(Integer sitterId);
}

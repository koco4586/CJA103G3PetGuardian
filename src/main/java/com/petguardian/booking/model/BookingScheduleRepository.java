package com.petguardian.booking.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface BookingScheduleRepository extends JpaRepository<BookingScheduleVO, Integer> {
    // 使用 PESSIMISTIC_WRITE 確保在讀取排程準備更新時，其他執行緒不能修改它
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BookingScheduleVO s WHERE s.sitterId = :sitterId AND s.scheduleDate = :date")
    /**
     * 核心功能：找尋該保姆在特定日期的排程
     * Optional 處理「如果當天還沒有排程紀錄」的情況
     */

    Optional<BookingScheduleVO> findBySitterIdAndScheduleDate(@Param("sitterId") Integer sitterId,
            @Param("date") LocalDate Date);

    /**
     * [NEW] 依據保姆ID與日期區間查詢排程
     * 用於優化月曆顯示效能，直接撈取特定月份的資料，避免全表掃描
     * 
     * @param sitterId  保姆編號
     * @param startDate 開始日期 (通常是當月1號)
     * @param endDate   結束日期 (通常是當月最後一天)
     * @return 該區間內的排程列表
     */
    List<BookingScheduleVO> findBySitterIdAndScheduleDateBetween(Integer sitterId, LocalDate startDate,
            LocalDate endDate);
}

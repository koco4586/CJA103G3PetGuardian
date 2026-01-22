package com.petguardian.booking.model;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingScheduleRepository extends JpaRepository<BookingScheduleVO, Integer> {
    
    /**
     * 核心功能：找尋該保姆在特定日期的排程
     * Optional 可以優雅地處理「如果當天還沒有排程紀錄」的情況
     */
    Optional<BookingScheduleVO> findBySitterIdAndScheduleDate(Integer sitterId, LocalDate scheduleDate);
}

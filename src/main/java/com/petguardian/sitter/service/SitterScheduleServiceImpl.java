package com.petguardian.sitter.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingScheduleRepository;
import com.petguardian.booking.model.BookingScheduleVO;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;

@Service
public class SitterScheduleServiceImpl implements SitterScheduleService {

    @Autowired
    private SitterRepository repository;

    @Autowired
    private BookingScheduleRepository bookingScheduleRepository;

    /**
     * 更新保姆服務時間（營業時間設定）
     * 
     * @param sitterId    保姆編號
     * @param serviceTime 服務時間（24字元字串，0=不可預約, 1=可預約）
     * @return SitterVO 更新後的保姆物件
     * @throws IllegalArgumentException 若保姆不存在
     */
    @Override
    @Transactional
    public SitterVO updateServiceTime(Integer sitterId, String serviceTime) {
        System.out.println("=== Service 層 Debug (Direct Update) ===");
        System.out.println("收到 sitterId: " + sitterId);
        System.out.println("收到 serviceTime: " + serviceTime);

        // 使用自定義的 JPQL 更新，繞過 JPA 的髒檢查機制確保寫入
        repository.updateServiceTime(sitterId, serviceTime);

        // 重新查詢以確認更新
        Optional<SitterVO> optional = repository.findById(sitterId);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("保姆不存在: " + sitterId);
        }

        SitterVO saved = optional.get();
        System.out.println("DB 更新後 serviceTime: " + saved.getServiceTime());
        System.out.println("================================");

        return saved;
    }

    // ========== 排程相關功能 (透過會員 ID) ==========

    @Override
    @Transactional(readOnly = true)
    public List<BookingScheduleVO> getScheduleByMember(Integer memId, int year, int month) {
        SitterVO sitter = repository.findByMemId(memId);
        if (sitter == null) {
            throw new IllegalArgumentException("會員尚未成為保姆");
        }
        Integer sitterId = sitter.getSitterId();

        // 由於 BookingScheduleRepository 只有 findAll，暫時用 Java filter
        List<BookingScheduleVO> allSchedules = bookingScheduleRepository.findAll();

        return allSchedules.stream()
                .filter(s -> s.getSitterId().equals(sitterId))
                .filter(s -> {
                    LocalDate d = s.getScheduleDate();
                    return d.getYear() == year && d.getMonthValue() == month;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateScheduleForMember(Integer memId, LocalDate date, String status) {
        SitterVO sitter = repository.findByMemId(memId);
        if (sitter == null) {
            throw new IllegalArgumentException("會員尚未成為保姆");
        }
        Integer sitterId = sitter.getSitterId();

        // 檢查該日期是否已有資料 (使用 Repository 現有方法)
        Optional<BookingScheduleVO> existingOpt = bookingScheduleRepository.findBySitterIdAndScheduleDate(sitterId,
                date);

        BookingScheduleVO schedule;
        if (existingOpt.isPresent()) {
            schedule = existingOpt.get();
            schedule.setBookingStatus(status);
        } else {
            schedule = new BookingScheduleVO();
            schedule.setSitterId(sitterId);
            schedule.setScheduleDate(date);
            schedule.setBookingStatus(status);
        }
        bookingScheduleRepository.save(schedule);
    }

    /**
     * 更新保姆的一週行程 (從前端傳來的複雜 JSON 資料解析並儲存)
     */
    @Override
    @Transactional
    public void updateWeeklySchedule(Integer sitterId, Map<String, Map<String, String>> scheduleData) {
        // 1. 建立 24 小時的狀態字串（合併七天的資料）
        char[] serviceTimeArray = new char[24];
        // 初始化為全部不可預約
        for (int i = 0; i < 24; i++) {
            serviceTimeArray[i] = '0';
        }

        // 2. 遍歷七天的資料
        for (int day = 0; day < 7; day++) {
            String dayKey = String.valueOf(day);
            if (scheduleData.containsKey(dayKey)) {
                Map<String, String> daySchedule = scheduleData.get(dayKey);

                for (int hour = 0; hour < 24; hour++) {
                    String hourStr = String.valueOf(hour);
                    if (daySchedule.containsKey(hourStr)) {
                        String status = daySchedule.get(hourStr);
                        // 0: 可預約 (前端傳來的狀態)
                        // service_time: 0=不可預約, 1=可預約 (資料庫儲存的狀態)
                        if ("0".equals(status)) {
                            // 只要任何一天這個時段是可預約，就設為可預約
                            serviceTimeArray[hour] = '1';
                        }
                    }
                }
            }
        }

        String serviceTime = new String(serviceTimeArray);

        // 3. 更新資料庫
        updateServiceTime(sitterId, serviceTime);
    }
}

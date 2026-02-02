package com.petguardian.booking.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.petsitter.model.PetSitterServiceVO;

/**
 * 訂單建立服務
 * 職責：處理新訂單的建立流程，包括資料驗證、時間檢查、衝突檢查、金額計算、排程更新
 */
@Service
@Transactional
public class BookingCreateService {

    @Autowired
    private BookingOrderRepository orderRepository;

    @Autowired
    private BookingScheduleInternalService scheduleInternalService;

    @Autowired
    private BookingDataIntegrationService dataService;

    /**
     * 建立預約訂單
     * 處理流程：
     * 1. 驗證寵物和保母服務資料
     * 2. 檢查時間邏輯（結束時間需晚於開始時間、不可跨日等）
     * 3. 檢查保母時段是否有衝突
     * 4. 計算預約金額
     * 5. 設定訂單狀態為待確認
     * 6. 存檔並更新保母排程（包含緩衝時間）
     */
    public BookingOrderVO createBooking(BookingOrderVO order) {
        // 步驟 1：外部資料驗證 
        // 驗證寵物是否存在且屬於該會員
        dataService.validateAndGetPet(order.getPetId(), order.getMemId());
        
        // 取得保母服務資訊（用於計算價格）
        PetSitterServiceVO sitterService = dataService.getSitterServiceInfo(
                order.getSitterId(),
                order.getServiceItemId()
        );

        // 步驟 2：時間與業務邏輯檢查 
        validateTimeLogic(order);

        //  步驟 3：檢查時段衝突
        // 計算請求的時段位元遮罩
        int requestedBits = scheduleInternalService.calculateBits(
                order.getStartTime(),
                order.getEndTime()
        );
        
        // 取得保母當日的已預約時段位元遮罩
        int currentSchedule = scheduleInternalService.getSitterScheduleBits(
                order.getSitterId(),
                order.getStartTime().toLocalDate()
        );

        // 檢查時段是否有重疊（使用位元運算）
        if ((currentSchedule & requestedBits) != 0) {
            throw new IllegalArgumentException("該時段保母已有其他預約。");
        }

        // 步驟 4：金額計算與狀態設定 
        // 計算服務時數（至少1小時）
        long hours = Math.max(1, Duration.between(order.getStartTime(), order.getEndTime()).toHours());
        
        // 設定預約金額（時數 × 單價）
        order.setReservationFee((int) (hours * sitterService.getDefaultPrice()));
        
        // 設定訂單狀態為待確認
        order.setOrderStatus(0);

        // 步驟 5：存檔並更新排程
        BookingOrderVO savedOrder = orderRepository.save(order);

        // 步驟 6：建立緩衝時間訂單（服務結束後多鎖1小時）
        BookingOrderVO bufferOrder = new BookingOrderVO();
        bufferOrder.setSitterId(savedOrder.getSitterId());
        bufferOrder.setBookingOrderId(savedOrder.getBookingOrderId());
        bufferOrder.setStartTime(savedOrder.getStartTime());
        bufferOrder.setEndTime(savedOrder.getEndTime().plusHours(1)); // 結束時間延長1小時

        // 更新保母排程：標記為預約中（包含正式時段和緩衝時段）
        scheduleInternalService.updateSitterSchedule(savedOrder, '2');  // '2' 表示預約中
        scheduleInternalService.updateSitterSchedule(bufferOrder, '2'); // 緩衝時段也標記為預約中

        return savedOrder;
    }

    /**
     * 驗證訂單時間的業務邏輯
     * 1. 結束時間必須晚於開始時間
     * 2. 不可跨日（開始和結束必須在同一天）
     * 3. 需提前2小時預約
     */
    private void validateTimeLogic(BookingOrderVO order) {
        LocalDateTime now = LocalDateTime.now();

        // 檢查：結束時間必須晚於開始時間
        if (!order.getEndTime().isAfter(order.getStartTime())) {
            throw new IllegalArgumentException("結束時間錯誤");
        }

        // 檢查：需提前1小時預約（目前已註解，可依需求啟用）
         if (order.getStartTime().isBefore(now.plusHours(1))) {
             throw new IllegalArgumentException("需兩小時前預約");
         }

        // 檢查：不可跨日（開始日期和結束日期必須相同）
        if (!order.getStartTime().toLocalDate().isEqual(order.getEndTime().toLocalDate())) {
            throw new IllegalArgumentException("不可跨日");
        }
    }
}
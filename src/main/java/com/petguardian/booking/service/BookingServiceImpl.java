package com.petguardian.booking.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.*;

@Service
@Transactional
public class BookingServiceImpl implements BookingService, BookingExternalDataService {

	@Autowired
    private BookingOrderRepository orderRepository;

    @Autowired
    private BookingScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleService scheduleService; // 負責處理 24 小時排程字串位元運算

    /**
     * 【功能：查詢會員訂單列表】
     */
    @Override
    public List<BookingOrderVO> getOrdersByMemberId(Integer memId) {
        // 這裡要呼叫 Repository 去資料庫撈資料
        return orderRepository.findByMemId(memId);
    }
    /**
     * 【功能：查詢單筆預約詳情】 
     */
    @Override
    public BookingOrderVO getOrderById(Integer orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }
    
    /**
     * 【功能：建立新預約】
     * 包含時間有效性檢查、自動計算服務費用、存檔訂單並自動佔用保母排程時段。
     * * @param order 包含預約基本資訊的 VO 物件
     * @return 儲存成功後含 ID 與計算金額的訂單物件
     * @throws IllegalArgumentException 當時間邏輯錯誤時拋出
     * 2 小時緩衝期、禁止跨日、禁止過期預約。
     */
    @Override
    public BookingOrderVO createBooking(BookingOrderVO order) {
    	LocalDateTime now = LocalDateTime.now();
    	
        // 1. 時間邏輯檢查
        if (order.getEndTime().isBefore(order.getStartTime())) {
            throw new IllegalArgumentException("預約失敗：結束時間不能早於開始時間。");
        }
        
        // 2. 限制：最晚要在 2 小時前預約 (給保母準備時間)
        if (order.getStartTime().isBefore(now.plusHours(2))) {
            throw new IllegalArgumentException("預約失敗：請至少於服務開始 2 小時前預約。");
        }
        
        // 3. 限制：不能預約超過 60 天後的時段 (避免過期過久的單)
        if (order.getStartTime().isAfter(now.plusDays(60))) {
            throw new IllegalArgumentException("預約失敗：僅開放 60 天內的預約。");
        }

        // 4. 限制：單筆預約禁止跨日 (避免 24h 位元字串運算錯誤)
        if (!order.getStartTime().toLocalDate().isEqual(order.getEndTime().toLocalDate())) {
            throw new IllegalArgumentException("預約失敗：單筆預約不可跨日，若有需求請拆分為多筆訂單。");
        }

        // 5. 獲取服務定價 (從保母那邊透過Interface獲取)
        SitterDTO sitter = getSitterInfo(order.getSitterId(), order.getServiceItemId());

        // 6. 衝突檢查：檢查該時段保母是否已經有約
        // 先計算這次預約想要佔用的位元 (例如: 2:00~4:00 會得到一個整數)
        int requestedBits = calculateBits(order.getStartTime(), order.getEndTime());
        
        // 取得保母當天的排程 (這部分需實作，或從資料庫抓取現有訂單算出)
        int currentSchedule = getSitterScheduleBits(order.getSitterId(), order.getStartTime().toLocalDate());
        
        // 使用位元 AND 運算：如果結果不為 0，代表時段重疊
        if ((currentSchedule & requestedBits) != 0) {
            throw new IllegalArgumentException("預約失敗：該時段保母已有其他預約，請選擇其他時間。");
        }
        
        // 7. 計算總金額：不足一小時以一小時計
        long hours = Math.max(1, Duration.between(order.getStartTime(), order.getEndTime()).toHours());
        order.setReservationFee((int) (hours * sitter.getPrice()));

        // 8. 設定訂單初始狀態 (0: 待確認/待支付)
        order.setOrderStatus(0);

        // 9. 執行訂單存檔
        BookingOrderVO savedOrder = orderRepository.save(order);

        // 10. 同步更新保母排程字串 (將對應時段設為 '1' 佔用)
        updateSitterSchedule(savedOrder);

        return savedOrder;
    }
    
    /**
     * 【功能：取消預約】
     */
    @Override
    public void cancelBooking(Integer orderId, String reason) {
        BookingOrderVO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到該筆預約訂單"));
        
        order.setOrderStatus(3); // 3: 已取消
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        orderRepository.save(order);
    }

    /**
     * 【更新保母排程】
     * 根據訂單時間區間，修改保母當日的 24 位元排程字串。
     */
    private void updateSitterSchedule(BookingOrderVO order) {
        LocalDate date = order.getStartTime().toLocalDate();
        int startH = order.getStartTime().getHour();
        int endH = order.getEndTime().getHour();

        // 查找該保母當日是否已有排程紀錄，若無則初始化全天為 '0' (空閒)
        BookingScheduleVO schedule = scheduleRepository.findBySitterIdAndScheduleDate(order.getSitterId(), date)
                .orElseGet(() -> {
                    BookingScheduleVO n = new BookingScheduleVO();
                    n.setSitterId(order.getSitterId());
                    n.setScheduleDate(date);
                    n.setBookingStatus("000000000000000000000000"); // 24小時空閒狀態
                    return n;
                });

        // 呼叫位元工具類，將開始小時到結束小時的字串位置改為 '1'
        String newStatus = scheduleService.updateStatusString(schedule.getBookingStatus(), startH, endH, '1');
        schedule.setBookingStatus(newStatus);
        schedule.setBookingOrderId(order.getBookingOrderId()); // 關聯最新訂單編號
        
        scheduleRepository.save(schedule);
    }

    /**
     * 【功能：核准退款申請】
     * 修改訂單狀態為已退款，並將原本佔用的保母時段歸還(變回空閒)。
     * * @param orderId 欲退款的訂單編號
     */
    @Override
    public void approveRefund(Integer orderId) {
        // 1. 查找訂單並更新狀態為 4 (已退款)
        BookingOrderVO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("系統錯誤：找不到此訂單編號。"));
        
        order.setOrderStatus(4); 
        orderRepository.save(order);

        // 2. 釋出該時段排程紀錄 (將對應位置由 '1' 改回 '0')
        LocalDate date = order.getStartTime().toLocalDate();
        scheduleRepository.findBySitterIdAndScheduleDate(order.getSitterId(), date)
                .ifPresent(schedule -> {
                    String updatedStatus = scheduleService.updateStatusString(
                        schedule.getBookingStatus(), 
                        order.getStartTime().getHour(), 
                        order.getEndTime().getHour(), 
                        '0' // 設為空閒
                    );
                    schedule.setBookingStatus(updatedStatus);
                    scheduleRepository.save(schedule);
                });
    }
    
    /**
     * 從資料庫找出該保母當天所有「未取消」的訂單，並加總成一個 24 位元的排程整數。
     */
    private int getSitterScheduleBits(Integer sitterId, LocalDate date) {
        // 優化：使用隔天凌晨作為結束點，更安全
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay(); 

        List<BookingOrderVO> existingOrders = orderRepository
            .findBySitterIdAndStartTimeBetween(sitterId, startOfDay, endOfDay);
        
        int totalSchedule = 0;
        for (BookingOrderVO order : existingOrders) {
            // 狀態 0(待確認)、1(已支付)、2(服務完成)、3(已取消)、4(已退款)、5(已結案) 都應該算佔用
            if (order.getOrderStatus() != 3 && order.getOrderStatus() != 4) { 
                totalSchedule |= calculateBits(order.getStartTime(), order.getEndTime());
            }
        }
        return totalSchedule;
    }

    /**
     * 【功能：後台執行撥款】
     * 當服務完成(狀態2)後，由管理員點擊執行，將款項撥付給保母並結案。
     * * @param orderId 欲結案撥款的訂單編號
     */
    @Override
    public void completePayout(Integer orderId) {
        // 1. 查找訂單紀錄
        BookingOrderVO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("系統錯誤：找不到此訂單編號。"));

        // 2. 流程檢查：非「服務完成」狀態不可撥款
        if (order.getOrderStatus() != 2) {
            throw new RuntimeException("訂單必須處於『服務完成』狀態才可執行撥款。");
        }

        // 3. 更新狀態為 5 (已撥款/全案結束)
        order.setOrderStatus(5);
        orderRepository.save(order);
        
        //可在此接虛擬錢包
    }
//	以下測試用
	
    @Override
    public MemberDTO getMemberInfo(Integer memId) {
        return new MemberDTO(memId, "開發測試員(" + memId + ")");
    }

    @Override
    public PetDTO getPetInfo(Integer petId) {
        return new PetDTO(petId, "測試小毛孩", "黃金獵犬");
    }

    @Override
    public SitterDTO getSitterInfo(Integer sitterId, Integer serviceItemId) {
        return new SitterDTO(sitterId, "假資料保母", serviceItemId, 500);
    }
    /**
     * 將時間轉換為 24 位元的整數。
     * 14:00 ~ 16:00 會在第 14, 15 位元放 1。
     */
    private int calculateBits(LocalDateTime start, LocalDateTime end) {
        int bits = 0;
        int startHour = start.getHour();
        int endHour = end.getHour();
        for (int i = startHour; i < endHour; i++) {
            bits |= (1 << i);
        }
        return bits;
    }
}

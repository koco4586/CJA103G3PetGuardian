package com.petguardian.booking.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.model.BookingScheduleRepository;
import com.petguardian.booking.model.BookingScheduleVO;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.login.MemberLoginRepository;
import com.petguardian.pet.model.PetRepository;
import com.petguardian.pet.model.PetVO;
import com.petguardian.petsitter.model.PetSitterServiceRepository;
import com.petguardian.petsitter.model.PetSitterServiceVO;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;

@Service
@Transactional
public class BookingServiceImpl implements BookingService, BookingExternalDataService {

    @Autowired
    private BookingOrderRepository orderRepository;

    @Autowired
    private BookingScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleService scheduleService; // 負責處理 24 小時排程字串位元運算

    @Autowired
    private MemberLoginRepository memberRepository;

    @Autowired
    private PetSitterServiceRepository serviceRepository;

    @Autowired
    private PetRepository petRepository;

    /**
     * 【功能：查詢會員訂單列表】
     */
    @Override
    public List<BookingOrderVO> getOrdersByMemberId(Integer memId) {
        // 這裡要呼叫 Repository 去資料庫撈資料
        return orderRepository.findByMemId(memId);
    }

    @Override
    public List<BookingOrderVO> getActiveOrdersByMemberId(Integer memId) {
        List<BookingOrderVO> allOrders = orderRepository.findByMemId(memId);
        // 只保留 0(待確認) 與 1(已支付)
        return allOrders.stream()
                .filter(o -> o.getOrderStatus() == 0 || o.getOrderStatus() == 1)
                .toList();
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
     * 
     * @return 儲存成功後含 ID 與計算金額的訂單物件
     * @throws IllegalArgumentException 當時間邏輯錯誤時拋出
     *                                  2 小時緩衝期、禁止跨日、禁止過期預約。
     */
    @Override
    public BookingOrderVO createBooking(BookingOrderVO order) {
        // 0. 檢查：寵物 ID 在資料庫中真的存在
        PetVO pet = petRepository.findByPrimaryKey(order.getPetId()).orElse(null);
        if (pet == null) {
            throw new IllegalArgumentException("預約失敗：找不到對應的寵物資料 (ID: " + order.getPetId() + ")");
        }

        if (!pet.getMemId().equals(order.getMemId())) {
            throw new IllegalArgumentException("預約失敗：因保姆此時段休息,還請另外再預約其他時間,謝謝");
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. 時間邏輯檢查
        if (!order.getEndTime().isAfter(order.getStartTime())) {
            throw new IllegalArgumentException("結束時間必須晚於開始時間，且至少預約 1 小時。");
        }

        // 2. 限制：最晚要在 2 小時前預約 (給保母準備時間)
        if (order.getStartTime().isBefore(now.plusHours(2))) {
            throw new IllegalArgumentException("請至少於服務開始 2 小時前預約。");
        }

        // 3. 限制：不能預約超過 60 天後的時段 (避免過期過久的單)
        if (order.getStartTime().isAfter(now.plusDays(60))) {
            throw new IllegalArgumentException("僅開放 60 天內的預約。");
        }

        // 4. 限制：單筆預約禁止跨日 (避免 24h 位元字串運算錯誤)
        if (!order.getStartTime().toLocalDate().isEqual(order.getEndTime().toLocalDate())) {
            throw new IllegalArgumentException("單筆預約不可跨日，若有需求請拆分為多筆訂單。");
        }

        // 5. 獲取服務定價 (從保母那邊透過Interface獲取)
        PetSitterServiceVO sitterService = getSitterInfo(order.getSitterId(), order.getServiceItemId());

        // 6. 衝突檢查：檢查該時段保母是否已經有約
        // 先計算這次預約想要佔用的位元 (例如: 2:00~4:00 會得到一個整數)
        int requestedBits = calculateBits(order.getStartTime(), order.getEndTime());

        // 取得保母當天的排程
        int currentSchedule = getSitterScheduleBits(order.getSitterId(), order.getStartTime().toLocalDate());

        // 使用位元 AND 運算：如果結果不為 0，代表時段重疊
        if ((currentSchedule & requestedBits) != 0) {
            throw new IllegalArgumentException("該時段保母已有其他預約，請選擇其他時間。");
        }

        // 7. 計算總金額：不足一小時以一小時計
        long hours = Math.max(1, Duration.between(order.getStartTime(), order.getEndTime()).toHours());
        order.setReservationFee((int) (hours * sitterService.getDefaultPrice()));

        // 8. 設定訂單初始狀態 (0: 待確認/待支付)
        order.setOrderStatus(0);

        // 9. 執行訂單存檔
        BookingOrderVO savedOrder = orderRepository.save(order);

        // 10. 同步更新保母排程字串
        // [修改邏輯] 增加緩衝時間機制：
        // 為了讓保姆在服務與服務之間有喘息空間，
        // 系統會強制將「佔用時間」往後延伸 1 小時 (Buffer Time)。
        // 範例：會員預約 10:00~11:00，系統實際佔用 10:00~12:00。
        BookingOrderVO bufferOrder = new BookingOrderVO();
        bufferOrder.setSitterId(savedOrder.getSitterId());
        bufferOrder.setBookingOrderId(savedOrder.getBookingOrderId()); // 關聯同一張單
        bufferOrder.setStartTime(savedOrder.getStartTime());
        // [關鍵] 結束時間自動 +1 小時，作為緩衝
        bufferOrder.setEndTime(savedOrder.getEndTime().plusHours(1));

        // 使用加長版的時間來更新行事曆，寫入狀態 '2' (已預約)
        updateSitterSchedule(bufferOrder, '2');

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
        // 呼叫排程更新，將時段改回 '0' (空閒)
        updateSitterSchedule(order, '0');
    }

    /**
     * 【更新保母排程】
     * 根據訂單時間區間，修改保母當日的 24 位元排程字串。
     */
    private void updateSitterSchedule(BookingOrderVO order, char targetStatus) {
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

        String newStatus = scheduleService.updateStatusString(schedule.getBookingStatus(), startH, endH, targetStatus);
        schedule.setBookingStatus(newStatus);
        // 如果是佔用，則更新關聯訂單 ID
        if (targetStatus == '1') {
            schedule.setBookingOrderId(order.getBookingOrderId());
        }

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
        // 釋出時段 (傳入 '0')
        updateSitterSchedule(order, '0');
    }

    /**
     * 從資料庫找出該保母當天所有「未取消」的訂單，並加總成一個 24 位元的排程整數。
     */
    private int getSitterScheduleBits(Integer sitterId, LocalDate date) {
        // 直接查詢排程表，若無紀錄代表整天都是空的 (0)
        return scheduleRepository.findBySitterIdAndScheduleDate(sitterId, date)
                .map(schedule -> {
                    // 將 "00011100..." 字串轉回 int 位元
                    return convertStatusStringToBits(schedule.getBookingStatus());
                })
                .orElse(0);
    }

    /**
     * 將 24 位元字串轉為整數，方便進行位元與運算 (&)
     */
    private int convertStatusStringToBits(String status) {
        int bits = 0;
        // 遍歷 24 個字元
        for (int i = 0; i < 24; i++) {
            // 只要狀態不是 0 (空閒)，可能是 1(休息) 或 2(預約)，都視為佔用
            if (status.charAt(i) != '0') {
                bits |= (1 << i);
            }
        }
        return bits;
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

        // 可在此接虛擬錢包
    }
    // 以下測試用

    @Override
    public Member getMemberInfo(Integer memId) {
        return memberRepository.findById(memId).orElse(null);
    }

    @Override
    public PetVO getPetInfo(Integer petId) {
        return petRepository.findByPrimaryKey(petId).orElse(null);
    }

    @Override
    public PetSitterServiceVO getSitterInfo(Integer sitterId, Integer serviceItemId) {
        // 使用複合主鍵查詢真實定價
        return serviceRepository.findBySitter_SitterId(sitterId).stream()
                .filter(s -> s.getServiceItemId().equals(serviceItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("該保母不提供此項服務或保母不存在"));
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

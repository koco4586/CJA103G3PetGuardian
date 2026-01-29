package com.petguardian.booking.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.petsitter.model.PetSitterServiceVO;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingOrderRepository orderRepository;

    @Autowired
    private BookingScheduleInternalService scheduleInternalService;

    @Autowired
    private BookingDataIntegrationService dataService;

    // 會員端查詢
    @Override
    public List<BookingOrderVO> getOrdersByMemberId(Integer memId) {
        List<BookingOrderVO> list = orderRepository.findByMemId(memId);
        scheduleInternalService.autoUpdateExpiredOrders(list); // 借用排程服務的修正邏輯
        list.forEach(this::enrichOrderInfo);
        return list;
    }

    // 查詢單筆
    @Override
    public BookingOrderVO getOrderById(Integer orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Override
    public List<BookingOrderVO> getActiveOrdersByMemberId(Integer memId) {
        return orderRepository.findByMemId(memId).stream()
                .filter(o -> o.getOrderStatus() == 0 || o.getOrderStatus() == 1)
                .toList();
    }

    // 過期訂單自動校正
    @Override
    public List<BookingOrderVO> findByMemberAndStatus(Integer memId, Integer status) {
        List<BookingOrderVO> allOrders = orderRepository.findByMemId(memId);
        scheduleInternalService.autoUpdateExpiredOrders(allOrders);
        return allOrders.stream()
                .filter(order -> order.getOrderStatus() != null && order.getOrderStatus().equals(status))
                .toList();
    }

    // 保母端方法
    @Override
    public List<BookingOrderVO> getOrdersBySitterId(Integer sitterId) {
        List<BookingOrderVO> list = orderRepository.findBySitterId(sitterId);
        scheduleInternalService.autoUpdateExpiredOrders(list); // 狀態修正

        // 遍歷每一筆訂單，把名字填進去
        list.forEach(this::enrichOrderInfo);
        return list;
    }

    @Override
    public List<BookingOrderVO> findBySitterAndStatus(Integer sitterId, Integer status) {
        List<BookingOrderVO> list = orderRepository.findBySitterId(sitterId).stream()
                .filter(o -> o.getOrderStatus().equals(status))
                .toList();

        list.forEach(this::enrichOrderInfo);
        return list;
    }

    // [New] 查詢某保母特定狀態的訂單 (Service層封裝)
    @Override
    public List<BookingOrderVO> findOrdersBySitterAndStatus(Integer sitterId, Integer status) {
        // 呼叫 Repository 進行查詢 (狀態: 0=待確認, 1=進行中...)
        List<BookingOrderVO> list = orderRepository.findBySitterIdAndOrderStatus(sitterId, status);
        // 補充訂單相關資訊 (如會員名稱、寵物名稱等)
        list.forEach(this::enrichOrderInfo);
        return list;
    }

    @Override
    public void updateOrderStatusBySitter(Integer orderId, Integer newStatus) {
        scheduleInternalService.updateOrderStatusBySitter(orderId, newStatus);
    }

    // 建立預約
    @Override
    public BookingOrderVO createBooking(BookingOrderVO order) {
        // 1. 外部資料驗證 (寵物/保母服務)
        dataService.validateAndGetPet(order.getPetId(), order.getMemId());
        PetSitterServiceVO sitterService = dataService.getSitterServiceInfo(order.getSitterId(),
                order.getServiceItemId());

        // 2. 時間與衝突檢查
        validateTimeLogic(order);
        int requestedBits = scheduleInternalService.calculateBits(order.getStartTime(), order.getEndTime());
        int currentSchedule = scheduleInternalService.getSitterScheduleBits(order.getSitterId(),
                order.getStartTime().toLocalDate());

        if ((currentSchedule & requestedBits) != 0) {
            throw new IllegalArgumentException("該時段保母已有其他預約。");
        }

        // 3. 金額計算與狀態設定
        long hours = Math.max(1, Duration.between(order.getStartTime(), order.getEndTime()).toHours());
        order.setReservationFee((int) (hours * sitterService.getDefaultPrice()));
        order.setOrderStatus(0);

        // 4. 存檔並更新排程
        BookingOrderVO savedOrder = orderRepository.save(order);

        // 緩衝處理
        BookingOrderVO bufferOrder = new BookingOrderVO();
        bufferOrder.setSitterId(savedOrder.getSitterId());
        bufferOrder.setBookingOrderId(savedOrder.getBookingOrderId());
        bufferOrder.setStartTime(savedOrder.getStartTime());
        bufferOrder.setEndTime(savedOrder.getEndTime().plusHours(1));

        scheduleInternalService.updateSitterSchedule(savedOrder, '2');
        scheduleInternalService.updateSitterSchedule(bufferOrder, '2');

        return savedOrder;
    }

    private void enrichOrderInfo(BookingOrderVO order) {
        // 1. 會員(飼主)名稱
        if (order.getMemId() != null) {
            var member = dataService.getMemberInfo(order.getMemId());
            order.setMemName(member != null ? member.getMemName() : "未知會員");
        }
        // 2. 寵物名稱
        if (order.getPetId() != null) {
            var pet = dataService.getPetInfo(order.getPetId());
            order.setPetName(pet != null ? pet.getPetName() : "未知寵物");
        }
        // 3. 處理取消原因的預設文字
        if (order.getOrderStatus() == 3 && (order.getCancelReason() == null || order.getCancelReason().isBlank())) {
            order.setCancelReason("保母忙碌中，暫時無法接單");
        }
    }

    // 取消與退款
    @Override
    public void cancelBooking(Integer orderId, String reason) {
        BookingOrderVO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));
        // 退款邏輯
        if (order.getOrderStatus() == 1) {
            int refundAmount = calculateRefund(order); // 內部私有計算邏輯
            if (refundAmount > 0) {
                dataService.processRefund(order.getMemId(), refundAmount);
            }
        }
        order.setOrderStatus(3);
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        orderRepository.save(order);

        scheduleInternalService.updateSitterSchedule(order, '0');
    }

    // 核准退款
    @Override
    public void approveRefund(Integer orderId, Double ratio) {
        BookingOrderVO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));
        int refundAmount = (int) (order.getReservationFee() * ratio);
        if (refundAmount > 0) {
            dataService.processRefund(order.getMemId(), refundAmount);
        }
        order.setOrderStatus(4); // 4: 已退款
        String ratioText = (ratio >= 1.0) ? "全額退款" : (int) (ratio * 100) + "% 部分退款";
        order.setCancelReason(order.getCancelReason() + " [" + ratioText + " - 管理員核准]");
        orderRepository.save(order);
        scheduleInternalService.updateSitterSchedule(order, '0');
    }

    // 後台撥款
    @Override
    public void completePayout(Integer orderId) {
        BookingOrderVO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));

        if (order.getOrderStatus() != 2) {
            throw new RuntimeException("訂單尚未完成服務。");
        }

        dataService.processPayout(order.getSitterId(), order.getReservationFee());
        order.setOrderStatus(5);
        orderRepository.save(order);
    }

    private void validateTimeLogic(BookingOrderVO order) {
        LocalDateTime now = LocalDateTime.now();
        if (!order.getEndTime().isAfter(order.getStartTime()))
            throw new IllegalArgumentException("結束時間錯誤");
        // if (order.getStartTime().isBefore(now.plusHours(2))) throw new
        // IllegalArgumentException("需兩小時前預約");
        if (!order.getStartTime().toLocalDate().isEqual(order.getEndTime().toLocalDate()))
            throw new IllegalArgumentException("不可跨日");
    }

    private int calculateRefund(BookingOrderVO order) {
        long daysUntil = Duration.between(LocalDateTime.now(), order.getStartTime()).toDays();
        double rate = (daysUntil >= 7) ? 1.0 : (daysUntil >= 1 ? 0.5 : 0.0);
        return (int) (order.getReservationFee() * rate);
    }
}
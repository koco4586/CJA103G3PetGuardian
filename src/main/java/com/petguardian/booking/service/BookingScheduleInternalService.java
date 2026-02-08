package com.petguardian.booking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.model.BookingScheduleRepository;
import com.petguardian.booking.model.BookingScheduleVO;

@Service
public class BookingScheduleInternalService {

    @Autowired
    private BookingOrderRepository orderRepository;

    @Autowired
    private BookingScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleService scheduleService;

    // === 1. 保母訂單查詢邏輯 ===

    public List<BookingOrderVO> getOrdersBySitterId(Integer sitterId) {
        List<BookingOrderVO> list = orderRepository.findBySitterId(sitterId);
        autoUpdateExpiredOrders(list); // 修正過期狀態
        return list;
    }

    public List<BookingOrderVO> findBySitterAndStatus(Integer sitterId, Integer status) {
        List<BookingOrderVO> allOrders = orderRepository.findBySitterId(sitterId);
        autoUpdateExpiredOrders(allOrders);
        return allOrders.stream()
                .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().equals(status))
                .collect(Collectors.toList());
    }

    // === 2. 狀態更新與排程連動邏輯 ===

    @Transactional
    public void updateOrderStatusBySitter(Integer orderId, Integer newStatus) {
        BookingOrderVO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));

        if (newStatus == 3) { // 拒絕預約
            order.setOrderStatus(3);
            order.setCancelReason("保母拒絕預約");
            updateSitterSchedule(order, '0'); // 釋放時段
        } else if (newStatus == 2) { // 完成服務
            order.setOrderStatus(2);
        } else {
            order.setOrderStatus(newStatus);
        }
        orderRepository.save(order);
    }

    // === 3. 核心排程運算 (由原本的 Impl 搬移過來) ===

    /**
     * 更新保母排程字串 (將對應時段設為指定狀態)
     */
    public void updateSitterSchedule(BookingOrderVO order, char targetStatus) {
        LocalDate date = order.getStartTime().toLocalDate();
        int startH = order.getStartTime().getHour();
        int endH = order.getEndTime().getHour();

        BookingScheduleVO schedule = scheduleRepository.findBySitterIdAndScheduleDate(order.getSitterId(), date)
                .orElseGet(() -> {
                    BookingScheduleVO n = new BookingScheduleVO();
                    n.setSitterId(order.getSitterId());
                    n.setScheduleDate(date);
                    n.setBookingStatus("000000000000000000000000");
                    return n;
                });

        String newStatus = scheduleService.updateStatusString(schedule.getBookingStatus(), startH, endH, targetStatus);
        schedule.setBookingStatus(newStatus);

        if (targetStatus != '0') {
            schedule.setBookingOrderId(order.getBookingOrderId());
        } else {
            schedule.setBookingOrderId(null); // 釋放訂單關聯
        }

        scheduleRepository.save(schedule);
    }

    /**
     * 獲取保母當天已佔用的位元整數
     */
    public int getSitterScheduleBits(Integer sitterId, LocalDate date) {
        return scheduleRepository.findBySitterIdAndScheduleDate(sitterId, date)
                .map(schedule -> convertStatusStringToBits(schedule.getBookingStatus()))
                .orElse(0);
    }

    /**
     * 計算特定時間範圍對應的位元
     */
    public int calculateBits(LocalDateTime start, LocalDateTime end) {
        int bits = 0;
        int startHour = start.getHour();
        int endHour = end.getHour();
        for (int i = startHour; i < endHour; i++) {
            bits |= (1 << i);
        }
        return bits;
    }

    private int convertStatusStringToBits(String status) {
        int bits = 0;
        for (int i = 0; i < 24; i++) {
            if (status.charAt(i) != '0') { // 只要不是 0 (空閒)，就視為佔用
                bits |= (1 << i);
            }
        }
        return bits;
    }

    public void autoUpdateExpiredOrders(List<BookingOrderVO> orders) {
        LocalDateTime now = LocalDateTime.now();
        boolean changed = false;
        for (BookingOrderVO order : orders) {
            if ((order.getOrderStatus() == 0 || order.getOrderStatus() == 1)
                    && order.getEndTime().isBefore(now)) {
                order.setOrderStatus(2);
                changed = true;
            }
        }
        if (changed) {
            orderRepository.saveAll(orders); // 一次性更新
        }
    }
}
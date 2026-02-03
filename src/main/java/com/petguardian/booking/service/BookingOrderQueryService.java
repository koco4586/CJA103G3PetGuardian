package com.petguardian.booking.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.member.model.Member;
import com.petguardian.pet.model.PetVO;

/**
 * 訂單查詢服務
 * 處理所有訂單的查詢操作，包括會員端和保母端的訂單查詢
 */
@Service
@Transactional(readOnly = true)
public class BookingOrderQueryService {

    @Autowired
    private BookingOrderRepository orderRepository;

    @Autowired
    private BookingScheduleInternalService scheduleInternalService;

    @Autowired
    private BookingDataIntegrationService dataService;
    
    @Autowired
    private com.petguardian.member.repository.login.MemberLoginRepository memberRepository;
    @Autowired
    private com.petguardian.pet.model.PetRepository petRepository;

    /**
     * 查詢會員的所有訂單
     */
    public List<BookingOrderVO> getOrdersByMemberId(Integer memId) {
        List<BookingOrderVO> list = orderRepository.findByMemId(memId);
        // 借用排程服務的修正邏輯，自動更新已過期但狀態未更新的訂單
        scheduleInternalService.autoUpdateExpiredOrders(list);
        // 訂單相關資訊（會員名稱、寵物名稱等）
        if (!list.isEmpty()) {
            batchEnrichOrderInfo(list);
        }
        return list;
    }

    /**
     * 根據訂單ID查詢單筆訂單
     * 訂單若不存在則返回 null
     */
    public BookingOrderVO getOrderById(Integer orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    /**
     * 查詢會員的進行中訂單（狀態為 0:待確認 或 1:進行中）
     */
    public List<BookingOrderVO> getActiveOrdersByMemberId(Integer memId) {
        return orderRepository.findByMemId(memId).stream()
                .filter(o -> o.getOrderStatus() == 0 || o.getOrderStatus() == 1)
                .toList();
    }

    /**
     * 查詢會員特定狀態的訂單
     * 訂單狀態（0:待確認, 1:進行中, 2:已完成, 3:申請退款中, 4:已退款, 5:已撥款）
     */
    public List<BookingOrderVO> findByMemberAndStatus(Integer memId, Integer status) {
        // 先取得會員的所有訂單
        List<BookingOrderVO> allOrders = orderRepository.findByMemId(memId);
        // 自動更新過期訂單的狀態
        scheduleInternalService.autoUpdateExpiredOrders(allOrders);
        // 篩選出指定狀態的訂單
        return allOrders.stream()
                .filter(order -> order.getOrderStatus() != null && order.getOrderStatus().equals(status))
                .toList();
    }

    /**
     * 查詢保母的所有訂單
     * 訂單列表（自動更新過期狀態並補充完整資訊）
     */
    public List<BookingOrderVO> getOrdersBySitterId(Integer sitterId) {
        List<BookingOrderVO> list = orderRepository.findBySitterId(sitterId);
        // 狀態修正：更新已過期的訂單
        scheduleInternalService.autoUpdateExpiredOrders(list);
        // 訂單相關資訊（會員名稱、寵物名稱等）
        if (!list.isEmpty()) {
            batchEnrichOrderInfo(list);
        }
        return list;
    }

    /**
     * 查詢保母特定狀態的訂單
     * 訂單狀態（0:待確認, 1:進行中, 2:已完成, 3:申請退款中, 4:已退款, 5:已撥款）
     */
    public List<BookingOrderVO> findOrdersBySitterAndStatus(Integer sitterId, Integer status) {
        // 直接呼叫 Repository 進行查詢
        List<BookingOrderVO> list = orderRepository.findBySitterIdAndOrderStatus(sitterId, status);
        // 訂單相關資訊（如會員名稱、寵物名稱等）
        if (!list.isEmpty()) {
            batchEnrichOrderInfo(list);
        }
        return list;
    }
    

    /**
     * 補充訂單的完整資訊
     */
//    private void enrichOrderInfo(BookingOrderVO order) {
//        // 1. 會員名稱
//        if (order.getMemId() != null) {
//            var member = dataService.getMemberInfo(order.getMemId());
//            order.setMemName(member != null ? member.getMemName() : "未知會員");
//        }
//
//        // 2. 寵物名稱
//        if (order.getPetId() != null) {
//            var pet = dataService.getPetInfo(order.getPetId());
//            order.setPetName(pet != null ? pet.getPetName() : "未知寵物");
//        }
//
//        // 3. 取消原因預設文字（當訂單被取消但未填寫原因時）
//        if (order.getOrderStatus() == 3 && (order.getCancelReason() == null || order.getCancelReason().isBlank())) {
//            order.setCancelReason("保母忙碌中，暫時無法接單");
//        }
//    }
    
    /**
     * 批次補充訂單資訊 (解決 N+1 問題)
     */
    private void batchEnrichOrderInfo(List<BookingOrderVO> orderList) {
        // 1. 蒐集所有 ID (使用 Set 避免重複)
        java.util.Set<Integer> memIds = new java.util.HashSet<>();
        java.util.Set<Integer> petIds = new java.util.HashSet<>();
        java.util.Set<Integer> serviceIds = new java.util.HashSet<>();
        
        for (BookingOrderVO order : orderList) {
            if (order.getMemId() != null) memIds.add(order.getMemId());
            if (order.getPetId() != null) petIds.add(order.getPetId());
            if (order.getServiceItemId() != null) serviceIds.add(order.getServiceItemId()); // 蒐集服務 ID
        }
        
        // 2. 建立 ID 對照表 (Map)
        java.util.Map<Integer, String> serviceNameMap = dataService.getServiceNamesMap(serviceIds);
        java.util.Map<Integer, String> memNameMap = new java.util.HashMap<>();
        java.util.Map<Integer, String> petNameMap = new java.util.HashMap<>();
        
        // 3. 批次查詢會員資料 (SQL: WHERE mem_id IN (...))
        if (!memIds.isEmpty()) {
        	List<Member> members = memberRepository.findAllById(memIds);
        	for (Member m : members) memNameMap.put(m.getMemId(), m.getMemName());
        }
        
        
        // 4. 批次查詢寵物資料 (SQL: WHERE pet_id IN (...))
        if (!petIds.isEmpty()) {
        	List<PetVO> pets = petRepository.findAllById(petIds);
        	for (PetVO p : pets) petNameMap.put(p.getPetId(), p.getPetName());
        }
        
        // 5. 將查到的名字填回訂單物件
        for (BookingOrderVO order : orderList) {
        	order.setServiceName(serviceNameMap.getOrDefault(order.getServiceItemId(), "一般服務"));
            // 填入會員名稱
            if (order.getMemId() != null) {
            	order.setMemName(memNameMap.getOrDefault(order.getMemId(), "未知會員"));
            }
            
            // 填入寵物名稱
            if (order.getPetId() != null) {
            	order.setPetName(petNameMap.getOrDefault(order.getPetId(), "未知寵物"));
            }
            // 處理取消原因的預設值
            if (order.getOrderStatus() != null && order.getOrderStatus() == 3 && 
                (order.getCancelReason() == null || order.getCancelReason().isBlank())) {
                order.setCancelReason("保母忙碌中，暫時無法接單");
            }
        }
    }
}
        

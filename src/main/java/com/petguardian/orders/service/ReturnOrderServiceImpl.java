package com.petguardian.orders.service;


import com.petguardian.orders.model.OrdersVO;
import com.petguardian.orders.model.OrdersRepository;
import com.petguardian.orders.model.ReturnOrderRepository;
import com.petguardian.orders.model.ReturnOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ReturnOrderServiceImpl implements ReturnOrderService {

    @Autowired
    private ReturnOrderRepository returnOrderDAO;

    @Autowired
    private OrdersRepository ordersDAO;

    @Autowired
    private OrdersService ordersService;

    // 退貨狀態常數
    public static final Integer RETURN_STATUS_PENDING = 0; // 審核中
    public static final Integer RETURN_STATUS_APPROVED = 1; // 退貨通過
    public static final Integer RETURN_STATUS_REJECTED = 2; // 退貨失敗

    // 訂單狀態常數
    private static final Integer ORDER_STATUS_SHIPPED = 1; // 已出貨
    private static final Integer ORDER_STATUS_COMPLETED = 2; // 已完成
    private static final Integer ORDER_STATUS_REFUNDING = 4; // 申請退貨中
    private static final Integer ORDER_STATUS_REFUNDED = 5; // 退貨完成

    @Override
    public Map<String, Object> applyReturn(Integer orderId, String returnReason) {
        if (orderId == null) {
            throw new IllegalArgumentException("訂單ID不能為 null");
        }
        if (returnReason == null || returnReason.trim().isEmpty()) {
            throw new IllegalArgumentException("退貨原因不能為空");
        }

        // 查詢訂單
        OrdersVO order = ordersDAO.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在: " + orderId));

        // 檢查訂單狀態（已出貨或已完成的訂單可申請退貨）
        Integer status = order.getOrderStatus();
        if (!status.equals(ORDER_STATUS_SHIPPED) && !status.equals(ORDER_STATUS_COMPLETED)) {
            throw new IllegalArgumentException("只有已出貨或已完成的訂單才能申請退貨");
        }

        // 檢查是否已有退貨申請
        if (returnOrderDAO.existsByOrderId(orderId)) {
            throw new IllegalArgumentException("此訂單已有退貨申請");
        }

        // 建立退貨單
        ReturnOrderVO returnOrder = new ReturnOrderVO();
        returnOrder.setOrderId(orderId);
        returnOrder.setReturnReason(returnReason);

        // 退款金額 (若金額小於運費則為0)
        int refundAmount = Math.max(0, order.getOrderTotal());
        returnOrder.setRefundAmount(refundAmount);

        returnOrder.setReturnStatus(RETURN_STATUS_PENDING);

        ReturnOrderVO savedReturn = returnOrderDAO.save(returnOrder);

        // 更新訂單狀態
        order.setOrderStatus(ORDER_STATUS_REFUNDING);
        OrdersVO updatedOrder = ordersDAO.save(order);

        Map<String, Object> result = new HashMap<>();
        result.put("returnOrder", savedReturn);
        result.put("order", updatedOrder);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReturnOrderVO> getReturnOrderById(Integer returnId) {
        if (returnId == null) {
            throw new IllegalArgumentException("退貨單ID不能為 null");
        }
        return returnOrderDAO.findById(returnId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReturnOrderVO> getReturnOrderByOrderId(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("訂單ID不能為 null");
        }
        return returnOrderDAO.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnOrderVO> getAllReturnOrders() {
        return returnOrderDAO.findAllByOrderByApplyTimeDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnOrderVO> getReturnOrdersByBuyerId(Integer buyerMemId) {
        if (buyerMemId == null) {
            throw new IllegalArgumentException("買家會員ID不能為 null");
        }
        return returnOrderDAO.findByBuyerMemId(buyerMemId);
    }

    @Override
    public ReturnOrderVO updateReturnStatus(Integer returnId, Integer newStatus) {
        if (returnId == null) {
            throw new IllegalArgumentException("退貨單ID不能為 null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("新狀態不能為 null");
        }

        // 查詢退貨單
        ReturnOrderVO returnOrder = returnOrderDAO.findById(returnId)
                .orElseThrow(() -> new IllegalArgumentException("退貨單不存在: " + returnId));

        // 更新退貨狀態
        returnOrder.setReturnStatus(newStatus);
        ReturnOrderVO updatedReturn = returnOrderDAO.save(returnOrder);

        // 根據退貨狀態更新訂單狀態
        OrdersVO order = ordersDAO.findById(returnOrder.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));

        if (newStatus.equals(RETURN_STATUS_APPROVED)) {
            order.setOrderStatus(ORDER_STATUS_REFUNDED); // 退貨完成
            ordersDAO.save(order);
            // 退貨通過，退款到買家錢包
            ordersService.refundToBuyerWallet(returnOrder.getOrderId());
        } else if (newStatus.equals(RETURN_STATUS_REJECTED)) {
            order.setOrderStatus(ORDER_STATUS_COMPLETED); // 恢復已完成
            ordersDAO.save(order);
        }

        return updatedReturn;
    }
}
package com.petguardian.seller.service;

import com.petguardian.orders.model.*;
import com.petguardian.orders.service.OrdersService;
import com.petguardian.wallet.model.Wallet;
import com.petguardian.wallet.model.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminStoreServiceImpl implements AdminStoreService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ReturnOrderRepository returnOrderRepository;

    @Autowired
    private ReturnOrderPicRepository returnOrderPicRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private StoreMemberRepository memberRepository;

    private static final Integer STATUS_PAID = 0;
    private static final Integer STATUS_SHIPPED = 1;
    private static final Integer STATUS_COMPLETED = 2;
    private static final Integer STATUS_CANCELED = 3;
    private static final Integer STATUS_REFUNDING = 4;
    private static final Integer STATUS_REFUNDED = 5;
    private static final Integer STATUS_PAIDOUT = 6;

    private static final Integer RETURN_STATUS_PENDING = 0;
    private static final Integer RETURN_STATUS_APPROVED = 1;
    private static final Integer RETURN_STATUS_REJECTED = 2;

    // ... getPendingOrders, getClosedOrders, getReturnOrders 保持原樣 (省略以節省空間，請使用上方你提供的版本，這邊只顯示關鍵修改方法) ...
    // 請保留原有的 getPendingOrders, getClosedOrders, getReturnOrders, getReturnOrdersWithDetails 方法

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPendingOrders() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<OrdersVO> paidOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_PAID);
        List<OrdersVO> shippedOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_SHIPPED);
        List<OrdersVO> allPendingOrders = new ArrayList<>();
        allPendingOrders.addAll(paidOrders);
        allPendingOrders.addAll(shippedOrders);
        for (OrdersVO order : allPendingOrders) {
            result.add(buildOrderData(order));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getClosedOrders() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<OrdersVO> completedOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_COMPLETED);
        List<OrdersVO> canceledOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_CANCELED);
        List<OrdersVO> paidOutOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_PAIDOUT);
        List<OrdersVO> allClosedOrders = new ArrayList<>();
        allClosedOrders.addAll(completedOrders);
        allClosedOrders.addAll(canceledOrders);
        allClosedOrders.addAll(paidOutOrders);
        for (OrdersVO order : allClosedOrders) {
            Map<String, Object> orderData = buildOrderData(order);
            orderData.put("canPayout", order.getOrderStatus().equals(STATUS_COMPLETED));
            orderData.put("isCanceled", order.getOrderStatus().equals(STATUS_CANCELED));
            orderData.put("isPaidOut", order.getOrderStatus().equals(STATUS_PAIDOUT));
            result.add(orderData);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getReturnOrders() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<OrdersVO> refundingOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_REFUNDING);
        List<OrdersVO> refundedOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_REFUNDED);
        List<OrdersVO> allReturnOrders = new ArrayList<>();
        allReturnOrders.addAll(refundingOrders);
        allReturnOrders.addAll(refundedOrders);
        for (OrdersVO order : allReturnOrders) {
            result.add(buildOrderData(order));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getReturnOrdersWithDetails() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<ReturnOrderVO> allReturnApplications = returnOrderRepository.findAllByOrderByApplyTimeDesc();
        for (ReturnOrderVO returnOrder : allReturnApplications) {
            Map<String, Object> returnData = new HashMap<>();
            returnData.put("returnOrder", returnOrder);
            ordersRepository.findById(returnOrder.getOrderId())
                    .ifPresent(order -> {
                        returnData.put("order", order);
                        memberRepository.findById(order.getBuyerMemId())
                                .ifPresent(buyer -> returnData.put("buyerName", buyer.getMemName()));
                        memberRepository.findById(order.getSellerMemId())
                                .ifPresent(seller -> returnData.put("sellerName", seller.getMemName()));
                    });
            returnData.put("isPending", returnOrder.getReturnStatus().equals(RETURN_STATUS_PENDING));
            returnData.put("isApproved", returnOrder.getReturnStatus().equals(RETURN_STATUS_APPROVED));
            returnData.put("isRejected", returnOrder.getReturnStatus().equals(RETURN_STATUS_REJECTED));
            result.add(returnData);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getReturnOrderDetail(Integer returnId) {
        // 【修改重點】：讀取退貨詳情（含買家、賣家名稱及退貨圖片 URL）
        Map<String, Object> result = new HashMap<>();

        try {
            ReturnOrderVO returnOrder = returnOrderRepository.findById(returnId)
                    .orElseThrow(() -> new RuntimeException("退貨申請不存在"));

            result.put("success", true);
            result.put("returnId", returnOrder.getReturnId());
            result.put("orderId", returnOrder.getOrderId());
            result.put("returnReason", returnOrder.getReturnReason());
            result.put("refundAmount", returnOrder.getRefundAmount());
            result.put("applyTime", returnOrder.getApplyTime());
            result.put("returnStatus", returnOrder.getReturnStatus());

            ordersRepository.findById(returnOrder.getOrderId())
                    .ifPresent(order -> {
                        result.put("orderTotal", order.getOrderTotal());
                        memberRepository.findById(order.getBuyerMemId())
                                .ifPresent(buyer -> result.put("buyerName", buyer.getMemName()));
                        memberRepository.findById(order.getSellerMemId())
                                .ifPresent(seller -> result.put("sellerName", seller.getMemName()));
                    });

            // 修正部分：直接讀取 picUrl，不再做 Base64 編碼
            List<ReturnOrderPicVO> pics = returnOrderPicRepository.findByReturnOrder_ReturnId(returnId);
            List<String> imageUrlList = new ArrayList<>();

            for (ReturnOrderPicVO pic : pics) {
                // 檢查 picUrl 是否有值
                if (pic.getPicUrl() != null && !pic.getPicUrl().isEmpty()) {
                    imageUrlList.add(pic.getPicUrl());
                }
            }

            result.put("returnImages", imageUrlList); // 前端 market.js 會讀取這個欄位
            result.put("hasImages", !imageUrlList.isEmpty());

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // 批准退貨申請，更新訂單狀態並撥款給買家
    @Override
    public void approveReturn(Integer returnId) {
        ReturnOrderVO returnOrder = returnOrderRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("退貨申請不存在"));

        if (!returnOrder.getReturnStatus().equals(RETURN_STATUS_PENDING)) {
            throw new RuntimeException("此退貨申請已處理過");
        }

        returnOrder.setReturnStatus(RETURN_STATUS_APPROVED);
        returnOrderRepository.save(returnOrder);

        OrdersVO order = ordersRepository.findById(returnOrder.getOrderId())
                .orElseThrow(() -> new RuntimeException("訂單不存在"));
        order.setOrderStatus(STATUS_REFUNDED);
        ordersRepository.save(order);

        Integer refundAmount = returnOrder.getRefundAmount();
        Wallet buyerWallet = walletRepository.findByMemId(order.getBuyerMemId())
                .orElseThrow(() -> new RuntimeException("買家錢包不存在"));

        buyerWallet.setBalance(buyerWallet.getBalance() + refundAmount);
        walletRepository.save(buyerWallet);
    }

    // 拒絕退貨申請，更新訂單狀態並撥款給賣家
    @Override
    public void rejectReturn(Integer returnId) {
        ReturnOrderVO returnOrder = returnOrderRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("退貨申請不存在"));

        if (!returnOrder.getReturnStatus().equals(RETURN_STATUS_PENDING)) {
            throw new RuntimeException("此退貨申請已處理過");
        }

        returnOrder.setReturnStatus(RETURN_STATUS_REJECTED);
        returnOrderRepository.save(returnOrder);

        OrdersVO order = ordersRepository.findById(returnOrder.getOrderId())
                .orElseThrow(() -> new RuntimeException("訂單不存在"));
        order.setOrderStatus(STATUS_COMPLETED);
        ordersRepository.save(order);

        Integer payoutAmount = returnOrder.getRefundAmount();
        Wallet sellerWallet = walletRepository.findByMemId(order.getSellerMemId())
                .orElseThrow(() -> new RuntimeException("賣家錢包不存在"));

        sellerWallet.setBalance(sellerWallet.getBalance() + payoutAmount);
        walletRepository.save(sellerWallet);
    }
    // 撥款給賣家，更新訂單狀態
    @Override
    public void payoutToSeller(Integer orderId) {
        OrdersVO order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));

        if (!order.getOrderStatus().equals(STATUS_COMPLETED)) {
            throw new RuntimeException("只有已完成的訂單才能撥款");
        }

        Integer payoutAmount = order.getOrderTotal();
        Wallet sellerWallet = walletRepository.findByMemId(order.getSellerMemId())
                .orElseThrow(() -> new RuntimeException("賣家錢包不存在"));

        sellerWallet.setBalance(sellerWallet.getBalance() + payoutAmount);
        walletRepository.save(sellerWallet);

        order.setOrderStatus(STATUS_PAIDOUT);
        ordersRepository.save(order);
    }

    // 取得訂單詳情（包含買家、賣家名稱及訂單項目），給管理員使用
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getOrderDetailForAdmin(Integer orderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 查詢訂單基本資料
            OrdersVO order = ordersRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("訂單不存在"));

            result.put("success", true);
            result.put("orderId", order.getOrderId());
            result.put("orderStatus", order.getOrderStatus());
            result.put("orderTotal", order.getOrderTotal());

            // 格式化下單時間
            String orderTimeStr = "-";
            if (order.getOrderTime() != null) {
                orderTimeStr = order.getOrderTime().toString().replace("T", " ");
                if (orderTimeStr.length() > 16) {
                    orderTimeStr = orderTimeStr.substring(0, 16);
                }
            }
            result.put("orderTime", orderTimeStr);

            // 收件資訊
            result.put("receiverName", order.getReceiverName());
            result.put("receiverPhone", order.getReceiverPhone());
            result.put("receiverAddress", order.getReceiverAddress());
            result.put("specialInstructions", order.getSpecialInstructions());

            // 買家名稱
            memberRepository.findById(order.getBuyerMemId())
                    .ifPresent(buyer -> result.put("buyerName", buyer.getMemName()));

            // 賣家名稱
            memberRepository.findById(order.getSellerMemId())
                    .ifPresent(seller -> result.put("sellerName", seller.getMemName()));

            // 利用已注入的 ordersService 取得商品明細（不需額外注入其他 Repository）
            try {
                Map<String, Object> orderWithItems = ordersService.getOrderWithItems(orderId);
                List<Map<String, Object>> orderItems =
                        (List<Map<String, Object>>) orderWithItems.get("orderItems");

                // 整理商品資料，統一欄位名稱給前端使用
                List<Map<String, Object>> itemList = new ArrayList<>();
                if (orderItems != null) {
                    for (Map<String, Object> item : orderItems) {
                        Map<String, Object> itemData = new HashMap<>();
                        itemData.put("proId", item.get("proId"));
                        itemData.put("proPrice", item.get("proPrice"));
                        itemData.put("quantity", item.get("quantity"));
                        itemData.put("subtotal", item.get("subtotal"));
                        // ordersService 回傳的欄位名稱是 productTitle 和 productImg
                        itemData.put("productName", item.get("productTitle"));
                        itemData.put("productImage", item.get("productImg"));
                        itemList.add(itemData);
                    }
                }
                result.put("items", itemList);
            } catch (Exception e) {
                // 若取得商品明細失敗，回傳空陣列
                result.put("items", new ArrayList<>());
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    // 建立訂單資料的輔助方法，包含買家、賣家名稱及訂單項目
    private Map<String, Object> buildOrderData(OrdersVO order) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("order", order);
        memberRepository.findById(order.getBuyerMemId())
                .ifPresent(buyer -> orderData.put("buyerName", buyer.getMemName()));
        memberRepository.findById(order.getSellerMemId())
                .ifPresent(seller -> orderData.put("sellerName", seller.getMemName()));
        try {
            Map<String, Object> orderWithItems = ordersService.getOrderWithItems(order.getOrderId());
            orderData.put("items", orderWithItems.get("orderItems"));
        } catch (Exception e) {
            orderData.put("items", new ArrayList<>());
        }
        return orderData;
    }
}
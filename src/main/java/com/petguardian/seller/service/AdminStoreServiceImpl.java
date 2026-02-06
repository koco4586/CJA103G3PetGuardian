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

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPendingOrders() {
        //取得所有待處理訂單（已付款、已出貨）
        List<Map<String, Object>> result = new ArrayList<>();

        List<OrdersVO> paidOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_PAID);
        List<OrdersVO> shippedOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_SHIPPED);

        //合併所有待處理訂單
        List<OrdersVO> allPendingOrders = new ArrayList<>();
        allPendingOrders.addAll(paidOrders);
        allPendingOrders.addAll(shippedOrders);
        //建立訂單資料
        for (OrdersVO order : allPendingOrders) {
            result.add(buildOrderData(order));
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getClosedOrders() {
        //取得所有已結案訂單（已完成、已取消、已撥款）
        List<Map<String, Object>> result = new ArrayList<>();

        List<OrdersVO> completedOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_COMPLETED);
        List<OrdersVO> canceledOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_CANCELED);
        List<OrdersVO> paidOutOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_PAIDOUT);

        List<OrdersVO> allClosedOrders = new ArrayList<>();
        allClosedOrders.addAll(completedOrders);
        allClosedOrders.addAll(canceledOrders);
        allClosedOrders.addAll(paidOutOrders);

        //建立訂單資料
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
        //取得所有退貨訂單（退貨中、已退貨）
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
        //取得所有退貨申請（含買家、賣家名稱及退貨狀態標記）
        List<Map<String, Object>> result = new ArrayList<>();
        List<ReturnOrderVO> allReturnApplications = returnOrderRepository.findAllByOrderByApplyTimeDesc();

        //建立退貨申請資料
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
        //取得特定退貨申請詳情（含買家、賣家名稱及退貨圖片）
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

            List<ReturnOrderPicVO> pics = returnOrderPicRepository.findByReturnOrder_ReturnId(returnId);
            List<String> imageUrlList = new ArrayList<>();
            for (ReturnOrderPicVO pic : pics) {
                if (pic.getPicUrl() != null && !pic.getPicUrl().trim().isEmpty()) {
                    imageUrlList.add(pic.getPicUrl());
                }
            }
            result.put("returnImages", imageUrlList);
            result.put("hasImages", !imageUrlList.isEmpty());


        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    @Override
    public void approveReturn(Integer returnId) {
        //批准退貨申請，更新訂單狀態並退款至買家錢包
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

    @Override
    public void rejectReturn(Integer returnId) {
        //駁回退貨申請，更新訂單狀態並撥款至賣家錢包
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

    @Override
    public void payoutToSeller(Integer orderId) {
        //撥款至賣家錢包，更新訂單狀態為已撥款
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

    private Map<String, Object> buildOrderData(OrdersVO order) {
        //建立訂單資料（含買家、賣家名稱及訂單項目）
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
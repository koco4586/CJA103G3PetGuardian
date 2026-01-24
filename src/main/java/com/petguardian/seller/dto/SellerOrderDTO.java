package com.petguardian.seller.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 賣家訂單 DTO
 * 用於賣家管理中心的訂單顯示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerOrderDTO {

    private Integer orderId;            // 訂單ID
    private Integer buyerMemId;         // 買家會員ID
    private String buyerName;           // 買家名稱
    private Integer sellerMemId;        // 賣家會員ID
    private LocalDateTime orderTime;    // 下單時間
    private Integer orderTotal;         // 訂單總金額
    private Integer paymentMethod;      // 付款方式 (0=錢包)
    private Integer orderStatus;        // 訂單狀態
    private String orderStatusText;     // 訂單狀態文字

    // 收件人資訊
    private String receiverName;        // 收件人姓名
    private String receiverPhone;       // 收件人電話
    private String receiverAddress;     // 收件人地址
    private String specialInstructions; // 特殊說明

    // 訂單項目
    private List<OrderItemDTO> orderItems;

    // 狀態判斷用
    private boolean canShip;            // 是否可出貨 (只有已付款狀態可出貨)
    private boolean canCancel;          // 是否可取消
    private boolean canChat;            // 是否可聊天 (一律可)

    /**
     * 訂單狀態常數
     */
    public static final Integer STATUS_PAID = 0;        // 已付款
    public static final Integer STATUS_SHIPPED = 1;     // 已出貨
    public static final Integer STATUS_COMPLETED = 2;   // 已完成
    public static final Integer STATUS_CANCELED = 3;    // 已取消
    public static final Integer STATUS_REFUNDING = 4;   // 申請退貨中
    public static final Integer STATUS_REFUNDED = 5;    // 退貨完成

    /**
     * 取得訂單狀態文字
     */
    public static String getStatusText(Integer status) {
        if (status == null) return "未知狀態";
        switch (status) {
            case 0: return "已付款";
            case 1: return "已出貨";
            case 2: return "已完成";
            case 3: return "已取消";
            case 4: return "申請退貨中";
            case 5: return "退貨完成";
            default: return "未知狀態";
        }
    }
}
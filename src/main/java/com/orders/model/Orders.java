package com.orders.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Setter @Getter
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "buyer_mem_id", nullable = false)
    private Integer buyerMemId;

    @Column(name = "seller_mem_id", nullable = false)
    private Integer sellerMemId;

    @Column(name = "order_time", nullable = false, insertable = false, updatable = false)
    private LocalDateTime orderTime;

    @Column(name = "order_total", nullable = false)
    private Integer orderTotal;

    @Column(name = "payment_method", nullable = false)
    private Integer paymentMethod; // 0:信用卡 1:行動支付

    @Column(name = "order_status", nullable = false)
    private Integer orderStatus = 0; // 0:已付款 1:已出貨 2:已完成 3:已取消 4:申請退貨中 5:退貨完成

    @Column(name = "receiver_name", length = 20, nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20, nullable = false)
    private String receiverPhone;

    @Column(name = "receiver_address", length = 100, nullable = false)
    private String receiverAddress;

    @Column(name = "special_instructions", length = 200)
    private String specialInstructions;

    // 可選：建立雙向關聯（方便查詢）
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
}
package com.petguardian.orders.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_item")
@Setter @Getter
public class OrderItemVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Integer orderItemId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "pro_id", nullable = false)
    private Integer proId;

    @Column(name = "quantity", nullable = false, columnDefinition = "INT UNSIGNED DEFAULT 1")
    private Integer quantity = 1; // 二手商品預設為 1

    @Column(name = "pro_price", nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer proPrice;

    // 關聯 Orders 物件
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private OrdersVO order;

//    計算小計
    public Integer getSubtotal() {
        return proPrice * quantity;
    }
}
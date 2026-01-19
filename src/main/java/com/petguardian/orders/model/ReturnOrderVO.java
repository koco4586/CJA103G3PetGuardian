package com.petguardian.orders.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "return_order")
@Setter @Getter
public class ReturnOrderVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id")
    private Integer returnId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "apply_time", nullable = false, insertable = false, updatable = false)
    private LocalDateTime applyTime;

    @Column(name = "return_reason", length = 500, nullable = false)
    private String returnReason;

    @Column(name = "refund_amount", nullable = false, columnDefinition = "INT UNSIGNED")
    private Integer refundAmount;

    @Column(name = "return_status", nullable = false)
    private Integer returnStatus = 0; // 0:審核中 1:退貨通過 2:退貨失敗

}

package com.product.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter @Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pro_id")
    private Integer proId;

    @Column(name = "pro_type_id", nullable = false)
    private Integer proTypeId;

    @Column(name = "mem_id", nullable = false)
    private Integer memId;

    @Column(name = "pro_name", nullable = false, length = 20)
    private String proName;

    @Column(name = "pro_price", nullable = false)
    private Integer proPrice;

    @Column(name = "pro_description", length = 1000)
    private String proDescription;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "pro_state", nullable = false)
    private Integer proState; // 0: 待售, 1: 已售出, 2: 下架

    @Column(name = "sold_time")
    private LocalDateTime soldTime;

    @Column(name = "launched_time", insertable = false, updatable = false)
    private LocalDateTime launchedTime;
}
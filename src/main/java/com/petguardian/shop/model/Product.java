package com.petguardian.shop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pro_id")
    private Integer proId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pro_type_id", nullable = false)
    private ProType proType;

    @Column(name = "mem_id", nullable = false)
    private Integer memId; // 賣家會員ID

    @Column(name = "pro_name", nullable = false, length = 20)
    private String proName;

    @Column(name = "pro_price", nullable = false)
    private Integer proPrice;

    @Column(name = "pro_description", length = 1000)
    private String proDescription;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "pro_state", nullable = false)
    private Integer proState; // 0=已售完, 1=上架中

    @Column(name = "sold_time")
    private LocalDateTime soldTime;

    @Column(name = "launched_time", nullable = false)
    private LocalDateTime launchedTime;

    @PrePersist
    protected void onCreate() {
        launchedTime = LocalDateTime.now();
    }
}
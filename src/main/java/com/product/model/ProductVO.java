package com.product.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@Setter
public class ProductVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pro_id")
    private Integer proId;

    @Column(name = "pro_type_id")
    private Integer proTypeId;

    @Column(name = "mem_id")
    private Integer memId;

    @Column(name = "pro_name")
    private String proName;

    @Column(name = "pro_price")
    private Integer proPrice;

    @Column(name = "pro_description")
    private String proDescription;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "pro_state")
    private Integer proState; // 0:待售, 1:已售出, 2:下架

    @Column(name = "launched_time", insertable = false, updatable = false)
    private LocalDateTime launchedTime;
}
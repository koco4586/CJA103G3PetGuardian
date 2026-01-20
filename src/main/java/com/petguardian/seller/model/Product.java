package com.petguardian.seller.model;

import com.petguardian.orders.model.StoreMemberVO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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

    // 建立與會員的關聯
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false, insertable = false, updatable = false)
    private StoreMemberVO seller;

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

    // 一對多關聯：一個商品有多張圖片
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProductPic> productPics;

    @PrePersist
    protected void onCreate() {
        launchedTime = LocalDateTime.now();
    }

    // 取得賣家名稱的方法
    public String getSellerName() {
        return seller != null ? seller.getMemName() : "未知賣家";
    }
}
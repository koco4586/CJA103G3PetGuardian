package com.petguardian.orders.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "return_order_pic")
@Getter
@Setter
public class ReturnOrderPicVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pic_id")
    private Integer picId;

    /**
     * 多張圖片對應一張退貨訂單
     * FetchType.LAZY: 延遲載入，只有在使用到此物件時才查詢資料庫，節省效能
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private ReturnOrderVO returnOrder;

    @Column(name = "pic_url", length = 500)
    private String picUrl;
}

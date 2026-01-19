package com.petguardian.sellerreview.model;

import com.petguardian.orders.model.OrdersVO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_review")
@Setter @Getter
public class SellerReviewVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "rating", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer rating; // 1-5星

    @Column(name = "review_content", length = 1000)
    private String reviewContent;

    @Column(name = "review_time", nullable = false, insertable = false, updatable = false)
    private LocalDateTime reviewTime;

    @Column(name = "show_status", nullable = false)
    private Integer showStatus = 0; // 0:顯示 1:不顯示

    public void setOrder(OrdersVO order) {
    }
}

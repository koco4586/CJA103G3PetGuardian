package com.petguardian.booking.model;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "booking_order")
@Data
public class BookingOrderVO {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_order_id")
    private Integer bookingOrderId;

    @Column(name = "sitter_id", nullable = false)
    private Integer sitterId;

    @Column(name = "mem_id", nullable = false)
    private Integer memId;

    @Column(name = "pet_id", nullable = false)
    private Integer petId;

    @Column(name = "service_item_id", nullable = false)
    private Integer serviceItemId;

    @Column(name = "reservation_fee")
    private Integer reservationFee;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // 訂單狀態(0:待確認, 1:進行中, 2:已完成, 3:已取消)
    @Column(name = "order_status")
    private Integer orderStatus = 0;

    // 評價相關
    private Integer sitterRating;
    private String sitterReview;
    private Integer ownerRating;
    private String ownerReview;

    // 檢舉與取消
    @Column(name = "report_status")
    private Integer reportStatus = 0;
    
    private String cancelReason;
    private LocalDateTime cancelTime;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    @jakarta.persistence.Transient // 告訴 JPA 不要把這個存進資料庫
    private String sitterName;
    
}

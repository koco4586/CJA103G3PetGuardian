package com.petguardian.booking.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "booking_schedule")
@Data
public class BookingScheduleVO {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;
	
	@Column(name = "sitter_id", nullable = false)
    private Integer sitterId;

    @Column(name = "booking_order_id")
    private Integer bookingOrderId;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    // 24個1小時狀態字串
    @Column(name = "booking_status", nullable = false, length = 24)
    private String bookingStatus = "000000000000000000000000";
}

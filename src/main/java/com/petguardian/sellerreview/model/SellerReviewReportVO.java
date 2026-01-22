package com.petguardian.sellerreview.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_review_report")
@Setter @Getter
public class SellerReviewReportVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_rpt_id")
    private Integer reviewRptId;

    @Column(name = "review_id", nullable = false)
    private Integer reviewId;

    @Column(name = "reporter_mem_id", nullable = false)
    private Integer reporterMemId;

    @Column(name = "report_reason", length = 1000, nullable = false)
    private String reportReason;

    @Column(name = "report_status", nullable = false)
    private Integer reportStatus = 0; // 0:待審核 1:成立 2:不成立

    @Column(name = "report_time", nullable = false, insertable = false, updatable = false)
    private LocalDateTime reportTime;
}

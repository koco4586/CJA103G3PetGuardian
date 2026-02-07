package com.petguardian.complaint.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "booking_order_report")
public class ComplaintVO implements java.io.Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_report_id")
    private Integer bookingReportId;

    @Column(name = "booking_order_id")
    private Integer bookingOrderId;

    @Column(name = "evaluate_id")
    private Integer evaluateId; // 被檢舉的評價ID

    @Column(name = "report_mem_id")
    private Integer reportMemId;

    @Column(name = "to_reported_mem_id")
    private Integer toReportedMemId;

    @Column(name = "report_reason")
    private String reportReason;

    @Column(name = "report_status")
    private Integer reportStatus = 0;

    // --- 擴充欄位 (供後台管理顯示用) ---
    @jakarta.persistence.Transient
    private String reporterName; // 檢舉人姓名

    @jakarta.persistence.Transient
    private String accusedName; // 被檢舉人姓名

    @jakarta.persistence.Transient
    private String reportedContent; // 被檢舉的評價內容

    @jakarta.persistence.Transient
    private Long evaluationComplaintCount = 0L; // 該評價被檢舉的總次數 (全域統計)

    @jakarta.persistence.Transient
    private Integer reportSequence = 1; // 該評價在本筆紀錄中是第幾次被檢舉 (1=首報, 2=二報...)

    // --- 以下為 Getter & Setter ---
    public Integer getBookingReportId() {
        return bookingReportId;
    }

    public void setBookingReportId(Integer bookingReportId) {
        this.bookingReportId = bookingReportId;
    }

    public Integer getBookingOrderId() {
        return bookingOrderId;
    }

    public void setBookingOrderId(Integer bookingOrderId) {
        this.bookingOrderId = bookingOrderId;
    }

    public Integer getEvaluateId() {
        return evaluateId;
    }

    public void setEvaluateId(Integer evaluateId) {
        this.evaluateId = evaluateId;
    }

    public Integer getReportMemId() {
        return reportMemId;
    }

    public void setReportMemId(Integer reportMemId) {
        this.reportMemId = reportMemId;
    }

    public Integer getToReportedMemId() {
        return toReportedMemId;
    }

    public void setToReportedMemId(Integer toReportedMemId) {
        this.toReportedMemId = toReportedMemId;
    }

    public String getReportReason() {
        return reportReason;
    }

    public void setReportReason(String reportReason) {
        this.reportReason = reportReason;
    }

    public Integer getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(Integer reportStatus) {
        this.reportStatus = reportStatus;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getAccusedName() {
        return accusedName;
    }

    public void setAccusedName(String accusedName) {
        this.accusedName = accusedName;
    }

    public String getReportedContent() {
        return reportedContent;
    }

    public void setReportedContent(String reportedContent) {
        this.reportedContent = reportedContent;
    }

    public Long getEvaluationComplaintCount() {
        return evaluationComplaintCount;
    }

    public void setEvaluationComplaintCount(Long evaluationComplaintCount) {
        this.evaluationComplaintCount = evaluationComplaintCount;
    }

    public Integer getReportSequence() {
        return reportSequence;
    }

    public void setReportSequence(Integer reportSequence) {
        this.reportSequence = reportSequence;
    }

}
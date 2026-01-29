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
    
    @Column(name = "report_mem_id")
    private Integer reportMemId;
    
    @Column(name = "to_reported_mem_id")
    private Integer toReportedMemId;
    
    @Column(name = "report_reason")
    private String reportReason;

   

    @Column(name = "report_status")
    private Integer reportStatus = 0;

    // --- 以下為 Getter & Setter ---
    public Integer getBookingReportId() { return bookingReportId; }
    public void setBookingReportId(Integer bookingReportId) { this.bookingReportId = bookingReportId; }
    public Integer getBookingOrderId() { return bookingOrderId; }
    public void setBookingOrderId(Integer bookingOrderId) { this.bookingOrderId = bookingOrderId; }
    public Integer getReportMemId() { return reportMemId; }
    public void setReportMemId(Integer reportMemId) { this.reportMemId = reportMemId; }
    public Integer getToReportedMemId() { return toReportedMemId; }
    public void setToReportedMemId(Integer toReportedMemId) { this.toReportedMemId = toReportedMemId; }
    public String getReportReason() { return reportReason; }
    public void setReportReason(String reportReason) { this.reportReason = reportReason; }
    
    public Integer getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(Integer reportStatus) {
        this.reportStatus = reportStatus;
    }
    
    
    
}
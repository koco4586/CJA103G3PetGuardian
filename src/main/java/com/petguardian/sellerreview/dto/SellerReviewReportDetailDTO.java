package com.petguardian.sellerreview.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 賣家評價檢舉詳情 DTO
 * 用於後台檢舉管理頁面顯示
 */
@Getter
@Setter
public class SellerReviewReportDetailDTO {

    // 檢舉資訊
    private Integer reviewRptId;
    private Integer reportStatus;       // 0:待審核 1:成立 2:不成立
    private String reportReason;
    private LocalDateTime reportTime;

    // 評價資訊
    private Integer reviewId;
    private String reviewContent;
    private Integer rating;
    private LocalDateTime reviewTime;
    private Integer showStatus;

    // 訂單資訊
    private Integer orderId;

    // 評價人（買家）資訊
    private Integer buyerMemId;
    private String buyerName;

    // 被評價人（賣家）資訊
    private Integer sellerMemId;
    private String sellerName;

    // 檢舉人資訊
    private Integer reporterMemId;
    private String reporterName;

    // 輔助方法：取得狀態文字
    public String getStatusText() {
        if (reportStatus == null) return "未知";
        return switch (reportStatus) {
            case 0 -> "待審核";
            case 1 -> "檢舉成立";
            case 2 -> "檢舉不成立";
            default -> "未知";
        };
    }
}

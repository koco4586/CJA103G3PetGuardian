package com.petguardian.sellerreview.service;

import com.petguardian.sellerreview.model.SellerReviewReportVO;

import java.util.List;
import java.util.Optional;

public interface SellerReviewReportService {

    /**
     * 提交檢舉
     */
    SellerReviewReportVO createReport(Integer reviewId, Integer reporterMemId, String reportReason);

    /**
     * 根據檢舉ID查詢檢舉
     */
    Optional<SellerReviewReportVO> getReportById(Integer reviewRptId);

    /**
     * 查詢評價的所有檢舉
     */
    List<SellerReviewReportVO> getReportsByReviewId(Integer reviewId);

    /**
     * 查詢檢舉人的所有檢舉
     */
    List<SellerReviewReportVO> getReportsByReporterId(Integer reporterMemId);

    /**
     * 查詢指定狀態的檢舉
     */
    List<SellerReviewReportVO> getReportsByStatus(Integer reportStatus);

    /**
     * 查詢所有檢舉
     */
    List<SellerReviewReportVO> getAllReports();

    /**
     * 更新檢舉狀態
     */
    SellerReviewReportVO updateReportStatus(Integer reviewRptId, Integer newStatus);

    /**
     * 檢查是否已檢舉過
     */
    boolean hasReported(Integer reviewId, Integer reporterMemId);

    /**
     * 統計評價被檢舉次數
     */
    Long countReportsByReviewId(Integer reviewId);
}

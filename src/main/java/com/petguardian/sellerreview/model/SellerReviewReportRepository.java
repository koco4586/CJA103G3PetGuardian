package com.petguardian.sellerreview.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerReviewReportRepository extends JpaRepository<SellerReviewReportVO, Integer> {

    // 根據評價ID查詢所有檢舉
    List<SellerReviewReportVO> findByReviewIdOrderByReportTimeDesc(Integer reviewId);

    // 根據檢舉人ID查詢檢舉
    List<SellerReviewReportVO> findByReporterMemIdOrderByReportTimeDesc(Integer reporterMemId);

    // 查詢指定狀態的檢舉
    List<SellerReviewReportVO> findByReportStatusOrderByReportTimeDesc(Integer reportStatus);

    // 查詢所有檢舉（依檢舉時間降序）
    List<SellerReviewReportVO> findAllByOrderByReportTimeDesc();

    // 檢查是否已檢舉過此評價
    @Query("SELECT CASE WHEN COUNT(srr) > 0 THEN true ELSE false END FROM SellerReviewReportVO srr WHERE srr.reviewId = :reviewId AND srr.reporterMemId = :reporterMemId")
    boolean existsByReviewIdAndReporterMemId(@Param("reviewId") Integer reviewId, @Param("reporterMemId") Integer reporterMemId);

    // 統計評價被檢舉次數
    Long countByReviewId(Integer reviewId);

    // 統計指定狀態的檢舉數量
    Long countByReportStatus(Integer reportStatus);
}

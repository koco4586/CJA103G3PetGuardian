package com.petguardian.sellerreview.service;


import com.petguardian.sellerreview.model.SellerReviewReportRepository;
import com.petguardian.sellerreview.model.SellerReviewReportVO;
import com.petguardian.sellerreview.model.SellerReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SellerReviewReportServiceImpl implements SellerReviewReportService {

    @Autowired
    private SellerReviewReportRepository reportDAO;

    @Autowired
    private SellerReviewRepository sellerReviewDAO;

    @Autowired
    private SellerReviewService sellerReviewService;

    // 檢舉狀態常數
    public static final Integer REPORT_STATUS_PENDING = 0;    // 待審核
    public static final Integer REPORT_STATUS_APPROVED = 1;   // 成立
    public static final Integer REPORT_STATUS_REJECTED = 2;   // 不成立

    @Override
    public SellerReviewReportVO createReport(Integer reviewId, Integer reporterMemId, String reportReason) {
        if (reviewId == null) {
            throw new IllegalArgumentException("評價ID不能為 null");
        }
        if (reporterMemId == null) {
            throw new IllegalArgumentException("檢舉人會員ID不能為 null");
        }
        if (reportReason == null || reportReason.trim().isEmpty()) {
            throw new IllegalArgumentException("檢舉原因不能為空");
        }
        if (reportReason.length() > 1000) {
            throw new IllegalArgumentException("檢舉原因不能超過1000字");
        }

        // 檢查評價是否存在
        sellerReviewDAO.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("評價不存在: " + reviewId));

        // 檢查是否已檢舉過
        if (reportDAO.existsByReviewIdAndReporterMemId(reviewId, reporterMemId)) {
            throw new IllegalArgumentException("您已檢舉過此評價");
        }

        // 建立檢舉
        SellerReviewReportVO report = new SellerReviewReportVO();
        report.setReviewId(reviewId);
        report.setReporterMemId(reporterMemId);
        report.setReportReason(reportReason);
        report.setReportStatus(REPORT_STATUS_PENDING);

        return reportDAO.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SellerReviewReportVO> getReportById(Integer reviewRptId) {
        if (reviewRptId == null) {
            throw new IllegalArgumentException("檢舉ID不能為 null");
        }
        return reportDAO.findById(reviewRptId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerReviewReportVO> getReportsByReviewId(Integer reviewId) {
        if (reviewId == null) {
            throw new IllegalArgumentException("評價ID不能為 null");
        }
        return reportDAO.findByReviewIdOrderByReportTimeDesc(reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerReviewReportVO> getReportsByReporterId(Integer reporterMemId) {
        if (reporterMemId == null) {
            throw new IllegalArgumentException("檢舉人會員ID不能為 null");
        }
        return reportDAO.findByReporterMemIdOrderByReportTimeDesc(reporterMemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerReviewReportVO> getReportsByStatus(Integer reportStatus) {
        if (reportStatus == null) {
            throw new IllegalArgumentException("檢舉狀態不能為 null");
        }
        return reportDAO.findByReportStatusOrderByReportTimeDesc(reportStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerReviewReportVO> getAllReports() {
        return reportDAO.findAllByOrderByReportTimeDesc();
    }

    @Override
    public SellerReviewReportVO updateReportStatus(Integer reviewRptId, Integer newStatus) {
        if (reviewRptId == null) {
            throw new IllegalArgumentException("檢舉ID不能為 null");
        }
        if (newStatus == null || (newStatus < 0 || newStatus > 2)) {
            throw new IllegalArgumentException("檢舉狀態必須是0, 1或2");
        }

        // 查詢檢舉
        SellerReviewReportVO report = reportDAO.findById(reviewRptId)
                .orElseThrow(() -> new IllegalArgumentException("檢舉不存在: " + reviewRptId));

        // 更新檢舉狀態
        report.setReportStatus(newStatus);
        SellerReviewReportVO updatedReport = reportDAO.save(report);

        // 如果檢舉成立，隱藏該評價
        if (newStatus.equals(REPORT_STATUS_APPROVED)) {
            sellerReviewService.updateShowStatus(report.getReviewId(), 1); // 1:不顯示
        }

        return updatedReport;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReported(Integer reviewId, Integer reporterMemId) {
        if (reviewId == null || reporterMemId == null) {
            throw new IllegalArgumentException("評價ID和檢舉人ID不能為 null");
        }
        return reportDAO.existsByReviewIdAndReporterMemId(reviewId, reporterMemId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countReportsByReviewId(Integer reviewId) {
        if (reviewId == null) {
            throw new IllegalArgumentException("評價ID不能為 null");
        }
        return reportDAO.countByReviewId(reviewId);
    }
}

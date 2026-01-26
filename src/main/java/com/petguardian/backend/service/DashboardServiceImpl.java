package com.petguardian.backend.service;

import com.petguardian.sitter.model.SitterApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.backend.repository.BackendMemberRepository;
import com.petguardian.orders.model.ReturnOrderRepository;
import com.petguardian.sellerreview.model.SellerReviewReportRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * 後台儀表板統計 Service 實作
 * 整合各模組的統計數據
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private BackendMemberRepository memberRepository;

    @Autowired
    private SitterApplicationRepository sitterApplicationRepository;

    @Autowired
    private ReturnOrderRepository returnOrderRepository;

    @Autowired
    private SellerReviewReportRepository sellerReviewReportRepository;

    /**
     * 取得儀表板統計數據
     */
    @Override
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 會員總數（排除已停權的會員，mem_status = 0 表示正常）
        long totalMembers = memberRepository.countByMemStatus(0);
        stats.put("totalMembers", totalMembers);

        // 待審保母（狀態不是已通過，app_status != 1）
        long pendingSitters = sitterApplicationRepository.countByAppStatusNot((byte) 1);
        stats.put("pendingSitters", pendingSitters);

        // 待處理退款（退貨狀態為審核中，return_status = 0）
        long pendingRefunds = returnOrderRepository.countByReturnStatus(0);
        stats.put("pendingRefunds", pendingRefunds);

        // 待處理評價（評價檢舉狀態為待審核，report_status = 0）
        long pendingReviews = sellerReviewReportRepository.countByReportStatus(0);
        stats.put("pendingReviews", pendingReviews);

        return stats;
    }
}
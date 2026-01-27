package com.petguardian.backend.service;

import com.petguardian.backend.model.BackendMemberRepository;
import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.orders.model.ReturnOrderRepository;
import com.petguardian.sellerreview.model.SellerReviewReportRepository;
import com.petguardian.sitter.model.SitterApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private BookingOrderRepository bookingOrderRepository;

    /**
     * 取得儀表板統計數據
     */
    @Override
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 會員總數（排除已停權的會員，mem_status = 0 表示正常）
        long totalMembers = memberRepository.countByMemStatus(0);
        stats.put("totalMembers", totalMembers);

        // 待審保母（狀態為 0:待審核）
        long pendingSitters = sitterApplicationRepository.countByAppStatus((byte) 0);
        stats.put("pendingSitters", pendingSitters);

        // 預約待處理退款（預約訂單狀態為 3:申請退款中）
        long bookingPendingRefunds = bookingOrderRepository.countByOrderStatus(3);
        stats.put("bookingPendingRefunds", bookingPendingRefunds);

        // 商城待處理退款（退貨狀態為審核中，return_status = 0）
        long storePendingRefunds = returnOrderRepository.countByReturnStatus(0);
        stats.put("storePendingRefunds", storePendingRefunds);

        // 預約待處理評價（預約訂單評價狀態為未評價，review_status = 0）
        long bookingPendingReviews = 0;
        stats.put("bookingPendingReviews", bookingPendingReviews);

        // 商城待處理評價（評價檢舉狀態為待審核，report_status = 0）
        long storePendingReviews = sellerReviewReportRepository.countByReportStatus(0);
        stats.put("storePendingReviews", storePendingReviews);

        return stats;
    }
}
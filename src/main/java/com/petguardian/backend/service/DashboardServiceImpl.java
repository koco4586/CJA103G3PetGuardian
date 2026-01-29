package com.petguardian.backend.service;

import com.petguardian.backend.model.BackendMemberRepository;
import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.complaint.model.Complaintrepository;
import com.petguardian.orders.model.ReturnOrderRepository;
import com.petguardian.sellerreview.model.SellerReviewReportRepository;
import com.petguardian.sitter.model.SitterApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 後台儀表板統計 Service 實作
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

    @Autowired
    private Complaintrepository complaintRepository;

    /**
     * 取得儀表板統計數據
     */
    @Override
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 會員總數（mem_status = 1 表示啟用）
        long totalMembers = memberRepository.countByMemStatus(1);
        stats.put("totalMembers", totalMembers);

        // 待審保母（app_status = 0 表示待審核）
        long pendingSitters = sitterApplicationRepository.countByAppStatus((byte) 0);
        stats.put("pendingSitters", pendingSitters);

        // 預約待處理退款（預約訂單狀態為 3:申請退款中）
        long bookingPendingRefunds = bookingOrderRepository.countByOrderStatus(3);
        stats.put("bookingPendingRefunds", bookingPendingRefunds);

        // 商城待處理退款（return_status = 0 表示審核中）
        long storePendingRefunds = returnOrderRepository.countByReturnStatus(0);
        stats.put("storePendingRefunds", storePendingRefunds);

        // 預約待處理評價（booking_order_report 的 report_status = 0 表示未處理）
        long bookingPendingReviews = complaintRepository.countByReportStatus(0);
        stats.put("bookingPendingReviews", bookingPendingReviews);

        // 商城待處理評價（seller_review_report 的 report_status = 0 表示待審核）
        long storePendingReviews = sellerReviewReportRepository.countByReportStatus(0);
        stats.put("storePendingReviews", storePendingReviews);

        return stats;
    }
}
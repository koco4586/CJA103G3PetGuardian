package com.petguardian.backend.service;

import com.petguardian.backend.model.BackendAdminRepository;
import com.petguardian.backend.model.BackendMemberRepository;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.complaint.model.Complaintrepository;
import com.petguardian.orders.model.ReturnOrderRepository;
import com.petguardian.sellerreview.model.SellerReviewReportRepository;
import com.petguardian.sitter.model.SitterApplicationRepository;
import com.petguardian.chat.model.ChatReportRepository;
import com.petguardian.forum.model.ForumCommentReportRepository;
import com.petguardian.forum.model.ForumPostReportRepository;
import com.petguardian.news.model.NewsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 後台儀表板統計 Service 實作
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    // 1. 自定 管理員
    @Autowired
    private BackendAdminRepository backendAdminRepository;

    // 2. 自定 會員
    @Autowired
    private BackendMemberRepository backendMemberRepository;

    // 3. 保母申請
    @Autowired
    private SitterApplicationRepository sitterApplicationRepository;

    // 4. 預約訂單
    @Autowired
    private BookingOrderRepository bookingOrderRepository;

    // 5. 預約評價檢舉
    @Autowired
    private Complaintrepository complaintRepository;

    // 6. 聊天室檢舉
    @Autowired
    private ChatReportRepository chatReportRepository;

    // 7. 最新消息
    @Autowired
    private NewsRepository newsRepository;

    // 8. 商城退款
    @Autowired
    private ReturnOrderRepository returnOrderRepository;

    // 9. 商城評價檢舉
    @Autowired
    private SellerReviewReportRepository sellerReviewReportRepository;

    // 10. 論壇留言檢舉
    @Autowired
    private ForumCommentReportRepository forumCommentReportRepository;

    // 11. 論壇文章檢舉
    @Autowired
    private ForumPostReportRepository forumPostReportRepository;

    /**
     * 取得儀表板統計數據
     */
    @Override
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 1. 管理員總數 (使用自定義 native query)
        long totalAdmins = backendAdminRepository.countActiveAdmins();
        stats.put("totalAdmins", totalAdmins);

        // 2. 會員總數 (使用自定義 native query)
        long totalMembers = backendMemberRepository.countActiveMembers();
        stats.put("totalMembers", totalMembers);

        // 3. 待審保母 (app_status = 0)
        long pendingSitters = sitterApplicationRepository.countByAppStatus((byte) 0);
        stats.put("pendingSitters", pendingSitters);

        // 4. 預約待審退款 (order_status = 3)
        long bookingPendingRefunds = bookingOrderRepository.countByOrderStatus(3);
        stats.put("bookingPendingRefunds", bookingPendingRefunds);

        // 5. 預約待審評價檢舉 (report_status = 0)
        long bookingPendingReviews = complaintRepository.countByReportStatus(0);
        stats.put("bookingPendingReviews", bookingPendingReviews);

        // 6. 聊天室待審檢舉 (report_status = 0)
        long chatPendingReports = chatReportRepository.countByReportStatus(0);
        stats.put("chatPendingReports", chatPendingReports);

        // 7. 已發布文章數量 (IS_PUBLISHED = 1)
        long publishedNewsCount = newsRepository.countByIsPublished(1);
        stats.put("publishedNewsCount", publishedNewsCount);

        // 8. 商城待審退款 (return_status = 0)
        long storePendingRefunds = returnOrderRepository.countByReturnStatus(0);
        stats.put("storePendingRefunds", storePendingRefunds);

        // 9. 商城待審評價檢舉 (report_status = 0)
        long storePendingReviews = sellerReviewReportRepository.countByReportStatus(0);
        stats.put("storePendingReviews", storePendingReviews);

        // 10. 論壇待審評價(留言)檢舉 (report_status = 0)
        long forumPendingCommentReports = forumCommentReportRepository.countByReportStatus(0);
        stats.put("forumPendingCommentReports", forumPendingCommentReports);

        // 11. 論壇待審文章檢舉 (report_status = 0)
        long forumPendingPostReports = forumPostReportRepository.countByReportStatus(0);
        stats.put("forumPendingPostReports", forumPendingPostReports);

        return stats;
    }
}
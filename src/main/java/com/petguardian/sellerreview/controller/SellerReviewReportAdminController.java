package com.petguardian.sellerreview.controller;

import com.petguardian.member.model.Member;
import com.petguardian.member.repository.management.MemberManagementRepository;
import com.petguardian.orders.model.OrdersRepository;
import com.petguardian.orders.model.OrdersVO;
import com.petguardian.sellerreview.dto.SellerReviewReportDetailDTO;
import com.petguardian.sellerreview.model.SellerReviewReportVO;
import com.petguardian.sellerreview.model.SellerReviewVO;
import com.petguardian.sellerreview.service.SellerReviewReportService;
import com.petguardian.sellerreview.service.SellerReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 後台賣家評價檢舉管理 Controller
 */
@Controller
@RequestMapping("/admin/store-reviews")
public class SellerReviewReportAdminController {

    @Autowired
    private SellerReviewReportService reportService;

    @Autowired
    private SellerReviewService reviewService;

    @Autowired
    private MemberManagementRepository memberRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    /**
     * 顯示檢舉管理頁面
     */
    @GetMapping
    public String showReportsPage(@RequestParam(defaultValue = "pending") String tab, Model model) {
        List<SellerReviewReportDetailDTO> reports;

        if ("closed".equals(tab)) {
            // 已結案（狀態1或2）
            List<SellerReviewReportVO> approvedReports = reportService.getReportsByStatus(1);
            List<SellerReviewReportVO> rejectedReports = reportService.getReportsByStatus(2);
            List<SellerReviewReportVO> closedReports = new ArrayList<>();
            closedReports.addAll(approvedReports);
            closedReports.addAll(rejectedReports);
            reports = convertToDetailDTOs(closedReports);
        } else {
            // 待處理（狀態0）
            List<SellerReviewReportVO> pendingReports = reportService.getReportsByStatus(0);
            reports = convertToDetailDTOs(pendingReports);
        }

        model.addAttribute("reports", reports);
        model.addAttribute("currentTab", tab);

        return "backend/store_reviews";
    }

    /**
     * 審核通過檢舉（刪除評價）
     */
    @PostMapping("/approve/{reportId}")
    public String approveReport(@PathVariable Integer reportId, RedirectAttributes redirectAttr) {
        try {
            reportService.updateReportStatus(reportId, 1); // 1:成立
            redirectAttr.addFlashAttribute("message", "檢舉已通過，評價已隱藏");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/store-reviews";
    }

    /**
     * 駁回檢舉
     */
    @PostMapping("/reject/{reportId}")
    public String rejectReport(@PathVariable Integer reportId, RedirectAttributes redirectAttr) {
        try {
            reportService.updateReportStatus(reportId, 2); // 2:不成立
            redirectAttr.addFlashAttribute("message", "檢舉已駁回");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/store-reviews";
    }

    /**
     * 將檢舉 VO 列表轉換為詳情 DTO 列表
     */
    private List<SellerReviewReportDetailDTO> convertToDetailDTOs(List<SellerReviewReportVO> reports) {
        if (reports.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集所有需要查詢的 ID
        List<Integer> reviewIds = reports.stream()
                .map(SellerReviewReportVO::getReviewId)
                .distinct()
                .collect(Collectors.toList());

        List<Integer> reporterMemIds = reports.stream()
                .map(SellerReviewReportVO::getReporterMemId)
                .distinct()
                .collect(Collectors.toList());

        // 批次查詢評價
        Map<Integer, SellerReviewVO> reviewMap = reviewIds.stream()
                .map(id -> reviewService.getReviewById(id).orElse(null))
                .filter(r -> r != null)
                .collect(Collectors.toMap(SellerReviewVO::getReviewId, Function.identity()));

        // 收集訂單 ID
        List<Integer> orderIds = reviewMap.values().stream()
                .map(SellerReviewVO::getOrderId)
                .distinct()
                .collect(Collectors.toList());

        // 批次查詢訂單
        Map<Integer, OrdersVO> orderMap = ordersRepository.findAllById(orderIds).stream()
                .collect(Collectors.toMap(OrdersVO::getOrderId, Function.identity()));

        // 收集會員 ID（買家、賣家、檢舉人）
        List<Integer> allMemIds = new ArrayList<>(reporterMemIds);
        orderMap.values().forEach(order -> {
            allMemIds.add(order.getBuyerMemId());
            allMemIds.add(order.getSellerMemId());
        });
        List<Integer> distinctMemIds = allMemIds.stream().distinct().collect(Collectors.toList());

        // 批次查詢會員
        Map<Integer, Member> memberMap = memberRepository.findAllById(distinctMemIds).stream()
                .collect(Collectors.toMap(Member::getMemId, Function.identity()));

        // 組裝 DTO
        List<SellerReviewReportDetailDTO> dtos = new ArrayList<>();
        for (SellerReviewReportVO report : reports) {
            SellerReviewReportDetailDTO dto = new SellerReviewReportDetailDTO();

            // 檢舉資訊
            dto.setReviewRptId(report.getReviewRptId());
            dto.setReportStatus(report.getReportStatus());
            dto.setReportReason(report.getReportReason());
            dto.setReportTime(report.getReportTime());

            // 檢舉人資訊
            dto.setReporterMemId(report.getReporterMemId());
            Member reporter = memberMap.get(report.getReporterMemId());
            dto.setReporterName(reporter != null ? reporter.getMemName() : "未知");

            // 評價資訊
            SellerReviewVO review = reviewMap.get(report.getReviewId());
            if (review != null) {
                dto.setReviewId(review.getReviewId());
                dto.setReviewContent(review.getReviewContent());
                dto.setRating(review.getRating());
                dto.setReviewTime(review.getReviewTime());
                dto.setShowStatus(review.getShowStatus());
                dto.setOrderId(review.getOrderId());

                // 訂單及會員資訊
                OrdersVO order = orderMap.get(review.getOrderId());
                if (order != null) {
                    dto.setBuyerMemId(order.getBuyerMemId());
                    dto.setSellerMemId(order.getSellerMemId());

                    Member buyer = memberMap.get(order.getBuyerMemId());
                    dto.setBuyerName(buyer != null ? buyer.getMemName() : "未知");

                    Member seller = memberMap.get(order.getSellerMemId());
                    dto.setSellerName(seller != null ? seller.getMemName() : "未知");
                }
            }

            dtos.add(dto);
        }

        return dtos;
    }
}

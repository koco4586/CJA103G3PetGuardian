package com.petguardian.chat.controller;

import com.petguardian.chat.model.ChatReport;
import com.petguardian.chat.service.chatmessage.report.ChatReportService;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.management.MemberManagementRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for back-office chat report management.
 */
@Controller
@RequestMapping("/admin/chat-reports")
public class ChatReportAdminController {

    @Autowired
    private ChatReportService chatReportService;

    @Autowired
    private MemberManagementRepository memberRepository;

    @GetMapping
    public String listReports(@RequestParam(defaultValue = "pending") String tab, Model model) {
        List<ChatReport> reports;

        if ("closed".equals(tab)) {
            reports = chatReportService.getClosedReports();
        } else {
            reports = chatReportService.getPendingReports();
        }

        model.addAttribute("reports", reports);
        model.addAttribute("currentTab", tab);

        // Pre-fetch all relevant member names to avoid N+1 in template
        Set<Integer> memberIdSet = new HashSet<>();
        if (reports != null) {
            for (ChatReport report : reports) {
                if (report.getReporterId() != null) {
                    memberIdSet.add(report.getReporterId());
                }
                if (report.getMessage() != null && report.getMessage().getMemberId() != null) {
                    memberIdSet.add(report.getMessage().getMemberId());
                }
            }
        }
        List<Integer> memberIds = new ArrayList<>(memberIdSet);

        Map<Integer, String> memberNameMap = new HashMap<>();
        if (!memberIds.isEmpty()) {
            List<Member> members = memberRepository.findAllById(memberIds);
            memberNameMap = members.stream()
                    .collect(Collectors.toMap(Member::getMemId, Member::getMemName));
        }
        model.addAttribute("memberNameMap", memberNameMap);

        return "backend/chat_reports";
    }

    @PostMapping("/approve/{id}")
    public String approveReport(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttr) {
        try {
            // Get current admin ID
            Integer handlerId = (Integer) session.getAttribute("admId");
            if (handlerId == null) {
                return "redirect:/adminloginpage";
            }

            chatReportService.updateReportStatus(id, 2, handlerId, "Report Processed"); // 2: Processed
            redirectAttr.addFlashAttribute("message", "檢舉已處理 (Approved)");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", "處理失敗: " + e.getMessage());
        }
        return "redirect:/admin/chat-reports";
    }

    @PostMapping("/reject/{id}")
    public String rejectReport(@PathVariable Integer id, HttpSession session, RedirectAttributes redirectAttr) {
        try {
            Integer handlerId = (Integer) session.getAttribute("admId");
            if (handlerId == null) {
                return "redirect:/adminloginpage";
            }

            chatReportService.updateReportStatus(id, 3, handlerId, "Report Rejected"); // 3: Rejected
            redirectAttr.addFlashAttribute("message", "檢舉已駁回 (Rejected)");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", "處理失敗: " + e.getMessage());
        }
        return "redirect:/admin/chat-reports";
    }
}

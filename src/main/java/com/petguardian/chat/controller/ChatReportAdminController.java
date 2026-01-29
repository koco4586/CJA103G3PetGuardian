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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        // Pre-fetch reporter names to avoid N+1 in template (though we have @ManyToOne,
        // batch fetching or simple map lookup is safer for consistency with previous
        // controller style)
        // Note: ChatReport has direct Member relation, so we could technically use
        // report.reporter.memName
        // but let's stick to the map pattern used in existing codebase if Lazy Loading
        // is an issue.
        // Actually, for simplicity and performance with existing
        // OpenEntityManagerInView,
        // we can rely on Lazy Loading or use a simple Fetch below.

        // Let's populate a map for safety if entities are detached or Lazy Init
        // exception risks
        // Collecting Reporter IDs
        List<Integer> memberIds = reports.stream()
                .map(ChatReport::getReporterId)
                .distinct()
                .collect(Collectors.toList());

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

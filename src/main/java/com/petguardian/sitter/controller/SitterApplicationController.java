package com.petguardian.sitter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petguardian.sitter.model.SitterApplicationVO;
import com.petguardian.sitter.service.SitterApplicationService;

@Controller
@RequestMapping("/sitter-application")
public class SitterApplicationController {

    @Autowired
    private SitterApplicationService service;

    // 1. 導向申請表格頁面
    // URL: /sitter-application/apply
    @GetMapping("/apply")
    public String showApplyForm() {
        return "sitter/application_form"; // 對應 templates/sitter/application_form.html
    }

    // 2. 處理申請送出
    // URL: /sitter-application/submit
    @PostMapping("/submit")
    public String submitApplication(
            @RequestParam("memId") Integer memId,
            @RequestParam("intro") String intro,
            @RequestParam("experience") String experience,
            Model model) {

        // 呼叫 Service 新增申請
        SitterApplicationVO vo = service.createApplication(memId, intro, experience);

        // 將結果放入 Model (可選)
        model.addAttribute("application", vo);

        // 導向成功頁面或列表頁
        return "redirect:/sitter-application/list?memId=" + memId;
    }

    // 3. 查詢會員的申請列表
    // URL: /sitter-application/list
    @GetMapping("/list")
    public String listApplications(@RequestParam(value = "memId", required = false) Integer memId, Model model) {
        List<SitterApplicationVO> list;
        if (memId != null) {
            list = service.getApplicationsByMember(memId);
        } else {
            // 若沒傳 memId，這邊先列出所有(或導回登入)，視業務需求
            // 這裡示範列出所有
            list = service.getAllApplications();
        }
        model.addAttribute("applications", list);
        return "sitter/application_list"; // 對應 templates/sitter/application_list.html
    }
}

package com.petguardian.admin.controller.insert;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminInsertRedirectController {

    @GetMapping("/admininsertpage")
    public String admininsertpage(){
        return "forward:/html/backend/admin/admin_insert.html";
    }
}

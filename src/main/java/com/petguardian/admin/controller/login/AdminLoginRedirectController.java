package com.petguardian.admin.controller.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminLoginRedirectController {

    @GetMapping("/adminloginpage")
    public String adminloginpage(){

        return "redirect:/html/backend/admin/admin_login.html";

    }

}

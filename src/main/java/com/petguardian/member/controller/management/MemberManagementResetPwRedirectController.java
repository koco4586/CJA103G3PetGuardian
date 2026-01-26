package com.petguardian.member.controller.management;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
public class MemberManagementResetPwRedirectController {

    @GetMapping("/resetpwpage")
    public String resetpw(){
        return "redirect:/html/member/management/member_management_resetpw.html";
    }

}

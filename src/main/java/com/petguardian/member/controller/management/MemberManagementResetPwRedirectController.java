package com.petguardian.member.controller.management;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/petguardian")
public class MemberManagementResetPwRedirectController {

    @GetMapping("/resetpw")
    public String resetpw(){
        return "redirect:/html/member/management/member_management_resetpw.html";
    }

}

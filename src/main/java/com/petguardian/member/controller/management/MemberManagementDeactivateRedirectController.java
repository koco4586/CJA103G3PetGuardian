package com.petguardian.member.controller.management;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
public class MemberManagementDeactivateRedirectController {

    @GetMapping("/deactivatepage")
    public String deactivatepage(){
        return "redirect:/html/frontend/member/management/member_management_deactivate.html";
    }

}

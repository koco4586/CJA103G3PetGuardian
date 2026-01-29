package com.petguardian.member.controller.management;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
public class MemberManagementRedirectController {

    @GetMapping("/managementpage")
    public String management(){

        return "redirect:/html/frontend/member/management/member_management_ai.html";//記得要改回來

}

}

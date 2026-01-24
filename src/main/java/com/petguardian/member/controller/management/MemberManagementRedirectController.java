package com.petguardian.member.controller.management;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/petguardian")
public class MemberManagementRedirectController {

    @GetMapping("/management")
    public String management(){

        return "redirect:/html/member/management/member_management.html";

}

}

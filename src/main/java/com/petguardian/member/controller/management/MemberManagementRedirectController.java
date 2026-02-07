package com.petguardian.member.controller.management;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
public class MemberManagementRedirectController {

    @GetMapping("/managementpage")
    public String managementpage(){

        return "forward:/html/frontend/member/management/member_management.html";//"redirect:/html/frontend/member/management/member_management.html";//記得要改回來

}

}

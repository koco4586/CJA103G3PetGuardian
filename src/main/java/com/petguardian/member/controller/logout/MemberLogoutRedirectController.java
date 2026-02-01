package com.petguardian.member.controller.logout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
public class MemberLogoutRedirectController {

	@GetMapping("/memberlogoutpage")
    public String memberlogout() {

        return "redirect:/html/frontend/member/logout/member_logout.html";

    }

}

package com.petguardian.member.controller.logout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
public class MemberLogoutSuccessRedirectController {

	@GetMapping("/logoutsuccesspage")
	public String logoutsuccesspage() {
		return "forward:/html/frontend/member/logout/memberlogoutsuccess.html";
	}
}
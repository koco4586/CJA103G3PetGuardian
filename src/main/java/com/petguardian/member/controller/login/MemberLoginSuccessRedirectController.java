package com.petguardian.member.controller.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
public class MemberLoginSuccessRedirectController {

	@GetMapping("/loginsuccesspage")
	public String loginsuccesspage() {
		return "forward:/html/frontend/member/login/memberloginsuccess.html";
	}
}
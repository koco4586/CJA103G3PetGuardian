package com.petguardian.member.controller.register;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
public class MemberRegisterSuccessRedirectController {

	@GetMapping("/registersuccesspage")
	public String registersuccesspage() {
		return "forward:/html/frontend/member/register/memberregistersuccess.html";
	}
}
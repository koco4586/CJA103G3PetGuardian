package com.petguardian.admin.controller.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminLoginSuccessRedirectController {

	@GetMapping("/adminloginsuccess")
	public String adminloginsuccess() {
		return "forward:/html/backend/admin/admin_login_success.html";
	}
}
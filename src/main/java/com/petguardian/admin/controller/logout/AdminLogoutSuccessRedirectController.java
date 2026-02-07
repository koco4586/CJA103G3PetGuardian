package com.petguardian.admin.controller.logout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminLogoutSuccessRedirectController {

	@GetMapping("/adminlogoutsuccess")
	public String adminlogoutsuccess() {
		return "forward:/html/backend/admin/adminlogout/adminlogoutsuccess.html";
	}
}
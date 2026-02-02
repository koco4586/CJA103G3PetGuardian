package com.petguardian.admin.controller.logout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminLogoutRedirectController {
	
	@GetMapping("/adminlogoutpage")
	public String adminlogout() {
		
		return "redirect:/html/backend/admin/admin_logout.html";
		
	}

}

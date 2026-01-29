package com.petguardian.admin.controller.adminadminmanagement;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminAdminManagementRedirectController {
	
	@GetMapping("/admin-admin-managementpage")
	public String adminAdminManagementpage() {
		
		return "redirect:/html/backend/admin/admin_admin_management.html";
		
	}

}
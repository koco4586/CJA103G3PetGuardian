package com.petguardian.member.controller.adminmembermanagement;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminMemberManagementRedirectController {

	@GetMapping("/admin-member-managementpage")
	public String adminMemberManagementpage() {
	
	return "redirect:/html/backend/member/admin_member_management.html";

	}
}

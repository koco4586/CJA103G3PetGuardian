package com.petguardian.admin.controller.updateinfo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminManagementUpdateInfoRedirectController {

	@GetMapping("/adminupdateinfopage")
	public String adminupdateinfopage(){
	return "redirect:/html/backend/admin/admin_management_updateinfo.html";	
	}
}

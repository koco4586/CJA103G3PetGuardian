package com.petguardian.admin.controller.resetpw;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminManagementResetPwRediectController {

	@GetMapping("/adminresetpwpage")
	public String adminresetpwpage() {
		return "forward:/html/backend/admin/admin_resetpw.html";
	}

}

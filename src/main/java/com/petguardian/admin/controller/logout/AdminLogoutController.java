package com.petguardian.admin.controller.logout;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.admin.service.logout.AdminLogoutService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/admin")
public class AdminLogoutController {

	@Autowired
	private AdminLogoutService adminLogoutService;

	@PostMapping("/adminlogout")
	public Map<String, String> adminlogout(HttpSession session) {

		Map<String, String> map = new HashMap<>();

		String result = adminLogoutService.adminlogout(session);

		map.put("result", result);

		return map;

	}

}

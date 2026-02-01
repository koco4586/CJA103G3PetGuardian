package com.petguardian.admin.service.logout;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class AdminLogoutService {

	public String adminlogout(HttpSession session) {

		session.invalidate();

		return "登出成功";

	}

}

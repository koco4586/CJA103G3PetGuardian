package com.petguardian.admin.service.logout;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class AdminLogoutService {

	public String adminlogout(HttpSession session) {

//		session.invalidate();

		// 只移除管理員相關的屬性，不銷毀整個 session
		session.removeAttribute("admId");

		return "登出成功";

	}

}

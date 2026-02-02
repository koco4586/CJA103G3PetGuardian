package com.petguardian.member.service.logout;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class MemberLogoutService {

	public String memberlogout(HttpSession session) {

		// 只移除會員相關的屬性，不要銷毀整個 session
		session.removeAttribute("memId");

		// session.invalidate();

		return "登出成功";

	}

}

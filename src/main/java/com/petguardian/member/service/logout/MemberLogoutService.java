package com.petguardian.member.service.logout;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class MemberLogoutService {
	
	public String memberlogout(HttpSession session) {

		session.invalidate();

		return "登出成功";

	}

}

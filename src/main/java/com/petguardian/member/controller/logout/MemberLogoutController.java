package com.petguardian.member.controller.logout;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.member.service.logout.MemberLogoutService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/front")
public class MemberLogoutController {

	@Autowired
	private MemberLogoutService memberLogoutService;

	@PostMapping("/memberlogout")
	public Map<String, String> memberlogout(HttpSession session) {

		Map<String, String> map = new HashMap<>();

		String result = memberLogoutService.memberlogout(session);

		map.put("result", result);

		return map;
	}
}

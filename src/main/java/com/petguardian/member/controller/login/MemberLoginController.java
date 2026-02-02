package com.petguardian.member.controller.login;

import java.util.HashMap;
import java.util.Map;

import com.petguardian.member.dto.MemberLoginDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.service.login.MemberLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/front")
public class MemberLoginController {

	@Autowired
	private MemberLoginService memberLoginService;

	@PostMapping("/login")
	public Map<String, String> login(@RequestBody MemberLoginDTO memberLoginDTO, HttpSession session) {

		Map<String, String> map = new HashMap<>();

		Member member = memberLoginService.login(memberLoginDTO);

		if (member == null) {

			map.put("result", "帳號或密碼輸入錯誤");

			return map;

			// 檢查會員狀態  (0=停權, 1=啟用)
		} else if (member.getMemStatus() == 0) {
			map.put("result", "此帳號已被停權,無法登入");
			return map;
		}

		else {

			session.setAttribute("memId", member.getMemId());

			map.put("result", "登入成功");

			return map;

		}

	}

}

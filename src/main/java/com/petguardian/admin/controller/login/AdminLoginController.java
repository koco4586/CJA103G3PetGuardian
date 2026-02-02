package com.petguardian.admin.controller.login;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.admin.dto.login.AdminLoginDTO;
import com.petguardian.admin.model.Admin;
import com.petguardian.admin.service.login.AdminLoginService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/admin")
public class AdminLoginController {

	@Autowired
	private AdminLoginService adminLoginService;

	@PostMapping("/adminlogin")
	public Map<String, String> adminlogin(@RequestBody AdminLoginDTO adminLoginDTO, HttpSession session) {

		Map<String, String> map = new HashMap<>();

		Admin admin = adminLoginService.adminlogin(adminLoginDTO);

		if (admin == null) {

			map.put("result", "帳號或密碼輸入錯誤");

			return map;
		}

		// 檢查管理員狀態 (0=停權, 1=啟用)
		else if (admin.getAdmStatus() == 0) {

			map.put("result", "此帳號已被停權,無法登入");
			
			return map;

		}

		else {

			session.setAttribute("admId", admin.getAdmId());

			map.put("result", "登入成功");

			return map;

		}

	}

}
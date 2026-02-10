package com.petguardian.admin.controller.resetpw;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.admin.dto.resetpw.AdminManagementResetPwDTO;
import com.petguardian.admin.service.resetpw.AdminManagementResetPwService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminManagementResetPwController {

	@Autowired
	private AdminManagementResetPwService adminManagementResetPwService;

	@PutMapping("/adminresetpw")
	public Map<String, String> adminresetpw(@RequestBody @Valid AdminManagementResetPwDTO adminManagementResetPwDTO,
			HttpSession session) {

		Map<String, String> map = new HashMap<>();

		Integer admId = (Integer) session.getAttribute("admId");

		String result = adminManagementResetPwService.adminresetpw(adminManagementResetPwDTO, admId);

		map.put("result", result);

		return map;

	}

}

package com.petguardian.admin.controller.adminadminmanagement;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.admin.dto.adminadminmanagement.AdminAdminManagementSelectDTO;
import com.petguardian.admin.service.adminadminmanagement.AdminAdminManagementService;

@RestController
@RequestMapping("/admin")
public class AdminAdminManagementController {

	@Autowired
	private AdminAdminManagementService adminAdminManagementService;

	@GetMapping("/admin-admin-management")
	public List<AdminAdminManagementSelectDTO> findAll() {

		return adminAdminManagementService.findAll();

	}

}

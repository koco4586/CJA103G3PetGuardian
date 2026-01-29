package com.petguardian.admin.controller.adminadminmanagement;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.admin.dto.adminadminmanagement.AdminAdminManagementUpdateStatusDTO;
import com.petguardian.admin.service.adminadminmanagement.AdminAdminManagementUpdateStatusService;

@RestController
@RequestMapping("/admin")
public class AdminAdminManagementUpdateStatusController {

	@Autowired
	private AdminAdminManagementUpdateStatusService adminAdminManagementUpdateStatusService;

	@PutMapping("/admin-admin-management-updatestatus")
	public Map<String, String> updatestatus(
			@RequestBody AdminAdminManagementUpdateStatusDTO adminAdminManagementUpdateStatusDTO) {

		Map<String, String> map = new HashMap<>();

		String result = adminAdminManagementUpdateStatusService.updatestatus(adminAdminManagementUpdateStatusDTO);
		map.put("result", result);

		return map;

	}

}

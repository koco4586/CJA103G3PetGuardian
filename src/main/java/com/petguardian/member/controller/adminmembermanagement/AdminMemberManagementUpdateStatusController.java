package com.petguardian.member.controller.adminmembermanagement;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.member.dto.AdminMemberManagementUpdateStatusDTO;
import com.petguardian.member.service.adminmembermanagement.AdminMemberManagementUpdateStatusService;

@RestController
@RequestMapping("/admin")
public class AdminMemberManagementUpdateStatusController {

	@Autowired
	private AdminMemberManagementUpdateStatusService adminMemberManagementUpdateStatusService;
	
	@PutMapping("/admin-member-management-updatestatus")
	public Map<String,String> updatestatus(@RequestBody AdminMemberManagementUpdateStatusDTO adminMemberManagementUpdateStatusDTO){
		
		Map<String,String> map = new HashMap<>();
		
		String result = adminMemberManagementUpdateStatusService.updatestatus(adminMemberManagementUpdateStatusDTO);
		
		map.put("result", result);
		
		return map;
		
	}
	
}

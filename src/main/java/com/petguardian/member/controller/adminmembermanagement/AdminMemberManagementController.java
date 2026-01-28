package com.petguardian.member.controller.adminmembermanagement;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.member.dto.AdminMemberManagementSelectDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.service.adminmembermanagement.AdminMemberManagementService;

@RestController
@RequestMapping("/admin")
public class AdminMemberManagementController {

	@Autowired
	private AdminMemberManagementService adminMemberManagementService;
	
	@GetMapping("/admin-member-management")
	public List<AdminMemberManagementSelectDTO> findAll() {
		
		return adminMemberManagementService.findAll();
		
	}
	
}

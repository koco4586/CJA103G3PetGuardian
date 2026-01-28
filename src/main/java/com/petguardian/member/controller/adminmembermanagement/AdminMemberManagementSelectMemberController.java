package com.petguardian.member.controller.adminmembermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.member.dto.AdminMemberManagementSelectDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.service.adminmembermanagement.AdminMemberManagementSelectMemberService;

@RestController
@RequestMapping("/admin")
public class AdminMemberManagementSelectMemberController {

	@Autowired
	private AdminMemberManagementSelectMemberService adminMemberManagementSelectMemberService;
	
	@GetMapping("/admin-member-management-searchmember")
	public AdminMemberManagementSelectDTO searchmember(@RequestParam Integer memId) {
		
		return adminMemberManagementSelectMemberService.searchmember(memId);
		
	} 
}

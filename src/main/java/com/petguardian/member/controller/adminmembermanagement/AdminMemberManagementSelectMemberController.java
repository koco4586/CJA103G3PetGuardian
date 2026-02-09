package com.petguardian.member.controller.adminmembermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.member.dto.AdminMemberManagementSelectDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.service.adminmembermanagement.AdminMemberManagementSelectMemberService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminMemberManagementSelectMemberController {

	@Autowired
	private AdminMemberManagementSelectMemberService adminMemberManagementSelectMemberService;

	@GetMapping("/admin-member-management-searchmember")
	public AdminMemberManagementSelectDTO searchmember(
			@RequestParam @Min(value = 1001, message = "會員編號需大於1001") @NotNull(message = "會員編號不得為空值") Integer memId) {

		return adminMemberManagementSelectMemberService.searchmember(memId);

	}
}

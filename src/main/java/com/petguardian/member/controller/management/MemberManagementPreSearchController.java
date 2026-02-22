package com.petguardian.member.controller.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.member.dto.MemberManagementPreSearchDTO;
import com.petguardian.member.service.management.MemberManagementPreSearchService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/front")
public class MemberManagementPreSearchController {

	@Autowired
	private MemberManagementPreSearchService memberManagementPreSearchService;

	@GetMapping("/presearch-member-management")
	public MemberManagementPreSearchDTO presearchmember(HttpSession session) {

		Integer memId = (Integer) session.getAttribute("memId");

		return memberManagementPreSearchService.presearchmember(memId);

	}

}

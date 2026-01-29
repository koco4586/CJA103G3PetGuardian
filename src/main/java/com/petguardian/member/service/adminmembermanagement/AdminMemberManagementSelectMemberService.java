package com.petguardian.member.service.adminmembermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.member.dto.AdminMemberManagementSelectDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.adminmembermanagement.AdminMemberManagementSelectMemberRepository;

@Service
public class AdminMemberManagementSelectMemberService {

	@Autowired
	private AdminMemberManagementSelectMemberRepository adminMemberManagementSelectMemberRepository;

	public AdminMemberManagementSelectDTO searchmember(Integer memId) {

		Member member = adminMemberManagementSelectMemberRepository.findById(memId).orElse(null);

		AdminMemberManagementSelectDTO adminMemberManagementSelectDTO = new AdminMemberManagementSelectDTO();

		adminMemberManagementSelectDTO.setMemId(member.getMemId());

		adminMemberManagementSelectDTO.setMemName(member.getMemName());

		adminMemberManagementSelectDTO.setMemEmail(member.getMemEmail());

		adminMemberManagementSelectDTO.setMemStatus(member.getMemStatus());

		adminMemberManagementSelectDTO.setMemCreatedAt(member.getMemCreatedAt());

		adminMemberManagementSelectDTO.setMemImage(member.getMemImage());
		
		return adminMemberManagementSelectDTO;

	}

}

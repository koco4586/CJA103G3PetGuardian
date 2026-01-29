package com.petguardian.member.service.adminmembermanagement;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.member.dto.AdminMemberManagementSelectDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.adminmembermanagement.AdminMemberManagementRepository;

@Service
public class AdminMemberManagementService {

	@Autowired
	private AdminMemberManagementRepository adminMemberManagementRepository;

	public List<AdminMemberManagementSelectDTO> findAll() {

		List<Member> memberList = adminMemberManagementRepository.findAll();

		List<AdminMemberManagementSelectDTO> adminMemberManagementSelectDTOList = new ArrayList<>();
		
		for(Member member : memberList) {
		
			AdminMemberManagementSelectDTO adminMemberManagementSelectDTO = new AdminMemberManagementSelectDTO();
			
			adminMemberManagementSelectDTO.setMemId(member.getMemId());
			
			adminMemberManagementSelectDTO.setMemName(member.getMemName());
			
			adminMemberManagementSelectDTO.setMemEmail(member.getMemEmail());
			
			adminMemberManagementSelectDTO.setMemStatus(member.getMemStatus());
			
			adminMemberManagementSelectDTO.setMemCreatedAt(member.getMemCreatedAt());
			
			adminMemberManagementSelectDTO.setMemImage(member.getMemImage());
			
			adminMemberManagementSelectDTOList.add(adminMemberManagementSelectDTO);
			
		}
		
		return adminMemberManagementSelectDTOList;
		
	}

}

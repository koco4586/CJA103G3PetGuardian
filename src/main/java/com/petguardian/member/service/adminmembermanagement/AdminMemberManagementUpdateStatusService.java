package com.petguardian.member.service.adminmembermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.member.dto.AdminMemberManagementUpdateStatusDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.adminmembermanagement.AdminMemberManagementUpdateStatusRepository;

@Service
public class AdminMemberManagementUpdateStatusService {

	@Autowired
	private AdminMemberManagementUpdateStatusRepository adminMemberManagementUpdateStatusRepository;

	public String updatestatus(AdminMemberManagementUpdateStatusDTO adminMemberManagementUpdateStatusDTO) {

		Integer memId = adminMemberManagementUpdateStatusDTO.getMemId();

		Member member = adminMemberManagementUpdateStatusRepository.findById(memId).orElse(null);

		Integer memStatus = member.getMemStatus();

		if (memStatus == 0) {
			member.setMemStatus(1);
			adminMemberManagementUpdateStatusRepository.save(member);
			return "已啟用";
		} else {
			member.setMemStatus(0);
			adminMemberManagementUpdateStatusRepository.save(member);
			return "已停用";
		}
	}

}

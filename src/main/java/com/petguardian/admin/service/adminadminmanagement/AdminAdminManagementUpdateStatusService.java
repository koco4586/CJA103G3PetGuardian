package com.petguardian.admin.service.adminadminmanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.admin.dto.adminadminmanagement.AdminAdminManagementUpdateStatusDTO;
import com.petguardian.admin.model.Admin;
import com.petguardian.admin.repository.adminadminmanagement.AdminAdminManagementUpdateStatusRepository;

@Service
public class AdminAdminManagementUpdateStatusService {


	@Autowired
	private AdminAdminManagementUpdateStatusRepository adminAdminManagementUpdateStatusRepository;
	
	public String updatestatus(AdminAdminManagementUpdateStatusDTO adminAdminManagementUpdateStatusDTO) {
		
		Integer admId = adminAdminManagementUpdateStatusDTO.getAdmId();
		
		Admin admin = adminAdminManagementUpdateStatusRepository.findById(admId).orElse(null);
		
		Integer admStatus = admin.getAdmStatus();

		if (admStatus == 0) {
			admin.setAdmStatus(1);
			
			adminAdminManagementUpdateStatusRepository.save(admin);
			
			return "已啟用";
			
		} else {
			
			admin.setAdmStatus(0);
			
			adminAdminManagementUpdateStatusRepository.save(admin);
			
			return "已停用";
			
		}
	}

}

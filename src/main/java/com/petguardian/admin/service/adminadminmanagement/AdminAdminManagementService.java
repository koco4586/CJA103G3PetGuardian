package com.petguardian.admin.service.adminadminmanagement;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.admin.dto.adminadminmanagement.AdminAdminManagementSelectDTO;
import com.petguardian.admin.model.Admin;
import com.petguardian.admin.repository.adminadminmanagement.AdminAdminManagementRepository;

@Service
public class AdminAdminManagementService {

	@Autowired
	private AdminAdminManagementRepository adminAdminManagementRepository;
	
	public List<AdminAdminManagementSelectDTO> findAll() {

		List<Admin> adminList = adminAdminManagementRepository.findAll();

		List<AdminAdminManagementSelectDTO> adminAdminManagementSelectDTOList = new ArrayList<>();
		
		for(Admin admin : adminList) {
		
			AdminAdminManagementSelectDTO adminAdminManagementSelectDTO = new AdminAdminManagementSelectDTO();
			
			
			adminAdminManagementSelectDTO.setAdmId(admin.getAdmId());
			
			adminAdminManagementSelectDTO.setAdmAccount(admin.getAdmAccount());
			
			adminAdminManagementSelectDTO.setAdmName(admin.getAdmName());
			
			adminAdminManagementSelectDTO.setAdmEmail(admin.getAdmEmail());
			
			adminAdminManagementSelectDTO.setAdmTel(admin.getAdmTel());
			
			adminAdminManagementSelectDTO.setAdmStatus(admin.getAdmStatus());
			
			adminAdminManagementSelectDTO.setAdmCreatedAt(admin.getAdmCreatedAt());
			
			adminAdminManagementSelectDTO.setAdmImage(admin.getAdmImage());
			
			
			adminAdminManagementSelectDTOList.add(adminAdminManagementSelectDTO);
			
		}
		
		return adminAdminManagementSelectDTOList;
		
	}

}

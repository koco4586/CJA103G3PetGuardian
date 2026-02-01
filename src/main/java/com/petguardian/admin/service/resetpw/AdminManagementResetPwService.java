package com.petguardian.admin.service.resetpw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.petguardian.admin.dto.resetpw.AdminManagementResetPwDTO;
import com.petguardian.admin.model.Admin;
import com.petguardian.admin.repository.resetpw.AdminManagementResetPwRepository;

@Service
public class AdminManagementResetPwService {

	@Autowired
	private AdminManagementResetPwRepository adminManagementResetPwRepository;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	public String adminresetpw(AdminManagementResetPwDTO adminManagementResetPwDTO, Integer admId) {
		
		String admPassword = adminManagementResetPwDTO.getAdmPassword();
		
		String admPasswordCheck = adminManagementResetPwDTO.getAdmPasswordCheck();
		
		if(!admPassword.equals(admPasswordCheck)) {
			return "密碼輸入不一致，請再次確認是否輸入正確。";
		}
		
		Admin admin = adminManagementResetPwRepository.findById(admId).orElse(null);
		
		String updatePw = passwordEncoder.encode(admPassword);
		
		admin.setAdmPassword(updatePw);
		
		adminManagementResetPwRepository.save(admin);
		
		return "密碼變更成功";
		
	}
	
}

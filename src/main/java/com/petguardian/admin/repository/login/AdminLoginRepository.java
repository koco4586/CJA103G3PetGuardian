package com.petguardian.admin.repository.login;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petguardian.admin.model.Admin;

public interface AdminLoginRepository extends JpaRepository<Admin,Integer>{

	public Admin findByAdmAccount(String admAccount);
	
}
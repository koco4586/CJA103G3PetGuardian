package com.petguardian.admin.repository.resetpw;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petguardian.admin.model.Admin;

public interface AdminManagementResetPwRepository extends JpaRepository<Admin, Integer> {

}

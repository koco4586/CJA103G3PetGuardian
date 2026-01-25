package com.petguardian.admin.repository.insert;

import com.petguardian.admin.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminInsertRepository extends JpaRepository<Admin,Integer> {

}

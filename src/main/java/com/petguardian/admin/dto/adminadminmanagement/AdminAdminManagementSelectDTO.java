package com.petguardian.admin.dto.adminadminmanagement;

import java.time.LocalDateTime;

public class AdminAdminManagementSelectDTO {

	private Integer admId;
	
	private String admAccount;
	
	private String admName;
	
	private String admEmail;
	
	private String admTel;
	
	private Integer admStatus;
	
	private LocalDateTime admCreatedAt; 
	
	private String admImage;

	public Integer getAdmId() {
		return admId;
	}

	public void setAdmId(Integer admId) {
		this.admId = admId;
	}

	public String getAdmAccount() {
		return admAccount;
	}

	public void setAdmAccount(String admAccount) {
		this.admAccount = admAccount;
	}

	public String getAdmName() {
		return admName;
	}

	public void setAdmName(String admName) {
		this.admName = admName;
	}

	public String getAdmEmail() {
		return admEmail;
	}

	public void setAdmEmail(String admEmail) {
		this.admEmail = admEmail;
	}

	public String getAdmTel() {
		return admTel;
	}

	public void setAdmTel(String admTel) {
		this.admTel = admTel;
	}

	public Integer getAdmStatus() {
		return admStatus;
	}

	public void setAdmStatus(Integer admStatus) {
		this.admStatus = admStatus;
	}

	public LocalDateTime getAdmCreatedAt() {
		return admCreatedAt;
	}

	public void setAdmCreatedAt(LocalDateTime admCreatedAt) {
		this.admCreatedAt = admCreatedAt;
	}

	public String getAdmImage() {
		return admImage;
	}

	public void setAdmImage(String admImage) {
		this.admImage = admImage;
	}

	
	
}

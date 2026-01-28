package com.petguardian.member.dto;

import java.time.LocalDateTime;

public class AdminMemberManagementSelectDTO {

	private Integer memId;
	
	private String memName;
	
	private String memEmail;
	
	private Integer memStatus;
	
	private LocalDateTime memCreatedAt;
	
	private String memImage;

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public String getMemName() {
		return memName;
	}

	public void setMemName(String memName) {
		this.memName = memName;
	}

	public String getMemEmail() {
		return memEmail;
	}

	public void setMemEmail(String memEmail) {
		this.memEmail = memEmail;
	}

	public Integer getMemStatus() {
		return memStatus;
	}

	public void setMemStatus(Integer memStatus) {
		this.memStatus = memStatus;
	}

	public LocalDateTime getMemCreatedAt() {
		return memCreatedAt;
	}

	public void setMemCreatedAt(LocalDateTime memCreatedAt) {
		this.memCreatedAt = memCreatedAt;
	}

	public String getMemImage() {
		return memImage;
	}

	public void setMemImage(String memImage) {
		this.memImage = memImage;
	}
	
	
	
}

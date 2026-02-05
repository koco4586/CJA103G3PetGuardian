package com.petguardian.booking.model;

import com.petguardian.sitter.model.SitterVO;

public class BookingDisplayDTO {
	private SitterVO sitter; // 包裝別人的保母資料
	private boolean isFavorited; // 收藏狀態邏輯
	private String servicesJson; // 服務列表
	private String memImage;// 會員頭像路徑

	public BookingDisplayDTO(SitterVO sitter, boolean isFavorited) {
		this.sitter = sitter;
		this.isFavorited = isFavorited;
	}

	// Getter & Setter
	public SitterVO getSitter() {
		return sitter;
	}

	public void setSitter(SitterVO sitter) {
		this.sitter = sitter;
	}

	// 注意：Thymeleaf 會根據 isIsFavorited 或是 getFavorited 來抓值
	public boolean isIsFavorited() {
		return isFavorited;
	}

	public void setIsFavorited(boolean isFavorited) {
		this.isFavorited = isFavorited;
	}

	public String getServicesJson() {
		return servicesJson;
	}

	public void setServicesJson(String servicesJson) {
		this.servicesJson = servicesJson;
	}

	public String getMemImage() {
		return memImage;
	}

	public void setMemImage(String memImage) {
		this.memImage = memImage;
	}
}
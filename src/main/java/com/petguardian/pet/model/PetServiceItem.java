package com.petguardian.pet.model;

import jakarta.persistence.*;

@Entity
@Table(name = "SERVICE_ITEMS") // 對接你的資料庫表名
public class PetServiceItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SERVICE_ITEM_ID") // 對接你的 PK
	private Integer serviceItemId;

	@Column(name = "SERVICE_TYPE") // 對接服務項目名稱
	private String serviceType;

	@Column(name = "SERVICE_STATUS") // 對應資料庫狀態 (0:下架, 1:上架)
	private Integer serviceStatus;
	@Column(name = "SERVICE_PRICE") // 對應服務價格
	private Integer servicePrice;
	@Column(name = "SERVICE_DESC") // 對應服務描述
	private String serviceDesc;

	// 根據您的 SQL，只保留 ID 和 Type

	// 必須與類別名稱完全一致的建構子
	public PetServiceItem() {
	}

	// Getter & Setter
	public Integer getServiceItemId() {
		return serviceItemId;
	}

	public void setServiceItemId(Integer serviceItemId) {
		this.serviceItemId = serviceItemId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public Integer getServiceStatus() {
		return serviceStatus;
	}

	public void setServiceStatus(Integer serviceStatus) {
		this.serviceStatus = serviceStatus;
	}

	public Integer getServicePrice() {
		return servicePrice;
	}

	public void setServicePrice(Integer servicePrice) {
		this.servicePrice = servicePrice;
	}

	public String getServiceDesc() {
		return serviceDesc;
	}

	public void setServiceDesc(String serviceDesc) {
		this.serviceDesc = serviceDesc;
	}
}

package com.petguardian.petsitter.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity //保姆服務對象table
@Table(name = "pet_sitter_service_pet_type")
public class PetSitterServicePetTypeVO implements Serializable {
	private static final long serialVersionUID = 1L;

	public PetSitterServicePetTypeVO() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "service_pet_id", updatable = false, nullable = false)
	private Integer servicePetId; // 服務寵物對象編號 int

	// 1. 關聯到服務項目表 (service_items)
	@Column(name = "service_item_id", nullable = false)
	private Integer  serviceItemId;// 寵物服務項目編號 int

	@Column(name = "size_id", nullable = false)
	private Integer sizeId; // 寵物體型編號 int

	@Column(name = "type_id", nullable = false)
	private Integer typeId; // 寵物種類編號 int

	public Integer getServicePetId() {
		return servicePetId;
	}

	public void setServicePetId(Integer servicePetId) {
		this.servicePetId = servicePetId;
	}

	public Integer getServiceItemId() {
		return serviceItemId;
	}

	public void setServiceItemId(Integer serviceItemId) {
		this.serviceItemId = serviceItemId;
	}

	public Integer getSizeId() {
		return sizeId;
	}

	public void setSizeId(Integer sizeId) {
		this.sizeId = sizeId;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	

}

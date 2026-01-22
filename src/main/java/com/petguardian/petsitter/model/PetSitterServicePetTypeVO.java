package com.petguardian.petsitter.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 保姆服務對象 Entity
 * 對應資料表: pet_sitter_service_pet_type
 * 
 * 定義保姆的每個服務項目適用於哪些寵物種類與體型
 * 包含 sitter_id, service_item_id, type_id, size_id 的關聯
 */
@Entity
@Table(name = "pet_sitter_service_pet_type")
public class PetSitterServicePetTypeVO implements Serializable {
	private static final long serialVersionUID = 1L;

	public PetSitterServicePetTypeVO() {

	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "service_pet_id", updatable = false, nullable = false)
	private Integer servicePetId; // 服務寵物對象編號

	/**
	 * 保姆編號 (FK → sitter.sitter_id，NOT NULL)
	 * 採用低耦合設計,不使用 @ManyToOne 關聯
	 */
	@Column(name = "sitter_id", nullable = false)
	private Integer sitterId; // 保姆編號

	/** 寵物服務項目編號 (FK → service_items.service_item_id，NOT NULL) */
	@Column(name = "service_item_id", nullable = false)
	private Integer serviceItemId; // 寵物服務項目編號

	/** 寵物體型編號 (FK → pet_size.size_id，NOT NULL) */
	@Column(name = "size_id", nullable = false)
	private Integer sizeId; // 寵物體型編號

	/** 寵物種類編號 (FK → pet_type.type_id，NOT NULL) */
	@Column(name = "type_id", nullable = false)
	private Integer typeId; // 寵物種類編號

	// ===== Getter/Setter =====

	public Integer getServicePetId() {
		return servicePetId;
	}

	public void setServicePetId(Integer servicePetId) {
		this.servicePetId = servicePetId;
	}

	public Integer getSitterId() {
		return sitterId;
	}

	public void setSitterId(Integer sitterId) {
		this.sitterId = sitterId;
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

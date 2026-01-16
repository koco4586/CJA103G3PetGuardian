package com.sitter.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pet_sitter_service_pet_type")
public class PetSitterServicePetTypeVO implements Serializable {
	private static final long serialVersionUID = 1L;

	public PetSitterServicePetTypeVO() {

	}

	@Id
	@Column(name = "service_pet_id", updatable = false, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer servicePetId; // 服務寵物對象編號 int

	// 1. 關聯到服務項目表 (service_items)
	@ManyToOne
	@JoinColumn(name = "service_item_id")
	private ServiceItems  serviceItemId;// 寵物服務項目編號 int

	@ManyToOne
	@JoinColumn(name = "size_id")
	private PetSizeVO sizeId; // 寵物體型編號 int

	@ManyToOne
	@JoinColumn(name = "type_id")
	private PetTypeVO typeId; // 寵物種類編號 int

	public Integer getServicePetId() {
		return servicePetId;
	}

	public void setServicePetId(Integer servicePetId) {
		this.servicePetId = servicePetId;
	}

	public ServiceItems  getServiceItemId() {
		return serviceItemId;
	}

	public void setServiceItemId(ServiceItems serviceItemId) {
		this.serviceItemId = serviceItemId;
	}

	public PetSizeVO getSizeId() {
		return sizeId;
	}

	public void setSizeId(PetSizeVO sizeId) {
		this.sizeId = sizeId;
	}

	public PetTypeVO getTypeId() {
		return typeId;
	}

	public void setTypeId(PetTypeVO typeId) {
		this.typeId = typeId;
	}

}

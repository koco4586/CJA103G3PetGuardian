package com.sitter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="pet_size")
public class PetSizeVO {
	public PetSizeVO() {};
	
	@Id
	@Column(name = "size_id", nullable = false, updatable = false)
	private Integer sizeId;	//體型編號	int
	
	@Column(name = "size_name")
	private String sizeName;	//體型名稱	varchar

	public Integer getSizeId() {
		return sizeId;
	}

	public void setSizeId(Integer sizeId) {
		this.sizeId = sizeId;
	}

	public String getSizeName() {
		return sizeName;
	}

	public void setSizeName(String sizeName) {
		this.sizeName = sizeName;
	}
	
	
}

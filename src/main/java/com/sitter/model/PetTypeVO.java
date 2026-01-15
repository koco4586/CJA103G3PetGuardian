package com.sitter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="pet_type")
public class PetTypeVO {
	
	public PetTypeVO() {};

	
	@Id
	@Column(name = "type_id", nullable = false, updatable = false)
	private Integer TypeId;	//種類編號	int
	
	@Column(name = "type_name")
	private String TypeName;	//種類名稱	varchar

	public Integer getTypeId() {
		return TypeId;
	}

	public void setTypeId(Integer typeId) {
		TypeId = typeId;
	}

	public String getTypeName() {
		return TypeName;
	}

	public void setTypeName(String typeName) {
		TypeName = typeName;
	}

	
}

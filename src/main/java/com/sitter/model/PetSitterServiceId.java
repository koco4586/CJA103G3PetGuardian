package com.sitter.model;

import java.io.Serializable;
import java.util.Objects;

//設立複合式主鍵避免重複
public class PetSitterServiceId implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public PetSitterServiceId() {

	}

	private Integer serviceItem;
    private Integer sitter;

	@Override
	public int hashCode() {
		return Objects.hash(serviceItem, sitter);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PetSitterServiceId other = (PetSitterServiceId) obj;
		return Objects.equals(serviceItem, other.serviceItem) && Objects.equals(sitter, other.sitter);
	}
	public Integer getServiceItem() {
		return serviceItem;
	}
	public void setServiceItem(Integer serviceItem) {
		this.serviceItem = serviceItem;
	}
	public Integer getSitter() {
		return sitter;
	}
	public void setSitter(Integer sitter) {
		this.sitter = sitter;
	}
	
	
}

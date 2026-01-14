package com.sitter.model;

import java.util.Objects;
//設立複合式主鍵避免重複
public class PetSitterServiceId {
	public PetSitterServiceId() {
		super();
	}
	
	private Integer serviceItemId;
	private Integer sitterId;
	@Override
	public int hashCode() {
		return Objects.hash(serviceItemId, sitterId);
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
		return Objects.equals(serviceItemId, other.serviceItemId) && Objects.equals(sitterId, other.sitterId);
	}
	
	
}

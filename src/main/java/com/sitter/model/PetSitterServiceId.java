package com.sitter.model;

import java.util.Objects;

//設立複合式主鍵避免重複
public class PetSitterServiceId {
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

}

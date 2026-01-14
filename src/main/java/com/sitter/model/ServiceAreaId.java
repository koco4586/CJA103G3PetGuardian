package com.sitter.model;

import java.util.Objects;

public class ServiceAreaId {
	// 設立複合式主鍵避免重複

	public ServiceAreaId() {

	};

	private Integer sitter;
	private Integer area;

	@Override
	public int hashCode() {
		return Objects.hash(area, sitter);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceAreaId other = (ServiceAreaId) obj;
		return Objects.equals(area, other.area) && Objects.equals(sitter, other.sitter);
	}

}

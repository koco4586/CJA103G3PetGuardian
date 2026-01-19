package com.petguardian.service.model;

import java.io.Serializable;
import java.util.Objects;
//放保姆和地區編號的主鍵
public class ServiceAreaId implements Serializable{
	// 設立複合式主鍵避免重複
    private static final long serialVersionUID = 1L;
    private Integer sitter;
    private Integer area;

	public ServiceAreaId() {

	};
	
	  public ServiceAreaId(Integer sitter, Integer area) {
	        this.sitter = sitter;
	        this.area = area;
	    }



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

	public Integer getSitter() {
		return sitter;
	}

	public void setSitter(Integer sitter) {
		this.sitter = sitter;
	}

	public Integer getArea() {
		return area;
	}

	public void setArea(Integer area) {
		this.area = area;
	}
	
	

}

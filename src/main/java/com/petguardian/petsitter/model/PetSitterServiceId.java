package com.petguardian.petsitter.model;

import java.io.Serializable;
import java.util.Objects;

//保姆服務資訊(寵物服務項目編號和保姆編號的主鍵)
//設立複合式主鍵避免重複
public class PetSitterServiceId implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private Integer serviceItemId;
    private Integer sitter;

    public PetSitterServiceId() {}

    public PetSitterServiceId(Integer serviceItemId, Integer sitterId) {
        this.serviceItemId = serviceItemId;
        this.sitter = sitterId;
    }

    public Integer getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(Integer serviceItemId) { this.serviceItemId = serviceItemId; }

    public Integer getSitter() { return sitter; }
    public void setSitter(Integer sitter) { this.sitter = sitter; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PetSitterServiceId)) return false;
        PetSitterServiceId that = (PetSitterServiceId) o;
        return Objects.equals(serviceItemId, that.serviceItemId) &&
               Objects.equals(sitter, that.sitter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceItemId, sitter);
    }
}

package com.sitter.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_items")
public class ServiceItems implements Serializable {
	private static final long serialVersionUID = 1L;

	public ServiceItems() {

	}

	@Id
	@Column(name = "service_item_id", updatable = false, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer serviceItemId;

	@Column(name = "service_type")
	private String serviceType;

	public Integer getServiceItemId() {
		return serviceItemId;
	}

	public void setServiceItemId(Integer serviceItemId) {
		this.serviceItemId = serviceItemId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

}

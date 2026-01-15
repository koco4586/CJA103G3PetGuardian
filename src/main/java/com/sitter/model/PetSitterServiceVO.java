package com.sitter.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "pet_sitter_service")
@IdClass(PetSitterServiceId.class)
public class PetSitterServiceVO implements Serializable {
	private static final long serialVersionUID = 1L;

	public PetSitterServiceVO() {

	}

	@Id
	@ManyToOne
	@JoinColumn(name = "service_item_id", nullable = false)
	private ServiceItems serviceItem;

	@Id
	@ManyToOne
	@JoinColumn(name = "sitter_id", nullable = false)
	private SitterVO sitter;

	@Column(name = "default_price")
	@NotNull(message = "規範價格: 請勿空白")
	@Min(value = (400), message = "規範價格: 不能小於{value}")
	@Max(value = (1000), message = "規範價格: 不能超過{value}")
	private Integer defaultPrice; // 規範價格 int

	@Column(name = "created_at")
	private LocalDateTime createdAt; // 建立時間 datetime

	@Column(name = "updated_at")
	private LocalDateTime updatedAt; // 最後更新時間 datetime

	

	public ServiceItems getServiceItem() {
		return serviceItem;
	}

	public void setServiceItem(ServiceItems serviceItem) {
		this.serviceItem = serviceItem;
	}


	public SitterVO getSitter() {
		return sitter;
	}

	public void setSitter(SitterVO sitter) {
		this.sitter = sitter;
	}

	public Integer getDefaultPrice() {
		return defaultPrice;
	}

	public void setDefaultPrice(Integer defaultPrice) {
		this.defaultPrice = defaultPrice;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

}

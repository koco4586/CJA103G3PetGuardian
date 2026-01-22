package com.petguardian.petsitter.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.petguardian.sitter.model.SitterVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 保姆服務項目 Entity
 * 對應資料表: pet_sitter_service
 * 
 * 定義每位保姆提供的服務項目及其定價
 * 複合主鍵: SitterVO (sitter_id) + serviceItemId
 */
@Entity
@Table(name = "pet_sitter_service")
@IdClass(PetSitterServiceId.class)
public class PetSitterServiceVO implements Serializable {
	private static final long serialVersionUID = 1L;

	public PetSitterServiceVO() {

	}

	// 服務項目編號PK_fk
	@Id
	@Column(name = "service_item_id", nullable = false)
	private Integer serviceItemId;

	/** 保姆編號pk+fk */
	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sitter_id", nullable = false, updatable = false)
	private SitterVO sitter;

	/** 規範價格 */
	@Column(name = "default_price")
	@NotNull(message = "規範價格: 請勿空白")
	@Min(value = 400, message = "規範價格: 不能小於{value}")
	@Max(value = 1000, message = "規範價格: 不能超過{value}")
	private Integer defaultPrice;

	/** 建立時間（DB default CURRENT_TIMESTAMP） */
	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private LocalDateTime createdAt;

	/** 最後更新時間（DB ON UPDATE） */
	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private LocalDateTime updatedAt;

	// ===== Getter/Setter =====
	public Integer getServiceItemId() {
		return serviceItemId;
	}

	public void setServiceItemId(Integer serviceItemId) {
		this.serviceItemId = serviceItemId;
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

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
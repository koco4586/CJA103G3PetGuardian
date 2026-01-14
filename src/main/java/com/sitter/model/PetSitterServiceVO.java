package com.sitter.model;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pet_sitter_service")
@IdClass(PetSitterServiceId.class)
public class PetSitterServiceVO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public PetSitterServiceVO() {
		
	}
	
	@Id // @Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
	@ManyToOne
    @JoinColumn(name = "service_item_id") // 對應 DB 的 service_item_id
	private ServiceItems serviceItems;    // 直接關聯服務項目物件	int
	
	@Id
    @ManyToOne
    @JoinColumn(name = "sitter_id")       // 對應 DB 的 sitter_id
    private SitterVO sitter;              // 直接關聯保姆物件
	
	@Column(name = "default_price")
	private Integer defaultPrice;	//規範價格	int
	
	@Column(name = "created_at")
	private Instant createdAt;	//建立時間	datetime
	
	@Column(name = "updated_at")
	private Instant updatedAt;	//最後更新時間	datetime
	
	public ServiceItems getServiceItem() {
		return serviceItems;
	}
	public void setServiceItem(ServiceItems serviceItems) {
		this.serviceItems = serviceItems;
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
	public Instant getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
	public Instant getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	
}

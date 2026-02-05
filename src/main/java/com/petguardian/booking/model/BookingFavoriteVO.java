package com.petguardian.booking.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "pet_sitter_favorites") // 建議資料庫表名
@IdClass(BookingFavoriteId.class)
public class BookingFavoriteVO implements Serializable {

	@Id
	@Column(name = "mem_id")
	private Integer memId;

	@Id
	@Column(name = "sitter_id")
	private Integer sitterId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt; // 抓取收藏時間

	@Transient // 不儲存到資料庫
	private String sitterName;

	@Transient
	private Integer basePrice;

	@Transient
	private Double avgRating; // 平均評分

	@Transient
	private Integer ratingCount; // 評分次數

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Integer getSitterId() {
		return sitterId;
	}

	public void setSitterId(Integer sitterId) {
		this.sitterId = sitterId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getSitterName() {
		return sitterName;
	}

	public void setSitterName(String sitterName) {
		this.sitterName = sitterName;
	}

	public Integer getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(Integer basePrice) {
		this.basePrice = basePrice;
	}

	public Double getAvgRating() {
		return avgRating;
	}

	public void setAvgRating(Double avgRating) {
		this.avgRating = avgRating;
	}

	public Integer getRatingCount() {
		return ratingCount;
	}

	public void setRatingCount(Integer ratingCount) {
		this.ratingCount = ratingCount;
	}
}
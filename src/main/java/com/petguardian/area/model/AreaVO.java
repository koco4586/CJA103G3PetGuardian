package com.petguardian.area.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/*
 * 註1: classpath必須有jakarta.persistence-api jar
 * 註2: Annotation可以添加在屬性上，也可以添加在getXxx()方法之上
 */

/**
 * 地區 Entity
 * 對應資料表: area
 */
@Entity
@Table(name = "area")
public class AreaVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer areaId; // 地區編號
	private String cityName; // 縣市
	private String district; // 行政區

	public AreaVO() {
		// 必需有一個不傳參數建構子 (JavaBean基本知識)
	}

	@Id
	@Column(name = "area_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getAreaId() {
		return this.areaId;
	}

	public void setAreaId(Integer areaId) {
		this.areaId = areaId;
	}

	@Column(name = "city_name")
	@NotEmpty(message = "縣市名稱: 請勿空白")
	@Size(min = 2, max = 50, message = "縣市名稱: 長度必需在{min}到{max}之間")
	public String getCityName() {
		return this.cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	@Column(name = "district")
	@NotEmpty(message = "行政區: 請勿空白")
	@Size(min = 2, max = 50, message = "行政區: 長度必需在{min}到{max}之間")
	public String getDistrict() {
		return this.district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

}

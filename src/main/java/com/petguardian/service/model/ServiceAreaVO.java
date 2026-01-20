package com.petguardian.service.model;

import java.io.Serializable;

import com.petguardian.area.model.AreaVO;
import com.petguardian.sitter.model.SitterVO;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity//保姆服務地區table
@Table(name = "service_area")
@IdClass(ServiceAreaId.class)
public class ServiceAreaVO implements Serializable {
	private static final long serialVersionUID = 1L;

	public ServiceAreaVO() {

	};

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sitter_id", nullable = false, updatable = false)
	private SitterVO sitter; // 保姆編號

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "area_id", nullable = false, updatable = false)
	private AreaVO area;// 地區編號

	public SitterVO getSitter() {
		return sitter;
	}

	public void setSitter(SitterVO sitter) {
		this.sitter = sitter;
	}

	public AreaVO getArea() {
		return area;
	}

	public void setArea(AreaVO area) {
		this.area = area;
	}

}

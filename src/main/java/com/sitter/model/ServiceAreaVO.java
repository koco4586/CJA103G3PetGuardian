//package com.sitter.model;
//
//import java.io.Serializable;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.IdClass;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.Table;
//
//@Entity
//@Table(name = "service_area")
//@IdClass(ServiceAreaId.class)
//public class ServiceAreaVO implements Serializable {
//	private static final long serialVersionUID = 1L;
//
//	public ServiceAreaVO() {
//
//	};
//
//	@Id
//	@ManyToOne
//	@JoinColumn(name = "sitter_id")
//	private SitterVO sitter; // 保姆編號
//
//	@Id
//	@ManyToOne
//	@JoinColumn(name = "area_Id")
//	private AreaVO area;// 地區編號
//
//	public SitterVO getSitter() {
//		return sitter;
//	}
//
//	public void setSitter(SitterVO sitter) {
//		this.sitter = sitter;
//	}
//
//	public AreaVO getArea() {
//		return area;
//	}
//
//	public void setArea(AreaVO area) {
//		this.area = area;
//	}
//
//}

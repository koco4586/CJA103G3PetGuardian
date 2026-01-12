package com.sitter.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class sitterVO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public sitterVO() { //必需有一個不傳參數建構子(JavaBean基本知識)
		super();
	};
	
	@Id //@Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
	@Column(name = "sitterId")
	private Integer sitterId; //保姆編號
	
	@OneToOne
	@Column(name ="memId", unique = true)  //會員編號
	private Integer memId;
	
	@Column(name="sitterName")
	private String sitterName; //保姆姓名
	
	@Column(name="sitterAdd")
	private String sitterAdd;  //服務地址
	
	@Column(name="defaultPrice")
	private Integer defaultPrice; //規範價格
	
	@Column(name="sitterCreatedAt")
	private LocalDateTime sitterCreatedAt; //註冊保姆時間
	
	@Column(name="sitterStatus")
	private Byte sitterStatus; //保姆狀態
	
	@Column(name="serviceTime")
	private String serviceTime; //服務時間
	
	@Column(name="sitterRatingCount")
	private Integer sitterRatingCount; //保姆總評價數
	
	@Column(name="sitterStarCount")
	private Integer sitterStarCount; //保姆總星星數

	
	public Integer getSitterId() {
		return sitterId;
	}
	public void setSitterId(Integer sitterId) {
		this.sitterId = sitterId;
	}
	public Integer getMemId() {
		return memId;
	}
	public void setMemId(Integer memId) {
		this.memId = memId;
	}
	public String getSitterName() {
		return sitterName;
	}
	public void setSitterName(String sitterName) {
		this.sitterName = sitterName;
	}
	public String getSitterAdd() {
		return sitterAdd;
	}
	public void setSitterAdd(String sitterAdd) {
		this.sitterAdd = sitterAdd;
	}
	public Integer getDefaultPrice() {
		return defaultPrice;
	}
	public void setDefaultPrice(Integer defaultPrice) {
		this.defaultPrice = defaultPrice;
	}
	public LocalDateTime getSitterCreatedAt() {
		return sitterCreatedAt;
	}
	
	public Byte getSitterStatus() {
		return sitterStatus;
	}
	public void setSitterStatus(Byte sitterStatus) {
		this.sitterStatus = sitterStatus;
	}
	public String getServiceTime() {
		return serviceTime;
	}
	public void setServiceTime(String serviceTime) {
		this.serviceTime = serviceTime;
	}
	public Integer getSitterRatingCount() {
		return sitterRatingCount;
	}
	public void setSitterRatingCount(Integer sitterRatingCount) {
		this.sitterRatingCount = sitterRatingCount;
	}
	public Integer getSitterStarCount() {
		return sitterStarCount;
	}
	public void setSitterStarCount(Integer sitterStarCount) {
		this.sitterStarCount = sitterStarCount;
	}
	
	
	
}

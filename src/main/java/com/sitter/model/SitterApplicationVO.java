package com.sitter.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sitter_application")
public class SitterApplicationVO implements Serializable {
	private static final long serialVersionUID = 1L;

	public SitterApplicationVO() { // 必需有一個不傳參數建構子(JavaBean基本知識)

	};

	@Id // @Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
	@Column(name = "app_Id", updatable = false, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer appId; // 申請編號

//	@OneToOne //等MemVO建立完成在import
//	@JoinColumn(name = "mem_id", unique = true) // 會員編號
//	private MemberVO memId;

	// 暫時用這個當欄位
	@Column(name = "mem_id", unique = true)
	private Integer memId;

	@Column(name = "app_Intro")
	private String appIntro; // 個人簡介

	@Column(name = "app_experience")
	private String appExperience; // 相關經驗

	@Column(name = "app_Status")
	private Byte appStatus; // 申請狀態

	@Column(name = "app_review_at")
	private LocalDateTime appReviewAt;// 審核時間

	@Column(name = "app_review_rote")
	private String appReviewNote; // 審核意見

	@Column(name = "app_created_at")
	private Instant appCreatedAt; // 申請時間

	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public String getAppIntro() {
		return appIntro;
	}

	public void setAppIntro(String appIntro) {
		this.appIntro = appIntro;
	}

	public String getAppExperience() {
		return appExperience;
	}

	public void setAppExperience(String appExperience) {
		this.appExperience = appExperience;
	}

	public Byte getAppStatus() {
		return appStatus;
	}

	public void setAppStatus(Byte appStatus) {
		this.appStatus = appStatus;
	}

	public LocalDateTime getAppReviewAt() {
		return appReviewAt;
	}

	public void setAppReviewAt(LocalDateTime appReviewAt) {
		this.appReviewAt = appReviewAt;
	}

	public String getAppReviewNote() {
		return appReviewNote;
	}

	public void setAppReviewNote(String appReviewNote) {
		this.appReviewNote = appReviewNote;
	}

	public Instant getAppCreatedAt() {
		return appCreatedAt;
	}

	public void setAppCreatedAt(Instant appCreatedAt) {
		this.appCreatedAt = appCreatedAt;
	}

}

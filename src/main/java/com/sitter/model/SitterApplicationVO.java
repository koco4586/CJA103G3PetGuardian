package com.sitter.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.sitter.model.MemberVO; 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


//保姆申請
@Entity
@Table(name = "sitter_application")
public class SitterApplicationVO implements Serializable {
	private static final long serialVersionUID = 1L;

	public SitterApplicationVO() { // 必需有一個不傳參數建構子(JavaBean基本知識)

	};

	@Id // @Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
	@Column(name = "app_id", updatable = false, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer appId; // 申請編號

	@ManyToOne
	@JoinColumn(name = "mem_id") // 會員編號
	private MemberVO member;

	@Column(name = "app_intro")
	private String appIntro; // 個人簡介

	@Column(name = "app_experience")
	private String appExperience; // 相關經驗

	@Column(name = "app_status", insertable = false)
	private Byte appStatus; // 申請狀態

	@Column(name = "app_review_at")
	private LocalDateTime  appReviewAt;// 審核時間

	@Column(name = "app_review_note")
	private String appReviewNote; // 審核意見
	
	@CreationTimestamp
	@Column(name = "app_created_at", updatable = false)
	private LocalDateTime  appCreatedAt; // 申請時間

	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	public MemberVO getMember() {
		return member;
	}

	public void setMember(MemberVO member) {
		this.member = member;
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

	public LocalDateTime getAppCreatedAt() {
		return appCreatedAt;
	}

	public void setAppCreatedAt(LocalDateTime appCreatedAt) {
		this.appCreatedAt = appCreatedAt;
	}

}

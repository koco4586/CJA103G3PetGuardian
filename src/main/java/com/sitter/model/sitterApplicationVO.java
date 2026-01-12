package com.sitter.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class sitterApplicationVO {
	private static final long serialVersionUID = 1L;
	
	public sitterApplicationVO() { //必需有一個不傳參數建構子(JavaBean基本知識)
		super();
	};
	
	@Id //@Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
	@Column(name = "appId")
	@GeneratedValue(strategy = GenerationType.IDENTITY) //@GeneratedValue的generator屬性指定要用哪個generator //【strategy的GenerationType, 有四種值: AUTO, IDENTITY, SEQUENCE, TABLE】 
	private Integer appId; //申請編號
	
	@OneToOne
	@JoinColumn(name ="memId", unique = true)  //會員編號
	private Integer memId; //會員編號
	
	@Column(name = "appIntro")
	private String appIntro; //個人簡介
	
	@Column(name = "AppExperience")
	private String AppExperience; //相關經驗
	
	@Column(name = "AppStatus")
	private Byte AppStatus; //申請狀態
	
	@Column(name = "AppReviewAt")
	private LocalDateTime AppReviewAt;//審核時間
	
	@Column(name = "AppReviewNote")
	private String AppReviewNote; //審核意見
	
	@Column(name = "AppCreatedAt")
	private LocalDateTime AppCreatedAt; //申請時間

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
		return AppExperience;
	}

	public void setAppExperience(String appExperience) {
		AppExperience = appExperience;
	}

	public Byte getAppStatus() {
		return AppStatus;
	}

	public void setAppStatus(Byte appStatus) {
		AppStatus = appStatus;
	}

	public LocalDateTime getAppReviewAt() {
		return AppReviewAt;
	}

	public void setAppReviewAt(LocalDateTime appReviewAt) {
		AppReviewAt = appReviewAt;
	}

	public String getAppReviewNote() {
		return AppReviewNote;
	}

	public void setAppReviewNote(String appReviewNote) {
		AppReviewNote = appReviewNote;
	}

	public LocalDateTime getAppCreatedAt() {
		return AppCreatedAt;
	}

	public void setAppCreatedAt(LocalDateTime appCreatedAt) {
		AppCreatedAt = appCreatedAt;
	}

	

}

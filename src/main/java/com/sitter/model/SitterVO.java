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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "sitter")
public class SitterVO implements Serializable {
	private static final long serialVersionUID = 1L;

	public SitterVO() { // 必需有一個不傳參數建構子(JavaBean基本知識)

	};

	@Id // @Id代表這個屬性是這個Entity的唯一識別屬性，並且對映到Table的主鍵
	@Column(name = "sitter_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY) // @GeneratedValue的generator屬性指定要用哪個generator// TABLE】
	private Integer sitterId; // 保姆編號

	@OneToOne 
	@JoinColumn(name = "mem_id", unique = true) // 會員編號
	private MemberVO member;

	@Column(name = "sitter_name")
	@NotEmpty(message = "服務地址不能空白")
	@Size(min = 2, max = 5, message = "保姆姓名長度必須介於2到5個字")
	@Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s]+$", message = "保姆姓名只能包含中文、英文、數字或空格")
	private String sitterName; // 保姆姓名

	@Column(name = "sitter_add")
	@NotEmpty(message = "服務地址不能空白")
	@Size(min = 5, max = 50, message = "服務地址長度必須介於5到100字元")
	@Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s]+$", message = "服務地址只能包含中文、英文、數字或空格")
	private String sitterAdd; // 服務地址

	@CreationTimestamp
	@Column(name = "sitter_created_at")
	private LocalDateTime sitterCreatedAt; // 註冊保姆時間

	@Column(name = "sitter_status")
	private Byte sitterStatus; // 保姆狀態

	@Column(name = "service_time")
	private String serviceTime; // 服務時間

	@Column(name = "sitter_rating_count")
	private Integer sitterRatingCount; // 保姆總評價數

	@Column(name = "sitter_star_count")
	private Integer sitterStarCount; // 保姆總星星數

	public Integer getSitterId() {
		return sitterId;
	}

	public void setSitterId(Integer sitterId) {
		this.sitterId = sitterId;
	}

	

	public MemberVO getMember() {
		return member;
	}

	public void setMember(MemberVO member) {
		this.member = member;
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

	public LocalDateTime getSitterCreatedAt() {
		return sitterCreatedAt;
	}

	public void setSitterCreatedAt(LocalDateTime sitterCreatedAt) {
		this.sitterCreatedAt = sitterCreatedAt;
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

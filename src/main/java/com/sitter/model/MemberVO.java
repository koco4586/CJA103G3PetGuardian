package com.sitter.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "member")
public class MemberVO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	public MemberVO() {
	}

	// ==================== PK ====================
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "mem_id", updatable = false, nullable = false)
	private Integer memId; // 會員編號

	// ==================== 基本資料 ====================
	@Column(name = "mem_name", nullable = false, length = 10)
	private String memName; // 會員姓名

	@Lob
	@Basic(optional = true)
	@Column(name = "mem_image")
	private byte[] memImage; // 會員頭像

	@Column(name = "mem_uid", length = 25)
	private String memUid; // 身分證字號

	@Column(name = "mem_bth")
	private LocalDate memBth; // 會員生日

	@Column(name = "mem_sex")
	private Integer memSex; // 0:未驗證, 1:男, 2:女

	@Column(name = "mem_email", nullable = false, unique = true, length = 100)
	private String memEmail; // 電子信箱

	@Column(name = "mem_tel", length = 20)
	private String memTel; // 手機號碼

	@Column(name = "mem_add", length = 100)
	private String memAdd; // 地址

	@Column(name = "mem_account_number", length = 20)
	private String memAccountNumber; // 銀行帳號

	// ==================== 帳號安全 ====================
	@Column(name = "mem_acc", nullable = false, length = 25)
	private String memAcc; // 會員帳號

	@Column(name = "mem_pw", length = 255)
	private String memPw; // 會員密碼

	// ==================== 狀態 ====================
	@Column(name = "mem_status")
	private Integer memStatus; // 0:未驗證, 1:正常, 2:停權

	@Column(name = "mem_sitter_status")
	private Integer memSitterStatus; // 0:未開通, 1:已開通

	// ==================== 時間 ====================
	@Column(name = "mem_created_at", insertable = false, updatable = false)
	private LocalDateTime memCreatedAt; // 註冊時間

	@Column(name = "mem_updated_at", insertable = false, updatable = false)
	private LocalDateTime memUpdatedAt; // 最後修改時間

	@Column(name = "mem_last_login")
	private LocalDateTime memLastLogin; // 最後登入時間

	// ==================== 商城評價 ====================
	@Column(name = "mem_shop_rating_score")
	private Integer memShopRatingScore; // 總星星數

	@Column(name = "mem_shop_rating_count")
	private Integer memShopRatingCount; // 評價次數

	// ==================== Getter / Setter ====================
	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public String getMemName() {
		return memName;
	}

	public void setMemName(String memName) {
		this.memName = memName;
	}

	public byte[] getMemImage() {
		return memImage;
	}

	public void setMemImage(byte[] memImage) {
		this.memImage = memImage;
	}

	public String getMemUid() {
		return memUid;
	}

	public void setMemUid(String memUid) {
		this.memUid = memUid;
	}

	public LocalDate getMemBth() {
		return memBth;
	}

	public void setMemBth(LocalDate memBth) {
		this.memBth = memBth;
	}

	public Integer getMemSex() {
		return memSex;
	}

	public void setMemSex(Integer memSex) {
		this.memSex = memSex;
	}

	public String getMemEmail() {
		return memEmail;
	}

	public void setMemEmail(String memEmail) {
		this.memEmail = memEmail;
	}

	public String getMemTel() {
		return memTel;
	}

	public void setMemTel(String memTel) {
		this.memTel = memTel;
	}

	public String getMemAdd() {
		return memAdd;
	}

	public void setMemAdd(String memAdd) {
		this.memAdd = memAdd;
	}

	public String getMemAccountNumber() {
		return memAccountNumber;
	}

	public void setMemAccountNumber(String memAccountNumber) {
		this.memAccountNumber = memAccountNumber;
	}

	public String getMemAcc() {
		return memAcc;
	}

	public void setMemAcc(String memAcc) {
		this.memAcc = memAcc;
	}

	public String getMemPw() {
		return memPw;
	}

	public void setMemPw(String memPw) {
		this.memPw = memPw;
	}

	public Integer getMemStatus() {
		return memStatus;
	}

	public void setMemStatus(Integer memStatus) {
		this.memStatus = memStatus;
	}

	public Integer getMemSitterStatus() {
		return memSitterStatus;
	}

	public void setMemSitterStatus(Integer memSitterStatus) {
		this.memSitterStatus = memSitterStatus;
	}

	public LocalDateTime getMemCreatedAt() {
		return memCreatedAt;
	}

	public LocalDateTime getMemUpdatedAt() {
		return memUpdatedAt;
	}

	public LocalDateTime getMemLastLogin() {
		return memLastLogin;
	}

	public void setMemLastLogin(LocalDateTime memLastLogin) {
		this.memLastLogin = memLastLogin;
	}

	public Integer getMemShopRatingScore() {
		return memShopRatingScore;
	}

	public void setMemShopRatingScore(Integer memShopRatingScore) {
		this.memShopRatingScore = memShopRatingScore;
	}

	public Integer getMemShopRatingCount() {
		return memShopRatingCount;
	}

	public void setMemShopRatingCount(Integer memShopRatingCount) {
		this.memShopRatingCount = memShopRatingCount;
	}
}

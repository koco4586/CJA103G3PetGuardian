package com.petguardian.sitter.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.petguardian.service.model.ServiceAreaVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 保姆 Entity
 * 對應資料表: sitter
 * 
 * 包含保姆基本資料、服務狀態、評價統計與排程資訊
 */
@Entity
@Table(name = "sitter")
public class SitterVO implements Serializable {
	private static final long serialVersionUID = 1L;

	public SitterVO() { // 必需有一個不傳參數建構子(JavaBean基本知識)

	};

	// 保姆編號（PK，自動遞增）
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sitter_id", nullable = false, updatable = false)
	private Integer sitterId;

	/**
	 * 會員編號 (FK → member.mem_id，NOT NULL)
	 * 採用低耦合設計,不使用 @ManyToOne 關聯
	 * 透過 Service 層注入 MemberRegisterRepository 存取會員資料
	 */
	@Column(name = "mem_id", nullable = false)
	private Integer memId;

	/** 保姆姓名（NOT NULL，varchar(50)） */
	@Column(name = "sitter_name", nullable = false, length = 50)
	@NotBlank(message = "保姆姓名不能空白")
	@Size(min = 2, max = 50, message = "保姆姓名長度必須介於2到50個字")
	@Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s]+$", message = "保姆姓名只能包含中文、英文、數字或空格")
	private String sitterName;

	/** 服務地址（NOT NULL，varchar(100)） */
	@Column(name = "sitter_add", nullable = false, length = 100)
	@NotBlank(message = "服務地址不能空白")
	@Size(min = 5, max = 100, message = "服務地址長度必須介於5到100字元")
	@Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s]+$", message = "服務地址只能包含中文、英文、數字或空格")
	private String sitterAdd;

	/**
	 * 註冊保姆時間（NOT NULL，DB: CURRENT_TIMESTAMP） - 建議交給 DB 自動產生
	 */
	@Column(name = "sitter_created_at", nullable = false, insertable = false, updatable = false)
	private LocalDateTime sitterCreatedAt;

	/**
	 * 保姆狀態（NOT NULL，DEFAULT 0） - 0=啟用, 1=停用 - 建議新增時交給 DB default
	 */
	@Column(name = "sitter_status", nullable = false, insertable = false)
	private Byte sitterStatus;

	/**
	 * 服務時間（varchar(24) NOT NULL） - 每日 24 小時字串：0=不可預約,1=可預約
	 * 目前設定為 24 (日曆模式)，僅記錄一天的狀態。
	 * 若要支援每週差異排程 (如平日/週末不同)，需改回 168 (週曆模式)。
	 */
	@Column(name = "service_time", nullable = false, length = 24)

	private String serviceTime = "0".repeat(24);

	/** 保姆總評價數（DEFAULT 0） */
	@Column(name = "sitter_rating_count", insertable = false)
	private Integer sitterRatingCount = 0;

	/** 保姆總星星數（DEFAULT 0） */
	@Column(name = "sitter_star_count", insertable = false)
	private Integer sitterStarCount = 0;

	/** 服務區域（和ServiceAreaVO 關聯） */
	@OneToMany(mappedBy = "sitter", fetch = FetchType.LAZY)
	private List<ServiceAreaVO> serviceAreas;

	// ===== Getter/Setter =====

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

	public List<ServiceAreaVO> getServiceAreas() {
		return serviceAreas;
	}

	public void setServiceAreas(List<ServiceAreaVO> serviceAreas) {
		this.serviceAreas = serviceAreas;
	}
}
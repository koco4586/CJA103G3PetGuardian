package com.petguardian.sitter.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 保姆申請 Entity
 * 對應資料表: sitter_application
 * 
 * 紀錄會員申請成為保姆的詳細資料、審核狀態與歷程
 */
@Entity
@Table(name = "sitter_application")
public class SitterApplicationVO implements Serializable {//
    private static final long serialVersionUID = 1L;

    public SitterApplicationVO() { // 必需有一個不傳參數建構子(JavaBean基本知識)

    };
    // =========================
    // 申請基本資料
    // =========================

    /**
     * 申請編號（Primary Key）
     * - 資料庫自動遞增
     * - 系統內唯一識別一筆保姆申請
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_id", updatable = false, nullable = false)
    private Integer appId;

    /**
     * 會員編號
     * - 對應 member.mem_id
     * - 表示是哪一位會員提出保姆申請
     * - 採用低耦合設計,不使用 @ManyToOne 關聯
     * - 透過 Service 層存取會員資料
     */
    @Column(name = "mem_id", nullable = false)
    private Integer memId;

    /**
     * 保姆編號
     * - 審核通過後，對應到的保姆資料表 ID
     * - 若未通過或待審核，此欄位可能為 null
     * - 對應 sitter_application.sitter_id
     */
    @Column(name = "sitter_id")
    private Integer sitterId;

    /**
     * 個人簡介
     * - 會員自我介紹（textarea）
     * - 用於管理員審核與前台顯示
     */
    @Lob
    @Column(name = "app_intro", nullable = false)
    @NotBlank(message = "個人簡介: 請勿空白")
    @Size(min = 5, max = 500, message = "個人簡介: 長度需在{min}~{max}字")
    private String appIntro;

    /**
     * 相關經驗說明
     * - 描述照顧寵物、相關工作或經驗
     * - 為審核是否通過的重要依據
     */
    @Lob
    @Column(name = "app_experience", nullable = false)
    @NotBlank(message = "相關經驗: 請勿空白")
    @Size(min = 5, max = 500, message = "相關經驗: 長度需在{min}~{max}字")
    private String appExperience;

    // =========================
    // 審核狀態相關
    // =========================

    /**
     * 申請狀態
     * - 0：待審核（預設）
     * - 1：審核通過
     * - 2：審核不通過
     * - 3：已撤回
     * 
     * 新增時由資料庫 DEFAULT 0 自動給值
     */
    @Column(name = "app_status", insertable = false, nullable = false)
    private Byte appStatus;

    /**
     * 審核時間
     * - 管理員進行審核時填寫
     * - 前台申請時不會有值
     */
    @Column(name = "app_review_at")
    private LocalDateTime appReviewAt;

    /**
     * 審核意見
     * - 管理員對申請的補充說明或拒絕原因
     */
    @Column(name = "app_review_note", length = 100)
    private String appReviewNote;

    // =========================
    // 系統時間
    // =========================

    /**
     * 申請建立時間
     * - 紀錄會員送出申請的時間
     * - 由資料庫 DEFAULT CURRENT_TIMESTAMP 自動產生
     */
    @Column(name = "app_created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime appCreatedAt;

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

    public Integer getSitterId() {
        return sitterId;
    }

    public void setSitterId(Integer sitterId) {
        this.sitterId = sitterId;
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

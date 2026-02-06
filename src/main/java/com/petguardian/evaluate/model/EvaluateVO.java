package com.petguardian.evaluate.model;

import java.io.Serializable;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * 評價系統 VO - 對應 evaluate.html 與 EVALUATE 資料表
 */

@Entity // 👈 必須有這個註解
@Table(name = "evaluate") // 確保對應到你的資料庫表名
public class EvaluateVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id // 👈 必須有主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EVALUATE_ID")
    private Integer evaluateId; // 評價編號 (PK)

    @Column(name = "BOOKING_ORDER_ID")
    @JsonProperty("bookingOrderId")
    private Integer bookingOrderId; // 預約訂單編號 (對應圖中的 BOOKING_ORDER_ID)
    @Column(name = "SENDER_ID")
    private Integer senderId; // 評價人 (MEM_ID 或 SITTER_ID)
    @Column(name = "RECEIVER_ID")
    private Integer receiverId; // 被評價人 (SITTER_ID 或 MEM_ID)
    @Transient
    private Integer memberId;
    @Transient
    private Integer sitterId;
    @Column(name = "ROLE_TYPE")
    private Integer roleType;
    @Column(name = "STAR_RATING")
    private Integer starRating; // 星星分數 (1-5)
    @Column(name = "CONTENT")
    private String content; // 評價內容文字

    // 時間自動化：對應資料庫的 DEFAULT CURRENT_TIMESTAMP
    @Column(name = "CREATE_TIME", insertable = false, updatable = false)
    private Timestamp createTime;

    // 🔥 檢舉功能：隱藏/刪除狀態 (0=正常, 1=已隱藏, 2=已刪除)
    @Column(name = "IS_HIDDEN")
    private Integer isHidden = 0;

    // --- 擴充欄位 (供 evaluate.html 顯示用) ---
    @Transient
    private String senderName; // 評價者姓名

    @Transient
    private String createTimeText; // 格式化時間

    public EvaluateVO() {
    }

    // Getter & Setter (建議使用 IDE 自動產生以確保名稱精確)
    public Integer getEvaluateId() {
        return evaluateId;
    }

    public void setEvaluateId(Integer evaluateId) {
        this.evaluateId = evaluateId;
    }

    public Integer getBookingOrderId() {
        return bookingOrderId;
    }

    public void setBookingOrderId(Integer bookingOrderId) {
        this.bookingOrderId = bookingOrderId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
        this.senderId = memberId; // 同步給 senderId，確保資料庫不為 null
    }

    public Integer getSitterId() {
        return sitterId;
    }

    public void setSitterId(Integer sitterId) {
        this.sitterId = sitterId;
        this.receiverId = sitterId; // 同步給 receiverId
    }

    public Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    public Integer getStarRating() {
        return starRating;
    }

    public void setStarRating(Integer starRating) {
        this.starRating = starRating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getCreateTimeText() {
        return createTimeText;
    }

    public void setCreateTimeText(String createTimeText) {
        this.createTimeText = createTimeText;
    }

    public Integer getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Integer isHidden) {
        this.isHidden = isHidden;
    }
}

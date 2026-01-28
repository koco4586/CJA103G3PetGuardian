package com.petguardian.evaluate.model;
import java.io.Serializable;
import java.sql.Timestamp;



import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 評價系統 VO - 對應 evaluate.html 與 EVALUATE 資料表
 */

@Entity // 👈 必須有這個註解
@Table(name = "evaluate") // 確保對應到你的資料庫表名
public class EvaluateVO implements Serializable {
    private static final long serialVersionUID = 1L;

    
    @Id // 👈 必須有主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer evaluateId;      // 評價編號 (PK)
    private Integer bookingOrderId;  // 預約訂單編號 (對應圖中的 BOOKING_ORDER_ID)
    private Integer senderId;        // 評價人 (MEM_ID 或 SITTER_ID)
    private Integer receiverId;      // 被評價人 (SITTER_ID 或 MEM_ID)
    private Integer memberId; 
    private Integer sitterId;
    
    private Integer roleType;        
    
    private Integer starRating;      // 星星分數 (1-5)
    private String content;          // 評價內容文字
    
    // 時間自動化：對應資料庫的 DEFAULT CURRENT_TIMESTAMP
    private Timestamp createTime;    

    // --- 擴充欄位 (供 evaluate.html 顯示用) ---
    private String senderName;       // 評價者姓名
    private String createTimeText;   // 格式化時間

    public EvaluateVO() {}

    // Getter & Setter (建議使用 IDE 自動產生以確保名稱精確)
    public Integer getEvaluateId() { return evaluateId; }
    public void setEvaluateId(Integer evaluateId) { this.evaluateId = evaluateId; }

    public Integer getBookingOrderId() { return bookingOrderId; }
    public void setBookingOrderId(Integer bookingOrderId) { this.bookingOrderId = bookingOrderId; }

    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.senderId = memberId; }

    public Integer getSitterId() { return sitterId; }
    public void setSitterId(Integer sitterId) { this.receiverId = sitterId; }
    
    public Integer getRoleType() { return roleType; }
    public void setRoleType(Integer roleType) { this.roleType = roleType; }

    public Integer getStarRating() { return starRating; }
    public void setStarRating(Integer starRating) { this.starRating = starRating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getCreateTimeText() { return createTimeText; }
    public void setCreateTimeText(String createTimeText) { this.createTimeText = createTimeText; }
}

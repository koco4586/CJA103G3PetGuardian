package com.petguardian.evaluate.model;
import java.io.Serializable;

/**
 * 用於評價頁面「框中框」顯示的 DTO
 * 將同一筆訂單的會員評價與保姆回饋整合在一起
 */
public class EvaluateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer bookingOrderId; // 訂單編號 (用於大框標題)

    // --- 會員評價部分 (左側框) ---
    private Integer memberId; // 👈 補上這個
    private Integer sitterId; // 👈 建議也補上這個
    private String memberName;      // 會員名稱
    private Integer memberRating;   // 會員給的星等
    private String memberContent;   // 會員寫的評語
    private String memberCreateTime;// 會員評價時間
    private Double memberAvgRating; // 會員自己累積的平均星數 (從其他保姆回饋來的)
    // --- 保姆評價部分 (右側框) ---
    private String sitterName;      // 保姆名稱
    private Integer sitterRating;   // 保姆給會員的星等 (你提到的保姆也能評分)
    private String sitterContent;   // 保姆回覆/回饋內容
    private String sitterCreateTime;// 保姆評價時間
    private Double sitterStarcount; // 保姆自己累積的平均星數 (從其他會員評價來的)
    private Integer sitterTotalReviews;
    // --- 無參數建構子 ---
    public EvaluateDTO() {}

    // --- Getter 和 Setter ---

    public Integer getBookingOrderId() {
        return bookingOrderId;
    }

    public void setBookingOrderId(Integer bookingOrderId) {
        this.bookingOrderId = bookingOrderId;
    }

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    
    public Integer getSitterId() { return sitterId; }
    public void setSitterId(Integer sitterId) { this.sitterId = sitterId; }
    
    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Integer getMemberRating() {
        return memberRating;
    }

    public void setMemberRating(Integer memberRating) {
        this.memberRating = memberRating;
    }

    public String getMemberContent() {
        return memberContent;
    }

    public void setMemberContent(String memberContent) {
        this.memberContent = memberContent;
    }

    public String getMemberCreateTime() {
        return memberCreateTime;
    }

    public void setMemberCreateTime(String memberCreateTime) {
        this.memberCreateTime = memberCreateTime;
    }

    public Double getMemberAvgRating() { return memberAvgRating; }
    public void setMemberAvgRating(Double memberAvgRating) { this.memberAvgRating = memberAvgRating; }
    
    public String getSitterName() {
        return sitterName;
    }

    public void setSitterName(String sitterName) {
        this.sitterName = sitterName;
    }

    public Integer getSitterRating() {
        return sitterRating;
    }

    public void setSitterRating(Integer sitterRating) {
        this.sitterRating = sitterRating;
    }

    public String getSitterContent() {
        return sitterContent;
    }

    public void setSitterContent(String sitterContent) {
        this.sitterContent = sitterContent;
    }

    public String getSitterCreateTime() {
        return sitterCreateTime;
    }

    public void setSitterCreateTime(String sitterCreateTime) {
        this.sitterCreateTime = sitterCreateTime;
    }
    
    public Double getSitterStarcount() { return sitterStarcount; }
    public void setSitterStarcount(Double sitterStarcount) { this.sitterStarcount = sitterStarcount; }

    public Integer getSitterTotalReviews() { return sitterTotalReviews; }
    public void setSitterTotalReviews(Integer sitterTotalReviews) { this.sitterTotalReviews = sitterTotalReviews; }
    @Override
    public String toString() {
        return "EvaluateDTO [OrderId=" + bookingOrderId + ", Member=" + memberName + " (" + memberAvgRating + 
               "), Sitter=" + sitterName + " (" + sitterStarcount + ")]";
    }
}

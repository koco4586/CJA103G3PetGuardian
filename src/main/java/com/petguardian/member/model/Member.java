package com.petguardian.member.model;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.petguardian.forum.model.ForumCommentReportVO;
import com.petguardian.forum.model.ForumCommentVO;
import com.petguardian.forum.model.ForumPostReportVO;
import com.petguardian.forum.model.ForumPostVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "member")
@DynamicInsert  // INSERT 時只插入非 null 欄位
@DynamicUpdate  // UPDATE 時只更新有改變的欄位
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mem_id")
    private Integer memId;

    @Column(name = "mem_name")
    private String memName;

    @Column(name = "mem_email")
    private String memEmail;

    @Column(name = "mem_acc")
    private String memAcc;

    @Column(name = "mem_pw")
    private String memPw;

    @Column(name = "mem_image")
    private String memImage;

    @Column(name = "mem_uid")
    private String memUid;

    @Column(name = "mem_bth")
    private LocalDate memBth;

    @Column(name = "mem_sex")
    private Integer memSex;

    @Column(name = "mem_tel")
    private String memTel;

    @Column(name = "mem_add")
    private String memAdd;

    @Column(name = "mem_account_number")
    private String memAccountNumber;

    @Column(name = "mem_status")
    private Integer memStatus;

    @Column(name = "mem_sitter_status")
    private Integer memSitterStatus;

    @Column(name = "mem_created_at")
    private LocalDateTime memCreatedAt;

    @Column(name = "mem_shop_rating_score")
    private Integer memShopRatingScore;

    @Column(name = "mem_shop_rating_count")
    private Integer memShopRatingCount;

    @Column(name = "mem_login_attempts")
    private Integer memLoginAttempts;
    
    // 羽澈
    @OneToMany(mappedBy = "member")
    private Set<ForumPostVO> forumPosts;
    
    // 羽澈
    @OneToMany(mappedBy = "member")
    private Set<ForumPostReportVO> forumPostReports;
    
    // 羽澈
    @OneToMany(mappedBy = "member")
    private Set<ForumCommentVO> forumComments;
    
    // 羽澈
    @OneToMany(mappedBy = "member")
    private Set<ForumCommentReportVO> forumCommentReports;
    
    // 羽澈
    public Set<ForumCommentReportVO> getForumCommentReports() {
		return forumCommentReports;
	}
    
    // 羽澈
	public void setForumCommentReports(Set<ForumCommentReportVO> forumCommentReports) {
		this.forumCommentReports = forumCommentReports;
	}

	// 羽澈
    public Set<ForumPostReportVO> getForumPostReports() {
		return forumPostReports;
	}
    
    // 羽澈
	public void setForumPostReports(Set<ForumPostReportVO> forumPostReports) {
		this.forumPostReports = forumPostReports;
	}
	
	// 羽澈
	public Set<ForumCommentVO> getForumComments() {
		return forumComments;
	}
	
	// 羽澈
	public void setForumComments(Set<ForumCommentVO> forumComments) {
		this.forumComments = forumComments;
	}

	// 羽澈
    public Set<ForumPostVO> getForumPosts() {
		return forumPosts;
	}
    
    // 羽澈
	public void setForumPosts(Set<ForumPostVO> forumPosts) {
		this.forumPosts = forumPosts;
	}

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

    public String getMemEmail() {
        return memEmail;
    }

    public void setMemEmail(String memEmail) {
        this.memEmail = memEmail;
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

    public String getMemImage() {
        return memImage;
    }

    public void setMemImage(String memImage) {
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

    public void setMemCreatedAt(LocalDateTime memCreatedAt) {
        this.memCreatedAt = memCreatedAt;
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

    public Integer getMemLoginAttempts() {
        return memLoginAttempts;
    }

    public void setMemLoginAttempts(Integer memLoginAttempts) {
        this.memLoginAttempts = memLoginAttempts;
    }
}

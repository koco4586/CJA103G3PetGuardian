package com.petguardian.forum.model;

import java.sql.Timestamp;

public class PostReviewDetailDTO {
	
	// 檢舉端資訊
	private Integer reportId;
    private Integer reportType;
    private String reportReason;  // 檢舉者填寫的詳細原因
    private Timestamp reportTime;
    private Integer memId;

    // 被檢舉端資訊 (貼文或留言)
    private Integer postId;       // postId 或 commentId
    private String postTitle;     // 如果是貼文則有標題
    private String postContent;   // 完整內容 (Text)
    private Integer authorId;	  // 被檢舉人 ID
	
    public PostReviewDetailDTO(Integer reportId, Integer reportType, String reportReason, Timestamp reportTime,
			Integer memId, Integer postId, String postTitle, String postContent, Integer authorId) {
		super();
		this.reportId = reportId;
		this.reportType = reportType;
		this.reportReason = reportReason;
		this.reportTime = reportTime;
		this.memId = memId;
		this.postId = postId;
		this.postTitle = postTitle;
		this.postContent = postContent;
		this.authorId = authorId;
	}

	public Integer getReportId() {
		return reportId;
	}

	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}

	public Integer getReportType() {
		return reportType;
	}

	public void setReportType(Integer reportType) {
		this.reportType = reportType;
	}

	public String getReportReason() {
		return reportReason;
	}

	public void setReportReason(String reportReason) {
		this.reportReason = reportReason;
	}

	public Timestamp getReportTime() {
		return reportTime;
	}

	public void setReportTime(Timestamp reportTime) {
		this.reportTime = reportTime;
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public String getPostTitle() {
		return postTitle;
	}

	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}

	public String getPostContent() {
		return postContent;
	}

	public void setPostContent(String postContent) {
		this.postContent = postContent;
	}

	public Integer getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Integer authorId) {
		this.authorId = authorId;
	}
    
}

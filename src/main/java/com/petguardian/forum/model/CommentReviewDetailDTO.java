package com.petguardian.forum.model;

import java.sql.Timestamp;

public class CommentReviewDetailDTO {

	// 檢舉端資訊
	private Integer reportId;
    private Integer reportType;
    private String reportReason;  // 檢舉者填寫的詳細原因
    private Timestamp reportTime;
    private Integer memId;

    // 被檢舉端資訊 (貼文或留言)
    private Integer commentId;	  // postId 或 commentId
    private String commentContent;
    private Integer commentAuthorId;
    private Integer postId;
    private String postTitle;     
    private String postContent;   // 完整內容 (Text)
    private Integer postAuthorId;	  // 被檢舉人 ID
	
    public CommentReviewDetailDTO() {
		super();
	}
	
    public CommentReviewDetailDTO(Integer reportId, Integer reportType, String reportReason, Timestamp reportTime,
			Integer memId, Integer commentId, String commentContent, Integer commentAuthorId, Integer postId, String postTitle,
			String postContent, Integer postAuthorId) {
		super();
		this.reportId = reportId;
		this.reportType = reportType;
		this.reportReason = reportReason;
		this.reportTime = reportTime;
		this.memId = memId;
		this.commentId = commentId;
		this.commentContent = commentContent;
		this.commentAuthorId = commentAuthorId;
		this.postId = postId;
		this.postTitle = postTitle;
		this.postContent = postContent;
		this.postAuthorId = postAuthorId;
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
	
	public Integer getCommentId() {
		return commentId;
	}
	
	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
	}
	
	public String getCommentContent() {
		return commentContent;
	}
	
	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}
	
	public Integer getCommentAuthorId() {
		return commentAuthorId;
	}

	public void setCommentAuthorId(Integer commentAuthorId) {
		this.commentAuthorId = commentAuthorId;
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

	public Integer getPostAuthorId() {
		return postAuthorId;
	}

	public void setPostAuthorId(Integer postAuthorId) {
		this.postAuthorId = postAuthorId;
	}
	
}

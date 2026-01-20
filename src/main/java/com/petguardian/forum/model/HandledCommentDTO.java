package com.petguardian.forum.model;

import java.sql.Timestamp;

public class HandledCommentDTO {
	
	private Integer commentId;
	private String commentContent;
	private Integer memId;
	private Integer postId;
	private Integer reportType;
	private Timestamp handleTime;
	
	public HandledCommentDTO(Integer commentId, String commentContent, Integer memId, Integer postId,
			Integer reportType, Timestamp handleTime) {
		super();
		this.commentId = commentId;
		this.commentContent = commentContent;
		this.memId = memId;
		this.postId = postId;
		this.reportType = reportType;
		this.handleTime = handleTime;
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
	public Integer getReportType() {
		return reportType;
	}
	public void setReportType(Integer reportType) {
		this.reportType = reportType;
	}
	public Timestamp getHandleTime() {
		return handleTime;
	}
	public void setHandleTime(Timestamp handleTime) {
		this.handleTime = handleTime;
	}
	
}

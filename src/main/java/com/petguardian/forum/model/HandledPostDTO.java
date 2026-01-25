package com.petguardian.forum.model;

import java.sql.Timestamp;

public class HandledPostDTO {
	
	private Integer postId;
	private String postTitle;
	private String forumName;
	private Integer memId;
	private Integer reportType;
	private Timestamp handleTime;
	private Integer reportId;
	
	public HandledPostDTO() {
		super();
	}

	public HandledPostDTO(Integer postId, String postTitle, String forumName, Integer memId, Integer reportType,
			Timestamp handleTime, Integer reportId) {
		super();
		this.postId = postId;
		this.postTitle = postTitle;
		this.forumName = forumName;
		this.memId = memId;
		this.reportType = reportType;
		this.handleTime = handleTime;
		this.reportId = reportId;
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

	public String getForumName() {
		return forumName;
	}

	public void setForumName(String forumName) {
		this.forumName = forumName;
	}

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
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

	public Integer getReportId() {
		return reportId;
	}

	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}
	
}

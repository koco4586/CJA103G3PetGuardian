package com.petguardian.forum.model;

import java.sql.Timestamp;

public class RejectedPostDTO {

	private Integer postId;
	private String postTitle;
	private Integer memId;
	private String forumName;
	private Integer reportType;
	private Timestamp handleTime;
	private Integer reportId;
	
	public RejectedPostDTO() {
		super();
	}

	public RejectedPostDTO(Integer postId, String postTitle, Integer memId, String forumName, Integer reportType,
			Timestamp handleTime, Integer reportId) {
		super();
		this.postId = postId;
		this.postTitle = postTitle;
		this.memId = memId;
		this.forumName = forumName;
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

	public Integer getMemId() {
		return memId;
	}

	public void setMemId(Integer memId) {
		this.memId = memId;
	}

	public String getForumName() {
		return forumName;
	}

	public void setForumName(String forumName) {
		this.forumName = forumName;
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

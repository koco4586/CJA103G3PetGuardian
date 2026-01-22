package com.petguardian.forum.model;

import java.sql.Timestamp;

public class PendingPostDTO {

	private Integer reportId;
	private Integer memId;
	private Integer postId;
	private Integer authorId;
	private Integer reportType;
	private Timestamp reportTime;
	
	public PendingPostDTO(Integer reportId, Integer memId, Integer postId, Integer authorId, Integer reportType,
			Timestamp reportTime) {
		super();
		this.reportId = reportId;
		this.memId = memId;
		this.postId = postId;
		this.authorId = authorId;
		this.reportType = reportType;
		this.reportTime = reportTime;
	}

	public Integer getReportId() {
		return reportId;
	}

	public void setReportId(Integer reportId) {
		this.reportId = reportId;
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

	public Integer getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Integer authorId) {
		this.authorId = authorId;
	}

	public Integer getReportType() {
		return reportType;
	}

	public void setReportType(Integer reportType) {
		this.reportType = reportType;
	}

	public Timestamp getReportTime() {
		return reportTime;
	}

	public void setReportTime(Timestamp reportTime) {
		this.reportTime = reportTime;
	}
	
}

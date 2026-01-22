package com.petguardian.forum.model;

import java.sql.Timestamp;

public class PendingCommentDTO {

	private Integer reportId;
	private Integer memId;
	private Integer commentId;
	private Integer authorId;
	private Integer reportType;
	private Timestamp reportTime;
	
	public PendingCommentDTO(Integer reportId, Integer memId, Integer commentId, Integer authorId,
			Integer reportType, Timestamp reportTime) {
		super();
		this.reportId = reportId;
		this.memId = memId;
		this.commentId = commentId;
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

	public Integer getCommentId() {
		return commentId;
	}

	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
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

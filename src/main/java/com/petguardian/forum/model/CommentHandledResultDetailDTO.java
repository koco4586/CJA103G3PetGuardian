package com.petguardian.forum.model;

import java.sql.Timestamp;

public class CommentHandledResultDetailDTO {

	private Integer reportId;
	private Timestamp handleTime;
	private Integer reportStatus;
	private String handleResult;
	
	public CommentHandledResultDetailDTO() {
		super();
	}

	public CommentHandledResultDetailDTO(Integer reportId, Timestamp handleTime, Integer reportStatus,
			String handleResult) {
		super();
		this.reportId = reportId;
		this.handleTime = handleTime;
		this.reportStatus = reportStatus;
		this.handleResult = handleResult;
	}

	public Integer getReportId() {
		return reportId;
	}

	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}

	public Timestamp getHandleTime() {
		return handleTime;
	}

	public void setHandleTime(Timestamp handleTime) {
		this.handleTime = handleTime;
	}

	public Integer getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(Integer reportStatus) {
		this.reportStatus = reportStatus;
	}

	public String getHandleResult() {
		return handleResult;
	}

	public void setHandleResult(String handleResult) {
		this.handleResult = handleResult;
	}
	
}

package com.forumcommentreport.model;

import java.io.Serializable;

import java.sql.Timestamp;

import com.forumpostcomment.model.ForumPostCommentVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "forumcommentreport")
public class ForumCommentReportVO implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_id", updatable = false)
	private Integer reportId;
	
	@Column(name = "mem_id", updatable = false)
	private Integer memId;
	
	@ManyToOne
	@JoinColumn(name = "comment_id", referencedColumnName = "comment_id")
	private ForumPostCommentVO forumPostComment;
	
//	@Column(name = "comment_id", updatable = false)
//	private Integer commentId;
	
	@Column(name = "report_type")
	private Integer reportType;
	
	@Column(name = "report_reason")
	private String reportReason;
	
	@Column(name = "report_status", insertable = false)
	private Integer reportStatus;
	
	@Column(name = "report_time", insertable = false)
	private Timestamp reportTime;
	
	@Column(name = "handle_time", insertable = false)
	private Timestamp handleTime;
	
	public ForumCommentReportVO() {
		super();
	}

	public ForumPostCommentVO getForumPostComment() {
		return forumPostComment;
	}

	public void setForumPostComment(ForumPostCommentVO forumPostComment) {
		this.forumPostComment = forumPostComment;
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

//	public Integer getCommentId() {
//		return commentId;
//	}
//
//	public void setCommentId(Integer commentId) {
//		this.commentId = commentId;
//	}

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

	public Integer getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(Integer reportStatus) {
		this.reportStatus = reportStatus;
	}

	public Timestamp getReportTime() {
		return reportTime;
	}

	public void setReportTime(Timestamp reportTime) {
		this.reportTime = reportTime;
	}

	public Timestamp getHandleTime() {
		return handleTime;
	}

	public void setHandleTime(Timestamp handleTime) {
		this.handleTime = handleTime;
	}
	
}

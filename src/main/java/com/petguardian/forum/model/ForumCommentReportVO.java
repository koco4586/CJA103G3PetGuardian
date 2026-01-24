package com.petguardian.forum.model;

import java.io.Serializable;

import java.sql.Timestamp;

import com.petguardian.member.model.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "forumcommentreport")
public class ForumCommentReportVO implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_id", updatable = false)
	private Integer reportId;
	
//	@Column(name = "mem_id", updatable = false)
//	private Integer memId;
	
	@ManyToOne
	@JoinColumn(name = "mem_id", referencedColumnName = "mem_id")
	private Member member;
	
	@ManyToOne
	@JoinColumn(name = "comment_id", referencedColumnName = "comment_id")
	private ForumCommentVO forumComment;
	
//	@Column(name = "comment_id", updatable = false)
//	private Integer commentId;
	
	@Column(name = "report_type")
	private Integer reportType;
	
	@Column(name = "report_reason")
	@NotBlank(message = "檢舉原因請勿空白")
	@Size(min = 20, max = 800, message = "檢舉原因必需在{min}到{max}字之間")
	private String reportReason;
	
	@Column(name = "report_status", insertable = false)
	private Integer reportStatus;
	
	@Column(name = "report_time", insertable = false, updatable = false)
	private Timestamp reportTime;
	
	@Column(name = "handle_time", insertable = false, updatable = false)
	private Timestamp handleTime;
	
	@Column(name = "handle_result", nullable = true)
	@Size(max = 800, message = "處理結果最多不可超過{max}個字")
	private String handleResult;
	
	public ForumCommentReportVO() {
		super();
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public ForumCommentVO getForumComment() {
		return forumComment;
	}

	public void setForumComment(ForumCommentVO forumComment) {
		this.forumComment = forumComment;
	}

	public Integer getReportId() {
		return reportId;
	}

	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}

//	public Integer getMemId() {
//		return memId;
//	}
//
//	public void setMemId(Integer memId) {
//		this.memId = memId;
//	}

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

	public String getHandleResult() {
		return handleResult;
	}

	public void setHandleResult(String handleResult) {
		this.handleResult = handleResult;
	}
	
}

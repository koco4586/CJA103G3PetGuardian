package com.forum.model;

import java.io.Serializable;
import java.sql.Timestamp;

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
@Table(name = "forumpostreport")
public class ForumPostReportVO implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_id", updatable = false)
	private Integer reportId;
	
	@Column(name = "mem_id", updatable = false)
	private Integer memId;
	
	@ManyToOne
	@JoinColumn(name = "post_id", referencedColumnName = "post_id")
	private ForumPostVO forumPost;
	
//	@Column(name = "post_id", updatable = false)
//	private Integer postId;
	
	@Column(name = "report_type")
	private Integer reportType;
	
	@Column(name = "report_reason")
	@NotBlank(message = "檢舉原因請勿空白")
	@Size(min = 20, max = 800, message = "檢舉原因長度必需在{min}到{max}字之間")
	private String reportReason;
	
	@Column(name = "report_status", insertable = false)
	private Integer reportStatus;
	
	@Column(name = "report_time", insertable = false, updatable = false)
	private Timestamp reportTime;
	
	@Column(name = "handle_time", insertable = false, updatable = false)
	private Timestamp handleTime;
	
	public ForumPostReportVO() {
		super();
	}

	public ForumPostVO getForumPost() {
		return forumPost;
	}

	public void setForumPost(ForumPostVO forumPost) {
		this.forumPost = forumPost;
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
	
//	public Integer getPostId() {
//		return postId;
//	}
	
//	public void setPostId(Integer postId) {
//		this.postId = postId;
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

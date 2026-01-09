package com.forumpostcomment.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

import com.forumcommentreport.model.ForumCommentReportVO;
import com.forumpost.model.ForumPostVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "forumpostcomment")
public class ForumPostCommentVO implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id", updatable = false)
	private Integer commentId;
	
	@Column(name = "mem_id", updatable = false)
	private Integer memId;
	
	@ManyToOne
	@JoinColumn(name = "post_id", referencedColumnName = "post_id")
	private ForumPostVO forumPost;
	
//	@Column(name = "post_id", updatable = false)
//	private Integer postId;
	
	@OneToMany(mappedBy = "forumPostComment")
	@OrderBy("reportId asc")
	private Set<ForumCommentReportVO> forumCommentReport;
	
	@Column(name = "comment_content")
	private String commentContent;
	
	@Column(name = "comment_pic", columnDefinition = "longblob")
	private byte[] commentPic;
	
	@Column(name = "created_at", updatable = false, insertable = false)
	private Timestamp createdAt;
	
	@Column(name = "last_edited_at", insertable = false)
	private Timestamp lastEditedAt;
	
	@Column(name = "comment_status", insertable = false)
	private Integer commentStatus;
	
	public ForumPostCommentVO() {
		super();
	}

	public ForumPostVO getForumPost() {
		return forumPost;
	}

	public void setForumPost(ForumPostVO forumPost) {
		this.forumPost = forumPost;
	}

	public Set<ForumCommentReportVO> getForumCommentReport() {
		return forumCommentReport;
	}

	public void setForumCommentReport(Set<ForumCommentReportVO> forumCommentReport) {
		this.forumCommentReport = forumCommentReport;
	}

	public Integer getCommentId() {
		return commentId;
	}

	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
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
//
//	public void setPostId(Integer postId) {
//		this.postId = postId;
//	}

	public String getCommentContent() {
		return commentContent;
	}

	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}

	public byte[] getCommentPic() {
		return commentPic;
	}

	public void setCommentPic(byte[] commentPic) {
		this.commentPic = commentPic;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getLastEditedAt() {
		return lastEditedAt;
	}

	public void setLastEditedAt(Timestamp lastEditedAt) {
		this.lastEditedAt = lastEditedAt;
	}

	public Integer getCommentStatus() {
		return commentStatus;
	}

	public void setCommentStatus(Integer commentStatus) {
		this.commentStatus = commentStatus;
	}
	
}

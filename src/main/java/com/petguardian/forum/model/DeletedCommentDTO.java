package com.petguardian.forum.model;

import java.sql.Timestamp;

public class DeletedCommentDTO {
	
	private Integer commentId;
	private String commentContent;
	private Integer memId;
	private Integer postId;
	private Timestamp lastEditedAt;
	
	public DeletedCommentDTO(Integer commentId, String commentContent, Integer memId, Integer postId,
			Timestamp lastEditedAt) {
		super();
		this.commentId = commentId;
		this.commentContent = commentContent;
		this.memId = memId;
		this.postId = postId;
		this.lastEditedAt = lastEditedAt;
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

	public Timestamp getLastEditedAt() {
		return lastEditedAt;
	}

	public void setLastEditedAt(Timestamp lastEditedAt) {
		this.lastEditedAt = lastEditedAt;
	}
	
}

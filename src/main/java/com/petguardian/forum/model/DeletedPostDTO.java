package com.petguardian.forum.model;

import java.sql.Timestamp;

public class DeletedPostDTO {

	private Integer postId;
	private String postTitle;
	private Integer memId;
	private String forumName;
	private Timestamp lastEditedAt;
	
	public DeletedPostDTO(Integer postId, String postTitle, Integer memId, String forumName, Timestamp lastEditedAt) {
		super();
		this.postId = postId;
		this.postTitle = postTitle;
		this.memId = memId;
		this.forumName = forumName;
		this.lastEditedAt = lastEditedAt;
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

	public Timestamp getLastEditedAt() {
		return lastEditedAt;
	}

	public void setLastEditedAt(Timestamp lastEditedAt) {
		this.lastEditedAt = lastEditedAt;
	}
	
}

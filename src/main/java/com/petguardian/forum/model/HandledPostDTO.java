package com.petguardian.forum.model;

import java.sql.Timestamp;

public class HandledPostDTO {
	
	private Integer postId;
	private String postTitle;
	private String forumName;
	private Integer memId;
	private Integer reportType;
	private Timestamp handleTime;
	
	public HandledPostDTO(Integer postId, String postTitle, String forumName, Integer memId, Integer reportType,
			Timestamp handleTime) {
		super();
		this.postId = postId;
		this.postTitle = postTitle;
		this.forumName = forumName;
		this.memId = memId;
		this.reportType = reportType;
		this.handleTime = handleTime;
	}
	
}

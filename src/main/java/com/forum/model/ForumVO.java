package com.forum.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

import com.forumpost.model.ForumPostVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "forum")
public class ForumVO implements Serializable{
		
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "forum_id", updatable = false)
	private Integer forumId;
	
	@Column(name = "forum_name")
	private String forumName;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;
	
	@Column(name = "forum_pic", columnDefinition = "longblob")
	private byte[] forumPic;
	
	@Column(name = "forum_status", insertable = false)
	private Integer forumStatus;
	
	@OneToMany(mappedBy = "forum")
	@OrderBy("postId desc")
	private Set<ForumPostVO> forumPost;
	
	public ForumVO() {
		super();
	}

	public Set<ForumPostVO> getForumPost() {
		return forumPost;
	}

	public void setForumPost(Set<ForumPostVO> forumPost) {
		this.forumPost = forumPost;
	}

	public Integer getForumId() {
		return forumId;
	}
	
	public void setForumId(Integer forumId) {
		this.forumId = forumId;
	}
	
	public String getForumName() {
		return forumName;
	}
	
	public void setForumName(String forumName) {
		this.forumName = forumName;
	}
	
	public Timestamp getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}
	
	public Integer getForumStatus() {
		return forumStatus;
	}
	
	public void setForumStatus(Integer forumStatus) {
		this.forumStatus = forumStatus;
	}

	public byte[] getForumPic() {
		return forumPic;
	}

	public void setForumPic(byte[] forumPic) {
		this.forumPic = forumPic;
	}
	
}

package com.petguardian.forum.model;

import java.io.Serializable;
import java.sql.Date;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "forum")
public class ForumVO implements Serializable{
		
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "forum_id", updatable = false)
	private Integer forumId;
	
	@Column(name = "forum_name")
	@NotBlank(message = "名稱請勿空白")
	@Pattern(regexp = "^[^<>\\\\/|\\r\\n]{1,50}$", message = "名稱格式不合法或包含換行，且不能超過50字")
	private String forumName;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private Date createdAt;
	
	@Lob
	@Column(name = "forum_pic", nullable = true, columnDefinition = "longblob")
	private byte[] forumPic;
	
	@Column(name = "forum_status", insertable = false)
	private Integer forumStatus;
	
	@Column(name = "forum_views", nullable = true, insertable = false)
	private Integer forumViews;
	
	@Transient
	private MultipartFile upFile;
	
	@OneToMany(mappedBy = "forum")
	@OrderBy("postId desc")
	private Set<ForumPostVO> forumPosts;
	
	public ForumVO() {
		super();
	}

	public MultipartFile getUpFile() {
		return upFile;
	}

	public void setUpFile(MultipartFile upFile) {
		this.upFile = upFile;
	}
	
	public Set<ForumPostVO> getForumPosts() {
		return forumPosts;
	}

	public void setForumPosts(Set<ForumPostVO> forumPosts) {
		this.forumPosts = forumPosts;
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
	
	public Date getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	public Integer getForumStatus() {
		return forumStatus;
	}
	
	public void setForumStatus(Integer forumStatus) {
		this.forumStatus = forumStatus;
	}
	
	public Integer getForumViews() {
		return forumViews;
	}

	public void setForumViews(Integer forumViews) {
		this.forumViews = forumViews;
	}

	public byte[] getForumPic() {
		return forumPic;
	}

	public void setForumPic(byte[] forumPic) {
		this.forumPic = forumPic;
	}
	
    //	驗證上傳檔案是否為圖片檔 || 驗證圖片大小不得超過1MB
	@AssertTrue(message = "請上傳圖片檔（jpg, png, gif），且檔案大小不得超過 1MB ")
	public boolean isValidImage() {		
		if (upFile == null || upFile.isEmpty()) {
			return true;
		}
		
		String contentType = upFile.getContentType();
		if(contentType == null || !contentType.startsWith("image/")) {
			return false;
		}
		
		long maxSize = 1 * 1024 *1024;
		if(upFile.getSize() > maxSize) {
			return false;
		}
		return true;
	}
	
}

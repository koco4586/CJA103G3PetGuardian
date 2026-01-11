package com.forum.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.forumpost.model.ForumPostVO;

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
	@NotBlank(message = "討論區名稱請勿空白")
	@Pattern(regexp = "^[(\u4e00-\u9fa5)(a-zA-Z0-9)]{1,50}$", message = "討論區名稱: 只能是中、英文字母、或數字，且不能超過50字")
	private String forumName;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;
	
	@Lob
	@Column(name = "forum_pic", nullable = true, columnDefinition = "longblob")
	private byte[] forumPic;
	
	@Column(name = "forum_status", insertable = false)
	private Integer forumStatus;
	
	@Transient
	private MultipartFile upFiles;
	
	@OneToMany(mappedBy = "forum")
	@OrderBy("postId desc")
	private Set<ForumPostVO> forumPost;
	
	public ForumVO() {
		super();
	}
	
	public MultipartFile getMultipartFile() {
		return upFiles;
	}

	public void setMultipartFile(MultipartFile upFiles) {
		this.upFiles = upFiles;
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
	
    //	驗證上傳檔案是否為圖片檔
	@AssertTrue(message = "請上傳圖片檔（jpg, png, gif）")
	public boolean isImage() {		
		if (upFiles == null || upFiles.isEmpty()) {
			return true;
		}
		String contentType = upFiles.getContentType();
		return contentType != null && contentType.startsWith("image/");	
	}
	
	//	驗證圖片大小不得超過1MB
	@AssertTrue(message = "圖片過大，請選擇小於 1MB 的檔案")
	public boolean isSize() {
		if (upFiles == null || upFiles.isEmpty()) {
			return true;
		}
		long maxSize = 1 * 1024 *1024;	
		return upFiles != null && maxSize > upFiles.getSize();
	}
	
}

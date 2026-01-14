package com.forum.post.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.forum.model.ForumVO;
import com.forum.postcomment.model.ForumPostCommentVO;
import com.forum.postpic.model.ForumPostPicVO;
import com.forum.postreport.model.ForumPostReportVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "forumpost")
public class ForumPostVO implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id", updatable = false)
	private Integer postId;
	
	@Column(name = "mem_id", updatable = false)
	private Integer memId;
	
	@ManyToOne
	@JoinColumn(name = "forum_id", referencedColumnName = "forum_id")
	private ForumVO forum;
	
//	@Column(name = "forum_id", updatable = false)
//	private Integer forumId;
	
	@Column(name = "post_title")
	@NotBlank(message = "文章標題請勿空白")
	@Pattern(regexp = "^[(\u4e00-\u9fa5)(a-zA-Z0-9)]{1,50}$", message = "文章標題: 只能是中、英文字母、或數字，且不能超過50字")
	private String postTitle;
	
	@Column(name = "post_content")
	@NotBlank(message = "文章內容請勿空白")
	@Size(min = 30, max = 2500, message = "文章長度必需在{min}到{max}字之間")
	private String postContent;
	
	@Lob
	@Column(name = "post_pic", nullable = true, columnDefinition = "longblob")
	private byte[] postPic;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;
	
	@Column(name = "last_edited_at", insertable = false, updatable = false)
	private Timestamp lastEditedAt;
	
	@Column(name = "post_status", insertable = false)
	private Integer postStatus;
	
	@Transient
	private MultipartFile upFiles;
	
	@OneToMany(mappedBy = "forumPost")
	@OrderBy("picId asc")
	private Set<ForumPostPicVO> forumPostPic;
	
	@OneToMany(mappedBy = "forumPost")
	@OrderBy("commentId asc")
	private Set<ForumPostCommentVO> forumPostComment;
	
	@OneToMany(mappedBy = "forumPost")
	@OrderBy("reportId asc")
	private Set<ForumPostReportVO> forumPostReport;
	
//	@ManyToMany
//	@JoinTable(
//			   name = "forumpostcollection",
//			   joinColumns = { @JoinColumn( name = "post_id", referencedColumnName = "post_id" ) },
//			   inverseJoinColumns = { @JoinColumn( name = "mem_id", referencedColumnName = "mem_id") }
//			  )
//	private Set<> members;
	
	public ForumPostVO() {
		super();
	}

	public MultipartFile getUpFiles() {
		return upFiles;
	}

	public void setUpFiles(MultipartFile upFiles) {
		this.upFiles = upFiles;
	}

	public ForumVO getForum() {
		return forum;
	}

	public void setForum(ForumVO forum) {
		this.forum = forum;
	}

	public Set<ForumPostPicVO> getForumPostPic() {
		return forumPostPic;
	}

	public void setForumPostPic(Set<ForumPostPicVO> forumPostPic) {
		this.forumPostPic = forumPostPic;
	}

	public Set<ForumPostCommentVO> getForumPostComment() {
		return forumPostComment;
	}

	public void setForumPostComment(Set<ForumPostCommentVO> forumPostComment) {
		this.forumPostComment = forumPostComment;
	}

	public Set<ForumPostReportVO> getForumPostReport() {
		return forumPostReport;
	}

	public void setForumPostReport(Set<ForumPostReportVO> forumPostReport) {
		this.forumPostReport = forumPostReport;
	}

	public Integer getPostId() {
		return postId;
	}
	
	public void setPostId(Integer postId) {
		this.postId = postId;
	}
	
	public Integer getMemId() {
		return memId;
	}
	
	public void setMemId(Integer memId) {
		this.memId = memId;
	}
	
//	public Integer getForumId() {
//		return forumId;
//	}
	
//	public void setForumId(Integer forumId) {
//		this.forumId = forumId;
//	}
	
	public String getPostTitle() {
		return postTitle;
	}
	
	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}
	
	public String getPostContent() {
		return postContent;
	}
	
	public void setPostContent(String postContent) {
		this.postContent = postContent;
	}
	
	public byte[] getPostPic() {
		return postPic;
	}
	
	public void setPostPic(byte[] postPic) {
		this.postPic = postPic;
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
	
	public Integer getPostStatus() {
		return postStatus;
	}
	
	public void setPostStatus(Integer postStatus) {
		this.postStatus = postStatus;
	}
	
	//	驗證上傳檔案是否為圖片檔
	@AssertTrue(message = "請上傳圖片檔（jpg, png, gif）")
	public boolean isImage() {
		if(upFiles == null || upFiles.isEmpty()) {
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

package com.forum.postcomment.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.forum.commentreport.model.ForumCommentReportVO;
import com.forum.post.model.ForumPostVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
	@NotBlank(message = "留言內容請勿空白")
	@Size(min = 1, max = 800, message = "留言長度必需在{min}到{max}字之間")
	private String commentContent;
	
	@Lob
	@Column(name = "comment_pic", nullable = true, columnDefinition = "longblob")
	private byte[] commentPic;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;
	
	@Column(name = "last_edited_at", insertable = false, updatable = false)
	private Timestamp lastEditedAt;
	
	@Column(name = "comment_status", insertable = false)
	private Integer commentStatus;
	
	@Transient
	private MultipartFile upFiles;
	
	public ForumPostCommentVO() {
		super();
	}

	public MultipartFile getUpFiles() {
		return upFiles;
	}

	public void setUpFiles(MultipartFile upFiles) {
		this.upFiles = upFiles;
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

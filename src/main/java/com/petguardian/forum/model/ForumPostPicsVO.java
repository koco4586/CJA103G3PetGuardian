package com.petguardian.forum.model;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "forumpostpicture")
public class ForumPostPicsVO implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pic_id", updatable = false)
	private Integer picId;
	
	@ManyToOne
	@JoinColumn(name = "post_id", referencedColumnName = "post_id")
	private ForumPostVO forumPost;
	
	@Lob
	@Column(name = "pic", nullable = true, columnDefinition = "longblob")
	private byte[] pic;
	
	@Transient
	private MultipartFile[] upFiles;

	public ForumPostPicsVO() {
		super();
	}

	public MultipartFile[] getUpFiles() {
		return upFiles;
	}

	public void setUpFiles(MultipartFile[] upFiles) {
		this.upFiles = upFiles;
	}

	public ForumPostVO getForumPost() {
		return forumPost;
	}

	public void setForumPost(ForumPostVO forumPost) {
		this.forumPost = forumPost;
	}

	public Integer getPicId() {
		return picId;
	}

	public void setPicId(Integer picId) {
		this.picId = picId;
	}

	public byte[] getPic() {
		return pic;
	}

	public void setPic(byte[] pic) {
		this.pic = pic;
	}
	
//	驗證上傳檔案是否為圖片檔
//	@AssertTrue(message = "請上傳圖片檔（jpg, png, gif）")
//	public boolean isImage() {
//	
//		if(upFiles == null || upFiles.length == 0) {
//			return true;
//		}
//		
//		for(int i = 0; i < upFiles.length; i++) {
//			if(upFiles[i] == null || upFiles[i].isEmpty()) {
//				continue;
//			} else {
//				String contentType = upFiles[i].getContentType();
//				if(contentType == null || !contentType.startsWith("image/")) {
//					return false;
//				}
//			}
//			
//		}
//		return true;
//	}
	
//	驗證單張圖片大小跟總上傳檔案大小
//	@AssertTrue(message = "單張圖片大小不得超過 1MB，且總上傳檔案不得超過 5MB")
//	public boolean isSize() {
//		if (upFiles == null || upFiles.length == 0) {
//			return true;
//		}
//		long maxSize = 1 * 1024 *1024;
//		long totalMaxSize = 5 * 1024 *1024;
//		long upFilesTotalSize = 0;
//		for(int i = 0; i < upFiles.length; i++) {
//			if(upFiles[i].isEmpty()) {
//				continue;
//			}
//			if(upFiles[i].getSize() > maxSize) {
//				return false;
//			} else {
//				upFilesTotalSize += upFiles[i].getSize();
//				if(upFilesTotalSize > totalMaxSize) {
//					return false;
//				}
//				
//			}
//			
//		}
//		return true;
//	}
	
}

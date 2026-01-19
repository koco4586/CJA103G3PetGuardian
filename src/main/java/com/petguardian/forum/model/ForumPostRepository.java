package com.petguardian.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForumPostRepository extends JpaRepository<ForumPostVO, Integer> {
	
	//	關鍵字查詢
	@Query(value = "select p from ForumPostVO p where p.postStatus = 1 and p.forum.forumId = :forumId and (p.postTitle like concat('%', :keyword, '%') or p.postContent like concat('%', :keyword, '%')) order by p.postId desc")
	public List<ForumPostVO> findByKeyword(@Param("keyword") String keyword, @Param("forumId") Integer forumId);
	
	// 	Spring 會自動解析為：postStatus = 1 AND postTitle LIKE %...% ORDER BY postId DESC
	//	List<ForumPostVO> findByPostStatusAndPostTitleContainingOrderByPostIdDesc(Integer postStatus, String postTitle);
	
	//	後台用，顯示已處理(檢舉下架)的文章
	@Query(value = "select p from ForumPostVO p join fetch p.forum f where p.postStatus = 0")
	public List<ForumPostVO> getAllHandledPosts();
	
	//	從討論區id拿到該討論區所有啟用中文章
	@Query(value = "select p from ForumPostVO p where p.forum.forumId = :forumId and p.postStatus = 1 order by p.postId desc")
	public List<ForumPostVO> findPostsByForumId(@Param("forumId") Integer forumId);
	
	//	只拿主頁圖片方法
	@Query(value = "select p.postPic from ForumPostVO p where p.postId = :postId")
	public byte[] getPicture(@Param("postId") Integer postId);
	
}

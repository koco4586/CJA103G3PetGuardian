package com.petguardian.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ForumPostRepository extends JpaRepository<ForumPostVO, Integer> {
	
	//	優化讀取貼文時的N+1問題
	@Query("""
			select distinct p 
			from ForumPostVO p
			join fetch p.member m
			left join fetch p.forumPostComments c
			left join fetch c.member cm
			where p.postId = :postId
	""")
	public ForumPostVO findOnePostWithCommentAndMember(@Param("postId") Integer postId);
	
	//	關鍵字查詢
	@Query(value = "select p from ForumPostVO p where p.postStatus = 1 and p.forum.forumId = :forumId and (p.postTitle like concat('%', :keyword, '%') or p.postContent like concat('%', :keyword, '%')) order by p.postId desc")
	public List<ForumPostVO> findByKeyword(@Param("keyword") String keyword, @Param("forumId") Integer forumId);
	
	// 	Spring 會自動解析為：postStatus = 1 AND postTitle LIKE %...% ORDER BY postId DESC
	//	List<ForumPostVO> findByPostStatusAndPostTitleContainingOrderByPostIdDesc(Integer postStatus, String postTitle);	
	
	//	從討論區id拿到該討論區所有啟用中文章
	@Query(value = "select p from ForumPostVO p where p.forum.forumId = :forumId and p.postStatus = 1 order by p.postId desc")
	public List<ForumPostVO> findPostsByForumId(@Param("forumId") Integer forumId);
	
	//	將Redis裡存的文章瀏覽次數寫回MySQL
	@Modifying
	@Transactional
	@Query(value = "update forumpost set post_views = :postViewCount where post_id = :postId", nativeQuery = true)
	public void savePostViewCountToDatabase(@Param("postId") Integer postId, @Param("postViewCount") Integer postViewCount);
	
	@Query("""
			select new com.petguardian.forum.model.DeletedPostDTO(
				p.postId, p.postTitle, m.memId, f.forumName, p.lastEditedAt
			)
			from ForumPostVO p
			join p.member m
			join p.forum f
			where p.postStatus = 2
			order by p.lastEditedAt desc
	""")
	public List<DeletedPostDTO> findAllDeletedPosts();
	
	//	只 fetch forum (因為頁面要顯示看板名)，不 fetch members (因為頁面不需要顯示其他收藏者)
	@Query("select distinct p from ForumPostVO p " +
	       "join p.members m " + 	 // 用來過濾「該會員的收藏」
	       "join fetch p.forum f " + // 用來顯示「討論區名稱 && 討論區ID」
	       "where m.memId = :memId order by p.createdAt desc")
	List<ForumPostVO> findAllPostCollectionsByMemId(@Param("memId") Integer memId);
	
	//	只拿主頁圖片方法
	@Query(value = "select p.postPic from ForumPostVO p where p.postId = :postId")
	public byte[] getPicture(@Param("postId") Integer postId);
	
}

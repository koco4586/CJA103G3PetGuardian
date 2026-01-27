package com.petguardian.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForumCommentRepository extends JpaRepository<ForumCommentVO, Integer> {
	
	@Query(value = "select c from ForumCommentVO c where commentStatus = 1 and c.forumPost.postId = :postId order by c.createdAt asc")
	public List<ForumCommentVO> findCommentsByPostId(@Param("postId") Integer postId);
	
	//	管理員用
	@Query(value = "select c from ForumCommentVO c where c.forumPost.postId = :postId order by c.createdAt asc")
	public List<ForumCommentVO> findAllCommentsByPostId(@Param("postId") Integer postId);
	
	@Query("""
			select new com.petguardian.forum.model.DeletedCommentDTO(
				c.commentId, c.commentContent, m.memId, p.postId, c.lastEditedAt
			)
			from ForumCommentVO c
			join c.member m
			join c.forumPost p
			where c.commentStatus = 2
			order by c.lastEditedAt desc
	""")
	public List<DeletedCommentDTO> findAllDeletedComments();
	
}

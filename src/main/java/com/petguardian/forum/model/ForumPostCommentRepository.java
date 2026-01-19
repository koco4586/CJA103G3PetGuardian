package com.petguardian.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForumPostCommentRepository extends JpaRepository<ForumPostCommentVO, Integer> {
	
	@Query(value = "select c from ForumPostCommentVO c where commentStatus = 1 and c.forumPost.postId = :postId order by c.commentId asc")
	public List<ForumPostCommentVO> findCommentsByPostId(@Param("postId") Integer postId);
	
}

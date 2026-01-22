package com.petguardian.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ForumCommentReportRepository extends JpaRepository<ForumCommentReportVO, Integer> {

	@Query("""
			select new com.petguardian.forum.model.HandledCommentDTO(
				c.commentId, c.commentContent, c.member.memId, p.postId, r.reportType, r.handleTime
			)
			from ForumCommentReportVO r
			join r.forumPostComment c
			join c.forumPost p
			where c.commentStatus = 0 and r.reportStatus = 1
			order by r.handleTime desc
	""")
	public List<HandledCommentDTO> findAllHandledComments();
	
	
	
}

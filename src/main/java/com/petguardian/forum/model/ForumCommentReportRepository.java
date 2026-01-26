package com.petguardian.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForumCommentReportRepository extends JpaRepository<ForumCommentReportVO, Integer> {

	@Query("""
			select new com.petguardian.forum.model.HandledCommentDTO(
				c.commentId, c.commentContent, c.member.memId, p.postId, r.reportType, r.handleTime
			)
			from ForumCommentReportVO r
			join r.forumComment c
			join c.forumPost p
			where c.commentStatus = 0 and r.reportStatus = 1
			order by r.handleTime desc
	""")
	public List<HandledCommentDTO> findAllHandledComments();
	
	@Query("""
			select new com.petguardian.forum.model.PendingCommentDTO(
				r.reportId, r.member.memId, c.commentId, c.member.memId, r.reportType, r.reportTime, p.postId 
			)
			from ForumCommentReportVO r
			join r.forumComment c
			join c.forumPost p
			where c.commentStatus = 1 and r.reportStatus = 0
			order by r.reportTime asc
	""")
	public List<PendingCommentDTO> findAllPendingComments();
	
	@Query("""
			select new com.petguardian.forum.model.RejectedCommentDTO(
				c.commentId, c.commentContent, c.member.memId, p.postId, r.reportType, r.handleTime, r.reportId
			)
			from ForumCommentReportVO r
			join r.forumComment c
			join c.forumPost p
			where c.commentStatus = 1 and r.reportStatus = 2
			order by r.handleTime desc
	""")
	public List<RejectedCommentDTO> findAllRejectedComments();
	
	@Query("""
			select new com.petguardian.forum.model.CommentReviewDetailDTO(
				r.reportId, r.reportType, r.reportReason, r.reportTime,
				r.member.memId, c.commentId, c.commentContent, c.member.memId,
				p.postId, p.postTitle, p.postContent, p.member.memId
			)
			from ForumCommentReportVO r
			join r.forumComment c
			join c.forumPost p
			where r.reportId = :reportId
	""")
	public CommentReviewDetailDTO commentReviewDetailToHandle(@Param("reportId") Integer reportId);
	
	@Query("""
			select new com.petguardian.forum.model.CommentHandledResultDetailDTO(
				r.reportId, r.handleTime, r.reportStatus, r.handleResult
			)
			from ForumCommentReportVO r
			where r.reportId = :reportId
	""")
	public CommentHandledResultDetailDTO commentHandledResultDetailToDisplay(@Param("reportId") Integer reportId);
	
}

package com.petguardian.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForumPostReportRepository extends JpaRepository<ForumPostReportVO, Integer> {
	
	@Query("""
			select new com.petguardian.forum.model.HandledPostDTO(
				p.postId, p.postTitle, f.forumName, p.member.memId, r.reportType, r.handleTime, r.reportId
			)
			from ForumPostReportVO r
			join r.forumPost p
			join p.forum f
			where p.postStatus = 0 and r.reportStatus = 1
			order by r.handleTime desc
	""")
	public List<HandledPostDTO> findAllHandledPosts();

	@Query("""
			select new com.petguardian.forum.model.PendingPostDTO(
				r.reportId, r.member.memId, p.postId, p.member.memId, r.reportType, r.reportTime
			)
			from ForumPostReportVO r
			join r.forumPost p
			where p.postStatus = 1 and r.reportStatus = 0
			order by r.reportTime asc
	""")
	public List<PendingPostDTO> findAllPendingPosts();

	
	@Query("""
			select new com.petguardian.forum.model.RejectedPostDTO(
				p.postId, p.postTitle, p.member.memId, f.forumName, r.reportType, r.handleTime, r.reportId
			)
			from ForumPostReportVO r
			join r.forumPost p
			join p.forum f
			where p.postStatus = 1 and r.reportStatus = 2
			order by r.handleTime desc
	""")
	public List<RejectedPostDTO> findAllRejectedPosts();
	
	@Query("""
			select new com.petguardian.forum.model.PostReviewDetailDTO(
				r.reportId, r.reportType, r.reportReason, r.reportTime,
				r.member.memId, p.postId, p.postTitle, p.postContent, p.member.memId
			)
			from ForumPostReportVO r
			join r.forumPost p
			where r.reportId = :reportId
	""")
	public PostReviewDetailDTO postReviewDetailToHandle(@Param("reportId") Integer reportId);
	
	@Query("""
			select new com.petguardian.forum.model.PostHandledResultDetailDTO(
				r.reportId, r.handleTime, r.reportStatus, r.handleResult
			)
			from ForumPostReportVO r
			where r.reportId = :reportId
	""")
	public PostHandledResultDetailDTO postHandledResultDetailToDisplay(@Param("reportId") Integer reportId);
	
	
	
	
	
	
	
}

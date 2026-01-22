package com.petguardian.forum.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ForumPostReportRepository extends JpaRepository<ForumPostReportVO, Integer> {
	
	@Query("""
			select new com.petguardian.forum.model.HandledPostDTO(
				p.postId, p.postTitle, f.forumName, p.member.memId, r.reportType, r.handleTime
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
				p.postId, p.postTitle, p.member.memId, f.forumName, r.reportType, r.handleTime
			)
			from ForumPostReportVO r
			join r.forumPost p
			join p.forum f
			where p.postStatus = 1 and r.reportStatus = 2
			order by r.handleTime desc
	""")
	public List<RejectedPostDTO> findAllRejectedPosts();
	
	
	
	
	
	
	
	
	
	
	
}

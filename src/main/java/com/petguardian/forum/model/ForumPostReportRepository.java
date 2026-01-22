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



}

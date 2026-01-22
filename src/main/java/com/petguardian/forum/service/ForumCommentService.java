package com.petguardian.forum.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.forum.model.ForumCommentRepository;
import com.petguardian.forum.model.ForumCommentVO;
import com.petguardian.forum.model.ForumPostRepository;
import com.petguardian.forum.model.ForumPostVO;
import com.petguardian.member.model.Member;

@Service
public class ForumCommentService {

	@Autowired
	ForumCommentRepository repo;

	@Autowired
	ForumPostRepository postRepo;

	public List<ForumCommentVO> getCommentsByPostId(Integer postId) {
		return repo.findCommentsByPostId(postId);
	}

	public void addCommentByPostId(String commentContent, Integer postId, Integer memberId) {

		// ForumPostVO forumPostVO = new ForumPostVO();
		// ForumPostCommentVO forumPostCommentVO = new ForumPostCommentVO();
		//
		// forumPostVO.setPostId(postId);
		// forumPostCommentVO.setCommentContent(commentContent);
		// forumPostCommentVO.setForumPost(forumPostVO);
		//
		// repo.save(forumPostCommentVO);

		ForumPostVO forumPostVO = postRepo.findById(postId)
				.orElseThrow(() -> new RuntimeException("找不到該貼文，編號：" + postId));

		ForumCommentVO forumCommentVO = new ForumCommentVO();
		forumCommentVO.setCommentContent(commentContent);
		forumCommentVO.setForumPost(forumPostVO);

		// 使用傳入的 memberId
		Member member = new Member();
		member.setMemId(memberId);
		forumCommentVO.setMember(member);

		repo.save(forumCommentVO);

	}

}

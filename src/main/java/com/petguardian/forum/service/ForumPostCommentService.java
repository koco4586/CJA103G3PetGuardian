package com.petguardian.forum.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.forum.model.ForumPostCommentRepository;
import com.petguardian.forum.model.ForumPostCommentVO;
import com.petguardian.forum.model.ForumPostRepository;
import com.petguardian.forum.model.ForumPostVO;

@Service
public class ForumPostCommentService {
	
	@Autowired
	ForumPostCommentRepository repo;
	
	@Autowired
	ForumPostRepository postRepo;
	
	public List<ForumPostCommentVO> getCommentsByPostId(Integer postId) {
		return repo.findCommentsByPostId(postId);
	}
	
	public void addCommentByPostId(String commentContent,Integer postId) {
		
//		ForumPostVO forumPostVO = new ForumPostVO();
//		ForumPostCommentVO forumPostCommentVO = new ForumPostCommentVO();
//		
//		forumPostVO.setPostId(postId);
//		forumPostCommentVO.setCommentContent(commentContent);
//		forumPostCommentVO.setForumPost(forumPostVO);
//		
//		repo.save(forumPostCommentVO);
		
		ForumPostVO forumPostVO = postRepo.findById(postId)
				.orElseThrow(() -> new RuntimeException("找不到該貼文，編號：" + postId));
		
		ForumPostCommentVO forumPostCommentVO = new ForumPostCommentVO();
		forumPostCommentVO.setCommentContent(commentContent);
		forumPostCommentVO.setForumPost(forumPostVO);
		
		// 測試用
		forumPostCommentVO.setMemId(1015);
		
		repo.save(forumPostCommentVO);
		
	}
	
}

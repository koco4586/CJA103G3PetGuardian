package com.petguardian.forum.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.forum.model.DeletedCommentDTO;
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
	
	@Transactional
	public void deleteComment(Integer commentId) {
		ForumCommentVO forumCommentVO = repo.findById(commentId)
				.orElseThrow(() -> new RuntimeException("找不到該留言，編號：" + commentId));
		forumCommentVO.setCommentStatus(2);
		repo.save(forumCommentVO);
	}
	
	public ForumCommentVO getOneComment(Integer commentId) {
		ForumCommentVO forumCommentVO = repo.findById(commentId)
				.orElseThrow(() -> new RuntimeException("找不到該留言，編號：" + commentId));
		return forumCommentVO;
	}
	
	public List<ForumCommentVO> getCommentsByPostId(Integer postId) {
		return repo.findCommentsByPostId(postId);
	}

	public List<ForumCommentVO> getAllCommentsByPostId(Integer postId) {
		return repo.findAllCommentsByPostId(postId);
	}
	
	public List<DeletedCommentDTO> getAllDeletedComments() {
		return repo.findAllDeletedComments();
	}
	
	@Transactional
	public void addCommentByPostId(String commentContent, Integer postId, Integer memberId) {

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
	
	@Transactional
	public void updateCommentByPostId(String commentContent, Integer commentId, Integer memberId) {
		
		ForumCommentVO forumCommentVO = repo.findById(commentId)
				.orElseThrow(() -> new RuntimeException("找不到該留言，編號：" + commentId));
		
		forumCommentVO.setCommentContent(commentContent);
		
		// 使用傳入的 memberId
		Member member = new Member();
		member.setMemId(memberId);
		forumCommentVO.setMember(member);

		repo.save(forumCommentVO);
		
	}
	
}

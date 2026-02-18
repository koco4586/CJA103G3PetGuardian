package com.petguardian.forum.service;

import java.util.List;

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

	private final ForumCommentRepository repo;
	private final ForumPostRepository postRepo;
	
	public ForumCommentService(ForumCommentRepository repo, ForumPostRepository postRepo) {
		super();
		this.repo = repo;
		this.postRepo = postRepo;
	}

	@Transactional
	public void deleteComment(Integer commentId) {
		ForumCommentVO forumCommentVO = repo.findById(commentId)
				.orElseThrow(() -> new RuntimeException("找不到該留言，編號：" + commentId));
		forumCommentVO.setCommentStatus(2);
		
		if(forumCommentVO.getChildComments() != null) {
			for(ForumCommentVO childComment : forumCommentVO.getChildComments()) {
				if(childComment.getCommentStatus() == 2) {
					continue;
				}
				childComment.setCommentStatus(0);
			}
		}
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
	public void addCommentByPostId(String commentContent, Integer postId, Integer memId, Integer parentCommentId) {

		ForumPostVO forumPostVO = postRepo.findById(postId)
				.orElseThrow(() -> new RuntimeException("找不到該貼文，編號：" + postId));

		ForumCommentVO forumCommentVO = new ForumCommentVO();
		forumCommentVO.setCommentContent(commentContent);
		forumCommentVO.setForumPost(forumPostVO);
		
		if(parentCommentId != null) {
			ForumCommentVO parentComment = this.getOneComment(parentCommentId);
			forumCommentVO.setParentComment(parentComment);
		}
		
		// 使用傳入的 memId
		Member member = new Member();
		member.setMemId(memId);
		forumCommentVO.setMember(member);

		repo.save(forumCommentVO);

	}
	
	@Transactional
	public void updateCommentByPostId(String commentContent, Integer commentId, Integer memId) {
		
		ForumCommentVO forumCommentVO = repo.findById(commentId)
				.orElseThrow(() -> new RuntimeException("找不到該留言，編號：" + commentId));
		
		forumCommentVO.setCommentContent(commentContent);
		
		// 使用傳入的 memId
		Member member = new Member();
		member.setMemId(memId);
		forumCommentVO.setMember(member);

		repo.save(forumCommentVO);
		
	}
	
}

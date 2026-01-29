package com.petguardian.forum.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.forum.model.CommentHandledResultDetailDTO;
import com.petguardian.forum.model.CommentReviewDetailDTO;
import com.petguardian.forum.model.ForumCommentReportRepository;
import com.petguardian.forum.model.ForumCommentReportVO;
import com.petguardian.forum.model.ForumCommentRepository;
import com.petguardian.forum.model.ForumCommentVO;
import com.petguardian.forum.model.ForumPostRepository;
import com.petguardian.forum.model.ForumPostVO;
import com.petguardian.forum.model.HandledCommentDTO;
import com.petguardian.forum.model.PendingCommentDTO;
import com.petguardian.forum.model.RejectedCommentDTO;

@Service
public class ForumCommentReportService {
	
	@Autowired
	ForumCommentReportRepository repo;
	
	@Autowired
	ForumCommentRepository commentRepo;
	
	@Transactional
	public void addReport(ForumCommentReportVO forumCommentReportVO, Integer commentId) {
		ForumCommentVO forumCommentVO = commentRepo.findById(commentId)
				.orElseThrow(() -> new RuntimeException("找不到該留言，編號：" + commentId));
		
		forumCommentReportVO.setForumComment(forumCommentVO);
		repo.save(forumCommentReportVO);
		
	}
	
	public List<HandledCommentDTO> getAllHandledComments() {
		return repo.findAllHandledComments();
	}
	
	@Transactional
	public void recoverComment(Integer reportId, Integer commentId) {
		ForumCommentVO forumCommentVO = commentRepo.findById(commentId)
				.orElseThrow(() -> new RuntimeException("找不到該留言，編號：" + commentId));
		
		forumCommentVO.setCommentStatus(1);
		
		if(reportId != null) {
			ForumCommentReportVO forumCommentReportVO = repo.findById(reportId)
					.orElseThrow(() -> new RuntimeException("找不到該檢舉，編號：" + reportId));
			forumCommentReportVO.setReportStatus(2);
			forumCommentReportVO.setHandleResult("管理員已執行恢復操作。");
			forumCommentReportVO.setForumComment(forumCommentVO);
			repo.save(forumCommentReportVO);
		}
		commentRepo.save(forumCommentVO);
	}
	
	public List<PendingCommentDTO> getAllPendingComments() {
		return repo.findAllPendingComments();
	}
	
	public List<RejectedCommentDTO> getAllRejectedComments() {
		return repo.findAllRejectedComments();
	}
	
	public CommentReviewDetailDTO getCommentReviewDetailToHandle(Integer reportId) {
		return repo.commentReviewDetailToHandle(reportId);
	}
	
	public CommentHandledResultDetailDTO getCommentHandledResultDetailToDisplay(Integer reportId) {
		return repo.commentHandledResultDetailToDisplay(reportId);
	}
	
	@Transactional
	public void updateHandleResult(Integer reportId, Integer commentId, String handleResult) {
		
		ForumCommentVO forumCommentVO = commentRepo.findById(commentId)
				.orElseThrow(() -> new RuntimeException("找不到該留言，編號：" + commentId));
		
		ForumCommentReportVO forumCommentReportVO = repo.findById(reportId)
				.orElseThrow(() -> new RuntimeException("找不到該檢舉，編號：" + reportId));
	
		forumCommentVO.setCommentStatus(0);
		forumCommentReportVO.setReportStatus(1);
		forumCommentReportVO.setHandleResult(handleResult);
		forumCommentReportVO.setForumComment(forumCommentVO);
		
		repo.save(forumCommentReportVO);
		
	}
	
	@Transactional
	public void dismissCommentReport(Integer reportId, String handleResult) {
		
		ForumCommentReportVO forumCommentReportVO = repo.findById(reportId)
				.orElseThrow(() -> new RuntimeException("找不到該檢舉，編號：" + reportId));
	
		forumCommentReportVO.setReportStatus(2);
		forumCommentReportVO.setHandleResult(handleResult);
		
		repo.save(forumCommentReportVO);
		
	}
	
}

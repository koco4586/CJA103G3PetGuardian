package com.petguardian.forum.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.forum.model.ForumPostReportRepository;
import com.petguardian.forum.model.ForumPostReportVO;
import com.petguardian.forum.model.ForumPostRepository;
import com.petguardian.forum.model.ForumPostVO;
import com.petguardian.forum.model.HandledPostDTO;
import com.petguardian.forum.model.PostHandledResultDetailDTO;
import com.petguardian.forum.model.PendingPostDTO;
import com.petguardian.forum.model.PostReviewDetailDTO;
import com.petguardian.forum.model.RejectedPostDTO;

@Service
public class ForumPostReportService {
	
	private final ForumPostRepository postRepo;
	private final ForumPostReportRepository repo;
	
	public ForumPostReportService(ForumPostRepository postRepo, ForumPostReportRepository repo) {
		super();
		this.postRepo = postRepo;
		this.repo = repo;
	}

	@Transactional
	public void addReport(ForumPostReportVO forumPostReportVO, Integer postId) {
		ForumPostVO forumPostVO = postRepo.findById(postId)
				.orElseThrow(() -> new RuntimeException("找不到該貼文，編號：" + postId));
	
		forumPostReportVO.setForumPost(forumPostVO);
		repo.save(forumPostReportVO);
	
	}
	
	public List<HandledPostDTO> getAllHandledPosts() {
		return repo.findAllHandledPosts();
	}
	
	@Transactional
	public void recoverPost(Integer postId, Integer reportId) {
		ForumPostVO forumPostVO = postRepo.findById(postId)
				.orElseThrow(() -> new RuntimeException("找不到該貼文，編號：" + postId));
		
		forumPostVO.setPostStatus(1);
		
		if(reportId != null) {
			ForumPostReportVO forumPostReportVO = repo.findById(reportId)
					.orElseThrow(() -> new RuntimeException("找不到該檢舉，編號：" + reportId));
			forumPostReportVO.setReportStatus(2);
			forumPostReportVO.setHandleResult("管理員已執行恢復操作。");
			forumPostReportVO.setForumPost(forumPostVO);
			repo.save(forumPostReportVO);
		}
		postRepo.save(forumPostVO);
	}
	
	public List<PendingPostDTO> getAllPendingPosts() {
		return repo.findAllPendingPosts();
	}
	
	public List<RejectedPostDTO> getAllRejectedPosts() {
		return repo.findAllRejectedPosts();
	}
	
	public PostReviewDetailDTO getPostReviewDetailToHandle(Integer reportId) {
		return repo.postReviewDetailToHandle(reportId);
	}
	
	public PostHandledResultDetailDTO getPostHandledResultDetailToDisplay(Integer reportId) {
		return repo.postHandledResultDetailToDisplay(reportId);
	}
	
	@Transactional
	public void updateHandleResult(Integer reportId, Integer postId, String handleResult) {
		
		ForumPostVO forumPostVO = postRepo.findById(postId)
				.orElseThrow(() -> new RuntimeException("找不到該貼文，編號：" + postId));
		
		ForumPostReportVO forumPostReportVO = repo.findById(reportId)
				.orElseThrow(() -> new RuntimeException("找不到該檢舉，編號：" + reportId));
		
		forumPostVO.setPostStatus(0);
		forumPostReportVO.setReportStatus(1);
		forumPostReportVO.setHandleResult(handleResult);
		forumPostReportVO.setForumPost(forumPostVO);
		
		repo.save(forumPostReportVO);
	}
	
	@Transactional
	public void dismissPostReport(Integer reportId, String handleResult) {
		
		ForumPostReportVO forumPostReportVO = repo.findById(reportId)
				.orElseThrow(() -> new RuntimeException("找不到該檢舉，編號：" + reportId));
		
		forumPostReportVO.setReportStatus(2);
		forumPostReportVO.setHandleResult(handleResult);
		
		repo.save(forumPostReportVO);
	}
	
}

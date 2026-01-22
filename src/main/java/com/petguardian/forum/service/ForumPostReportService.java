package com.petguardian.forum.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.forum.model.ForumPostReportRepository;
import com.petguardian.forum.model.HandledPostDTO;
import com.petguardian.forum.model.PendingPostDTO;
import com.petguardian.forum.model.RejectedPostDTO;

@Service
public class ForumPostReportService {
	
	@Autowired
	ForumPostReportRepository repo;
	
	public List<HandledPostDTO> getAllHandledPosts() {
		return repo.findAllHandledPosts();
	}
	
	public List<PendingPostDTO> getAllPendingPosts() {
		return repo.findAllPendingPosts();
	}
	
	public List<RejectedPostDTO> getAllRejectedPosts() {
		return repo.findAllRejectedPosts();
	}
	
}

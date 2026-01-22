package com.petguardian.forum.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.forum.model.ForumCommentReportRepository;
import com.petguardian.forum.model.HandledCommentDTO;
import com.petguardian.forum.model.PendingCommentDTO;
import com.petguardian.forum.model.RejectedCommentDTO;

@Service
public class ForumCommentReportService {
	
	@Autowired
	ForumCommentReportRepository repo;
	
	public List<HandledCommentDTO> getAllHandledComments() {
		return repo.findAllHandledComments();
	}
	
	public List<PendingCommentDTO> getAllPendingComments() {
		return repo.findAllPendingComments();
	}
	
	public List<RejectedCommentDTO> getAllRejectedComments() {
		return repo.findAllRejectedComments();
	}
	
}

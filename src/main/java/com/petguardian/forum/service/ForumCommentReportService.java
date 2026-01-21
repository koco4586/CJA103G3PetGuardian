package com.petguardian.forum.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.forum.model.ForumCommentReportRepository;
import com.petguardian.forum.model.HandledCommentDTO;

@Service
public class ForumCommentReportService {
	
	@Autowired
	ForumCommentReportRepository repo;
	
	public List<HandledCommentDTO> getAllHandledComments() {
		return repo.findAllHandledComments();
	}
	
}

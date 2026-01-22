package com.petguardian.forum.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.forum.model.ForumPostReportRepository;
import com.petguardian.forum.model.HandledPostDTO;

@Service
public class ForumPostReportService {
	
	@Autowired
	ForumPostReportRepository repo;
	
	public List<HandledPostDTO> getAllHandledPosts() {
		return repo.findAllHandledPosts();
	}
	
}

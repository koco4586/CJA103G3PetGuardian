package com.petguardian.forum.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.petguardian.forum.model.ForumPostPicsRepository;

@Service
public class ForumPostPicsService {
		
	private final ForumPostPicsRepository repo;
	
	public ForumPostPicsService(ForumPostPicsRepository repo) {
		super();
		this.repo = repo;
	}

	public List<Integer> getPicsIdByPostId(Integer postId) {
		return repo.findPicsIdByPostId(postId);
	}
	
	public byte[] getPicByPicId(Integer picId) {
		return repo.findPicByPicId(picId);
	}

}

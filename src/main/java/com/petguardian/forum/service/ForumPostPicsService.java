package com.petguardian.forum.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.forum.model.ForumPostPicsRepository;

@Service
public class ForumPostPicsService {
	
	@Autowired
	ForumPostPicsRepository repo;
	
	public List<Integer> getPicsIdByPostId(Integer postId) {
		return repo.findPicsIdByPostId(postId);
	}
	
	public byte[] getPicByPicId(Integer picId) {
		return repo.findPicByPicId(picId);
	}

}

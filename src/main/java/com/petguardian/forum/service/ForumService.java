package com.petguardian.forum.service;

import com.petguardian.forum.model.ForumRepository;
import com.petguardian.forum.model.ForumVO;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ForumService {

	@Autowired
	ForumRepository repository;

	public void addForum(ForumVO forumVO) {
		repository.save(forumVO);
	}

	public void updateForum(ForumVO forumVO) {
		repository.save(forumVO);
	}

	public ForumVO getOneForum(Integer forumId) {
		Optional<ForumVO> optional = repository.findById(forumId);
		return optional.orElse(null);
	}

	public List<ForumVO> getAll() {
		return repository.findAll();
	}

	public void updateForumStatus(Integer forumStatus, Integer forumId) {
		Integer newStatus = (forumStatus == 1) ? 0 : 1;
		repository.updateStatus(newStatus, forumId);
	}

	public List<ForumVO> getForumByName(String forumName) {
		return repository.findByForumName(forumName);
	}

	public List<ForumVO> getAllActive() {
		return repository.getAllActive();
	}

	public byte[] getForumPic(Integer forumId) {
		return repository.getPicture(forumId);
	}

}

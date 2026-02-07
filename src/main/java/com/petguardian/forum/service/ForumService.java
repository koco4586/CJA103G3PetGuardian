package com.petguardian.forum.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.petguardian.forum.model.ForumRepository;
import com.petguardian.forum.model.ForumVO;

@Service
public class ForumService {
	
	private final ForumRepository repository;
	
	public ForumService(ForumRepository repository) {
		super();
		this.repository = repository;
	}

	public void addForum(ForumVO forumVO) {
		repository.save(forumVO);
	}
	
	public void updateForum(ForumVO forumVO) {
		repository.save(forumVO);
	}
	
	public ForumVO getOneForum(Integer forumId) {
		ForumVO forumVO = repository.findById(forumId)
				.orElseThrow(() -> new RuntimeException("找不到此討論區，編號：" + forumId));	
		return forumVO;
	}
	
	public List<ForumVO> getAll(){
		return repository.findAll();
	}
	
	public void updateForumStatus(Integer forumStatus, Integer forumId) {
		Integer newStatus = (forumStatus == 1) ? 0 : 1;
		repository.updateStatus(newStatus, forumId);	
	}
	
	public List<ForumVO> getForumByName(String forumName){
		return repository.findByForumName(forumName);
	}
	
	public List<ForumVO> getAllActive(){
		return repository.getAllActive();
	}
	
	public List<ForumVO> getTopHotForumsByForumIds(List<Integer> forumIds){
		
		List<ForumVO> forums = repository.findAllById(forumIds);
		
		Map<Integer, ForumVO> forumMap = forums.stream()
				.collect(Collectors.toMap(forum -> forum.getForumId(), forum -> forum));
		
		return forumIds.stream()
				.map(forumId -> {
					return forumMap.get(forumId);
				})
				.filter(forum -> forum != null)
				.collect(Collectors.toList());
	}
	
	public byte[] getForumPic(Integer forumId){
		return repository.getPicture(forumId);
	}
	
}

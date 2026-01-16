package com.forum.post.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ForumPostService {
	
	@Autowired
	ForumPostRepository repo;
	
	public void addPost(ForumPostVO forumPostVO) {
		repo.save(forumPostVO);
	}
	
	public void updatePost(ForumPostVO forumPostVO) {
		repo.save(forumPostVO);
	}
	
	public ForumPostVO getOnePost(Integer postId) {
		Optional<ForumPostVO> optional = repo.findById(postId);
		return optional.orElse(null);
	}
	
	public List<ForumPostVO> getAllActiveByForumId(Integer forumId){
		return repo.findPostsByForumId(forumId);
	}
	
	public List<ForumPostVO> getPostBykeyword(String keyword, Integer forumId){
		return repo.findByKeyword(keyword, forumId);
	}
	
	public List<ForumPostVO> getAllReportedPost(){
		return repo.getAllReportedPost();
	}
	
	public byte[] getPostPic(Integer postId) {
		return repo.getPicture(postId);
	}
	
}

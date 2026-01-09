package com.forum.model;

import java.util.List;

public interface ForumDAO_interface {
	
	public void insert(ForumVO forumVO);
	public ForumVO findByPrimaryKey(Integer forumId);
	public void update(ForumVO forumVO);
	public void updateStatus(ForumVO forumVO);
	public byte[] getPicture(Integer forumId);
	public List<ForumVO> searchByForumName(String forumName);
	public List<ForumVO> getAllActive();
	public List<ForumVO> getAll();
	
}

package com.forum.model;

import java.util.List;

public class ForumService {
	
	private ForumDAO_interface dao;
	
	public ForumService() {
		dao = new ForumJDBCDAO();
	}
	
	public ForumVO addForum(String forumName, byte[] forumPic){
		
		ForumVO forumVO = new ForumVO();
		
		forumVO.setForumName(forumName);
		forumVO.setForumPic(forumPic);
		
		dao.insert(forumVO);
		
		return forumVO;
	}
	
	public ForumVO updateForum(Integer forumId, String forumName, byte[] forumPic){
		
		ForumVO forumVO = new ForumVO();
		
		forumVO.setForumId(forumId);
		forumVO.setForumName(forumName);
		forumVO.setForumPic(forumPic);
		
		dao.update(forumVO);
		
		return forumVO;
	}
	
	public ForumVO disableForum(Integer forumId){
		
		ForumVO forumVO = new ForumVO();
		
		forumVO.setForumStatus(0);
		forumVO.setForumId(forumId);
		
		dao.updateStatus(forumVO);
		
		return forumVO;
	}
	
	public ForumVO activeForum(Integer forumId){
		
		ForumVO forumVO = new ForumVO();

		forumVO.setForumStatus(1);
		forumVO.setForumId(forumId);
		
		dao.updateStatus(forumVO);
		
		return forumVO;
	}
	
	public List<ForumVO> getForumByName(String forumName){
		return dao.searchByForumName(forumName);
	}
	
	public List<ForumVO> getAll(){	
		return dao.getAll();	
	}
	
	public List<ForumVO> getAllActive(){
		return dao.getAllActive();	
	}
	
	public ForumVO getOneForum(Integer forumId){
		return dao.findByPrimaryKey(forumId);
	}
	
	public byte[] getForumPic(Integer forumId){
		return dao.getPicture(forumId);
	}

}

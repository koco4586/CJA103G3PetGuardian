package com.petguardian.forum.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.petguardian.forum.model.ForumPostPicsRepository;
import com.petguardian.forum.model.ForumPostPicsVO;
import com.petguardian.forum.model.ForumPostRepository;
import com.petguardian.forum.model.ForumPostVO;

@Service
public class ForumPostService {
	
	@Autowired
	ForumPostRepository repo;
	
	@Autowired
	ForumPostPicsRepository picRepo;
	
	public void addPost(ForumPostVO forumPostVO) {
		repo.save(forumPostVO);
	}
	
	 			   // 開啟交易，確保文章跟圖片要一起成功commit，或一起失敗rollback，
	@Transactional // 避免髒資料，有文章沒圖片或有圖片卻沒有所屬的文章。
	public void addPostWithPics(ForumPostVO forumPostVO, MultipartFile[] postPics) throws IOException {
		// 1. 先儲存主貼文 (這是為了拿到 DB 自動產生的 postId)
		repo.save(forumPostVO);
		for (MultipartFile postPic : postPics) {
			// 每一張圖都要 new 一個新的物件
            ForumPostPicsVO forumPostPicsVO = new ForumPostPicsVO();
            forumPostPicsVO.setForumPost(forumPostVO); // 重點：設定關聯 (外鍵) = 對到哪篇文章
            byte[] pic = postPic.getBytes();
            forumPostPicsVO.setPic(pic);
            // 儲存到 (forumpostpicture) 表格
            picRepo.save(forumPostPicsVO);
		}
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
	
	public byte[] getPostPic(Integer postId) {
		return repo.getPicture(postId);
	}
	
}

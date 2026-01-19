package com.forum.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.forum.model.ForumPostService;
import com.forum.model.ForumPostVO;

@Controller
@RequestMapping("/forumpost")
public class ForumPostController {
	
	@Autowired
	ForumPostService forumPostService;
	
	@GetMapping("getForumIdForPosts")
	public String getForumIdForPosts(@RequestParam("forumId") Integer forumId, @RequestParam("forumName") String forumName, ModelMap model) {
		List<ForumPostVO> postList = forumPostService.getAllActiveByForumId(forumId);
		model.addAttribute("postList", postList);
		model.addAttribute("forumName", forumName);
		return "frontend/forum/listAllActivePost";
	}
	
	@GetMapping("getPostIdForOnePost")
	public String getPostIdForOnePost(@RequestParam("postId") Integer postId, ModelMap model) {
		
		// 開始查詢資料
		ForumPostVO forumPostVO = forumPostService.getOnePost(postId);
		
		// 查詢完成，交給負責的html顯示
		model.addAttribute(forumPostVO);
		return "frontend/forum/onePost";
	}
	
	
	
}

package com.petguardian.forum.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petguardian.forum.service.ForumService;
import com.petguardian.forum.service.RedisService;
import com.petguardian.forum.model.ForumVO;

@Controller
@RequestMapping("/forum")
public class ForumController {

	private final ForumService forumService;
	private final RedisService redisService;

	public ForumController(ForumService forumService, RedisService redisService) {
		super();
		this.forumService = forumService;
		this.redisService = redisService;
	}

	@GetMapping("list-all-active-forum")
	public String listAllActiveForum(ModelMap model) {
		
		List<ForumVO> forumList = forumService.getAllActive();
		List<Integer> forumIds = redisService.getTopHotForumIds(5);
		List<ForumVO> topHotForumList = forumService.getTopHotForumsByForumIds(forumIds);
		
		for(ForumVO forum : forumList) {
			Integer forumViewCount = redisService.getForumViewCount(forum.getForumId());
			if(forumViewCount != null) {
				forum.setForumViews(forumViewCount);
			}
		}
		
		model.addAttribute("topHotForumList", topHotForumList);
		model.addAttribute("forumList", forumList);
		
		return "frontend/forum/list-all-active-forum";
	}

	@GetMapping("get-forum-name-for-display")
	public String getForumNameForDisplay(@RequestParam("forumName") String forumName, ModelMap model) {

		List<Integer> forumIds = redisService.getTopHotForumIds(5);
		
		// 空字串驗證，沒輸入資料forward回原頁面
		if (forumName == null || forumName.trim().isEmpty()) {
			
			model.addAttribute("errorMsgs", "請輸入欲查詢的討論區名稱");
			model.addAttribute("forumList", forumService.getAllActive());
			model.addAttribute("topHotForumList", forumService.getTopHotForumsByForumIds(forumIds));
			
			return "frontend/forum/list-all-active-forum";
		}

		// 查詢討論區名稱
		List<ForumVO> forumList = forumService.getForumByName(forumName);

		// 查無資料，forward回原頁面
		if (forumList == null || forumList.isEmpty()) {
			
			model.addAttribute("errorMsgs", "查無相關討論區");
			model.addAttribute("forumList", forumService.getAllActive());
			model.addAttribute("topHotForumList", forumService.getTopHotForumsByForumIds(forumIds));
			
			return "frontend/forum/list-all-active-forum";
		}

		// 有資料，將資料放入model並forward至顯示頁面
		model.addAttribute("forumList", forumList);
		model.addAttribute("topHotForumList", forumService.getTopHotForumsByForumIds(forumIds));
		
		return "frontend/forum/list-all-active-forum";

	}

}

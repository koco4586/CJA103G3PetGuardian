package com.petguardian.forum.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petguardian.forum.service.ForumService;
import com.petguardian.forum.model.ForumVO;

@Controller
@RequestMapping("/forum")
public class ForumController {

	@Autowired
	ForumService forumService;

	@GetMapping("list-all-active-forum")
	public String listAllActiveForum(Model model) {
		List<ForumVO> forumList = forumService.getAllActive();
		model.addAttribute("forumList", forumList);
		return "frontend/forum/list-all-active-forum";
	}

	@GetMapping("get-forum-name-for-display")
	public String getForumNameForDisplay(@RequestParam("forumName") String forumName, ModelMap model) {

		// 空字串驗證，沒輸入資料forward回原頁面
		if (forumName == null || forumName.trim().isEmpty()) {
			model.addAttribute("errorMsgs", "請輸入欲查詢的討論區名稱");
			model.addAttribute("forumList", new ArrayList<ForumVO>(forumService.getAllActive()));
			return "frontend/forum/list-all-active-forum";
		}

		// 查詢討論區名稱
		List<ForumVO> forumList = forumService.getForumByName(forumName);

		// 查無資料，forward回原頁面
		if (forumList == null || forumList.isEmpty()) {
			model.addAttribute("errorMsgs", "查無相關討論區");
			model.addAttribute("forumList", forumService.getAllActive());
			return "frontend/forum/list-all-active-forum";
		}

		// 有資料，將資料放入model並forward至顯示頁面
		model.addAttribute("forumList", forumList);
		return "frontend/forum/list-all-active-forum";

	}

}

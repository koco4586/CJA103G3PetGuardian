package com.forum.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.forum.model.ForumService;
import com.forum.model.ForumVO;


@Controller
@RequestMapping("/admin/forum")
public class AdminForumController {
	
	@Autowired
	ForumService forumService;

	@GetMapping("listAllForum")
	public String listAllForum(Model model) {
		List<ForumVO> forumList = forumService.getAll();
		model.addAttribute("forumList", forumList);
		return "backend/forum/listAllForum";
	}
	
	
	@PostMapping("getForumIdForUpdateStatus")
	public String getForumIdForUpdateStatus(@RequestParam("forumStatus") Integer forumStatus,
											@RequestParam("forumId") Integer forumId, ModelMap model) {
		// 開始更新資料並重導到listAllForum
		forumService.updateForumStatus(forumStatus, forumId);
		return "redirect:/admin/forum/listAllForum";
		
	}
	
	
	
}

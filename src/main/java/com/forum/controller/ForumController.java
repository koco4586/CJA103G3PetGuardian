package com.forum.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.forum.model.ForumService;
import com.forum.model.ForumVO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/forum")
public class ForumController {
	
	@Autowired
	ForumService forumService;
	
	@PostMapping("getForumNameForDisplay")
	public String getForumNameForDisplay(@RequestParam("forumName") String forumName, ModelMap model) {
		
		// 空字串驗證，沒輸入資料forward回原頁面
		if(forumName == null || forumName.trim().isEmpty()) {
			model.addAttribute("errorMsgs", "請輸入欲查詢的討論區名稱");
			return "";
		}
		
		// 查詢討論區名稱
		List<ForumVO> forumList = forumService.getForumByName(forumName);
		
		// 查無資料，forward回原頁面
		if(forumList == null || forumList.isEmpty()) {
			model.addAttribute("errorMsgs", "查無相關的討論區");
			return "";
		}
		
		// 有資料，將資料放入model並forward至顯示頁面
		model.addAttribute("forumList", forumList);
		return "";
		
	}
	
	
}

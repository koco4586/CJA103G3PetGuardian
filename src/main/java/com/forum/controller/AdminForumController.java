package com.forum.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.forum.model.ForumService;
import com.forum.model.ForumVO;

import jakarta.validation.Valid;


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
		// 開始更新資料
		forumService.updateForumStatus(forumStatus, forumId);
		
		// 更新完成重導到listAllForum
		return "redirect:/admin/forum/listAllForum";
		        
	}
	
	@PostMapping("getOneForUpdate")
	public String getOneForUpdate(@RequestParam("forumId") Integer forumId, ModelMap model) {
		
		// 開始查詢資料
		ForumVO forumVO = forumService.getOneForum(forumId);
		
		// 查詢完成，交給負責更新的html
		model.addAttribute("forumVO", forumVO);
		return "backend/forum/updateForum";
	
	}
	
	@PostMapping("updateForum")
	public String updateForum(@Valid ForumVO forumVO, BindingResult result, ModelMap model,
							  @RequestParam("upFiles") MultipartFile upFiles) throws IOException {
		
		// Java Bean Validation 錯誤處理
		if(result.hasErrors()) {
			return "backend/forum/updateForum";
		}
		
		// MultipartFile convert byte[]
		if(upFiles != null && !upFiles.isEmpty()) {
			byte[] forumPic = upFiles.getBytes();
			forumVO.setForumPic(forumPic);
		}
		
		// 開始更新資料
		forumService.updateForum(forumVO);
		
		// 更新完成重導到listAllForum
		return "redirect:/admin/forum/listAllForum";
	
	}
	
	@PostMapping("insertForum")
	public String insertForum(@Valid ForumVO forumVO, BindingResult result, ModelMap model,
							  @RequestParam("upFiles") MultipartFile upFiles) throws IOException {
		
		// Java Bean Validation 錯誤處理
		if(result.hasErrors()) {
			return "backend/forum/addForum";
		}
		
		// MultipartFile convert byte[]
		if(upFiles != null && !upFiles.isEmpty()) {
			byte[] forumPic = upFiles.getBytes();
			forumVO.setForumPic(forumPic);
		}
		
		// 開始新增資料
		forumService.addForum(forumVO);
		
		// 新增完成重導到listAllForum
		return "redirect:/admin/forum/listAllForum";	
	
	}
	
}

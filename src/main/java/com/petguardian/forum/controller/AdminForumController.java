package com.petguardian.forum.controller;

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

import com.petguardian.forum.service.ForumService;
import com.petguardian.forum.model.ForumVO;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/forum")
public class AdminForumController {

	@Autowired
	ForumService forumService;

	@GetMapping("list-all-forum")
	public String listAllForum(Model model) {
		List<ForumVO> forumList = forumService.getAll();
		model.addAttribute("forumList", forumList);
		return "backend/forum/list-all-forum";
	}

	@GetMapping("add-forum")
	public String addForum(ModelMap model) {
		ForumVO forumVO = new ForumVO();
		model.addAttribute("forumVO", forumVO);
		return "backend/forum/add-forum";
	}

	@PostMapping("get-forum-id-for-update-status")
	public String getForumIdForUpdateStatus(@RequestParam("forumStatus") Integer forumStatus,
			@RequestParam("forumId") Integer forumId, ModelMap model) {
		// 開始更新資料
		forumService.updateForumStatus(forumStatus, forumId);

		// 更新完成重導到listAllForum
		return "redirect:/admin/forum/list-all-forum";

	}

	@PostMapping("get-one-for-update")
	public String getOneForUpdate(@RequestParam("forumId") Integer forumId, ModelMap model) {

		// 開始查詢資料
		ForumVO forumVO = forumService.getOneForum(forumId);

		// 查詢完成，交給負責更新的html
		model.addAttribute("forumVO", forumVO);
		return "backend/forum/update-forum";

	}

	@PostMapping("update-forum")
	public String updateForum(@Valid ForumVO forumVO, BindingResult result, ModelMap model) throws IOException {

		// Java Bean Validation 錯誤處理
		if (result.hasErrors()) {

			// 把ObjectError手動加到result (Vaild 找 beans是FieldError，方法層級驗證是 GlobalError)
			if (result.hasGlobalErrors()) {
				result.getGlobalErrors().forEach(error -> {
					result.rejectValue("upFiles", null, error.getDefaultMessage());
				});
			}
			return "backend/forum/update-forum";
		}

		// MultipartFile convert byte[]
		MultipartFile upFiles = forumVO.getUpFiles();
		if (upFiles != null && !upFiles.isEmpty()) {
			byte[] forumPic = upFiles.getBytes();
			forumVO.setForumPic(forumPic);
		} else {
			byte[] forumPic = forumService.getForumPic(forumVO.getForumId());
			forumVO.setForumPic(forumPic);
		}

		// 開始更新資料
		forumService.updateForum(forumVO);

		// 更新完成重導到listAllForum
		return "redirect:/admin/forum/list-all-forum";

	}

	@PostMapping("insert-forum")
	public String insertForum(@Valid ForumVO forumVO, BindingResult result, ModelMap model) throws IOException {

		// Java Bean Validation 錯誤處理
		if (result.hasErrors()) {

			// 把ObjectError手動加到result (Vaild 找 beans是FieldError，方法層級驗證是 GlobalError)
			if (result.hasGlobalErrors()) {
				result.getGlobalErrors().forEach(error -> {
					result.rejectValue("upFiles", null, error.getDefaultMessage());
				});
			}
			return "backend/forum/add-forum";
		}

		// MultipartFile convert byte[]
		MultipartFile upFiles = forumVO.getUpFiles();
		if (upFiles != null && !upFiles.isEmpty()) {
			byte[] forumPic = upFiles.getBytes();
			forumVO.setForumPic(forumPic);
			// } else {
			// byte[] forumPic = forumService.getForumPic(forumVO.getForumId());
			// forumVO.setForumPic(forumPic);
		}

		// 開始新增資料
		forumService.addForum(forumVO);

		// 新增完成重導到listAllForum
		return "redirect:/admin/forum/list-all-forum";

	}

}

package com.petguardian.forum.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petguardian.forum.service.ForumPostService;
import com.petguardian.forum.model.ForumPostVO;
import com.petguardian.forum.model.ForumVO;
import com.petguardian.forum.service.ForumService;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/forumpost")
public class ForumPostController {

	@Autowired
	ForumPostService forumPostService;

	@Autowired
	ForumService forumService;

	@GetMapping("get-forum-id-for-posts")
	public String getForumIdForPosts(@RequestParam("forumId") Integer forumId,
			@RequestParam("forumName") String forumName, ModelMap model) {
		List<ForumPostVO> postList = forumPostService.getAllActiveByForumId(forumId);
		model.addAttribute("postList", postList);
		model.addAttribute("forumName", forumName);
		return "frontend/forum/list-all-active-post";
	}

	@GetMapping("get-post-id-for-one-post")
	public String getPostIdForOnePost(@RequestParam("postId") Integer postId, ModelMap model) {

		// 開始查詢資料
		ForumPostVO forumPostVO = forumPostService.getOnePost(postId);

		// 查詢完成，交給負責的html顯示
		model.addAttribute(forumPostVO);
		return "frontend/forum/one-post";
	}

	@GetMapping("get-post-by-title")
	public String getPostByTitle(@RequestParam("postTitle") String postTitle, @RequestParam("forumId") Integer forumId,
			ModelMap model) {
		List<ForumPostVO> postList = forumPostService.getPostBykeyword(postTitle, forumId);

		if (postList == null || postList.isEmpty()) {
			model.addAttribute("errorMsgs", "查無相關貼文");
			postList = forumPostService.getAllActiveByForumId(forumId);
		}

		model.addAttribute("postList", postList);
		model.addAttribute("forumId", forumId);
		// 嘗試取得討論區名稱以維持頁面顯示
		ForumVO forumVO = forumService.getOneForum(forumId);
		if (forumVO != null) {
			model.addAttribute("forumName", forumVO.getForumName());
		}

		return "frontend/forum/list-all-active-post";
	}

	@GetMapping("add-post")
	public String addPost(@RequestParam("forumId") Integer forumId, ModelMap model) {
		ForumPostVO forumPostVO = new ForumPostVO();
		ForumVO forumVO = new ForumVO();
		forumVO.setForumId(forumId);
		forumPostVO.setForum(forumVO);

		model.addAttribute("forumPostVO", forumPostVO);
		return "frontend/forum/add-post";
	}

	@PostMapping("insert-post")
	public String insertPost(@Valid ForumPostVO forumPostVO, BindingResult result, ModelMap model) throws IOException {
		if (result.hasErrors()) {
			return "frontend/forum/add-post";
		}

		MultipartFile upFiles = forumPostVO.getUpFiles();
		if (upFiles != null && !upFiles.isEmpty()) {
			byte[] postPic = upFiles.getBytes();
			forumPostVO.setPostPic(postPic);
		}

		forumPostService.addPost(forumPostVO);

		Integer forumId = forumPostVO.getForum().getForumId();
		String forumName = "Forum";
		ForumVO forumVO = forumService.getOneForum(forumId);
		if (forumVO != null) {
			forumName = forumVO.getForumName();
		}

		return "redirect:/forumpost/get-forum-id-for-posts?forumId=" + forumId + "&forumName="
				+ java.net.URLEncoder.encode(forumName, "UTF-8");
	}

}

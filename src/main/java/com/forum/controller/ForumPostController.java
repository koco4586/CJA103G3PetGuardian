package com.forum.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.forum.model.ForumPostPicsService;
import com.forum.model.ForumPostService;
import com.forum.model.ForumPostVO;


@Controller
@RequestMapping("/forumpost")
public class ForumPostController {
	
	@Autowired
	ForumPostService forumPostService;
	
	@Autowired
	ForumPostPicsService forumPostPicsService;
	
	@GetMapping("getForumIdForPosts")
	public String getForumIdForPosts(@RequestParam("forumId") Integer forumId, ModelMap model) {
		List<ForumPostVO> postList = forumPostService.getAllActiveByForumId(forumId);
		model.addAttribute("postList", postList);
//		model.addAttribute("forumName", forumName);
//		model.addAttribute("forumId", forumId);
		return "frontend/forum/listAllActivePosts";
	}
	
	@GetMapping("getPostIdForOnePost")
	public String getPostIdForOnePost(@RequestParam("postId") Integer postId, ModelMap model) {
		
		// 開始查詢資料
		ForumPostVO forumPostVO = forumPostService.getOnePost(postId);
		List<Integer> picsId = forumPostPicsService.getPicsIdByPostId(postId);
		
		// 查詢完成，交給負責的html顯示
		model.addAttribute("forumPostVO", forumPostVO);
		model.addAttribute("picsId", picsId);
		return "frontend/forum/onePost";
	}
	
	@GetMapping("addPost")
	public String addForum(ModelMap model) {
		ForumPostVO forumPostVO = new ForumPostVO();
		model.addAttribute("forumPostVO", forumPostVO);
		return "frontend/forum/addPost";
	}
	
	
	
	@GetMapping("getKeywordForPosts")
	public String getKeywordForPosts(@RequestParam("keyword") String keyword, @RequestParam("forumId") Integer forumId, ModelMap model) {
		
		// 空字串驗證，沒輸入資料forward回原頁面
		if(keyword == null || keyword.trim().isEmpty()) {
			// 重要】搜尋完後，要記得再把 forumId 塞回去 model，否則下次搜尋時會報錯
//			model.addAttribute("forumId", forumId);
			model.addAttribute("errorMsgs", "請輸入欲查詢的內容");
			model.addAttribute("postList", new ArrayList<ForumPostVO>(forumPostService.getAllActiveByForumId(forumId)));
			return "frontend/forum/listAllActivePosts";
		}
		
		// 查詢討論區名稱
		List<ForumPostVO> postList = forumPostService.getPostBykeyword(keyword, forumId);
		
		// 查無資料，forward回原頁面
		if(postList == null || postList.isEmpty()) {
			//【重要】搜尋完後，要記得再把 forumId 塞回去 model，否則下次搜尋時會報錯
//			model.addAttribute("forumId", forumId);
			model.addAttribute("errorMsgs", "查無相關貼文");
			model.addAttribute("postList", new ArrayList<ForumPostVO>(forumPostService.getAllActiveByForumId(forumId)));
			return "frontend/forum/listAllActivePosts";
		}
		
		// 有資料，將資料放入model並forward至顯示頁面
		//【重要】搜尋完後，要記得再把 forumId 塞回去 model，否則下次搜尋時會報錯
//		model.addAttribute("forumId", forumId);
		model.addAttribute("postList", postList);
		return "frontend/forum/listAllActivePosts";
	}
	
	@ModelAttribute
	public void addAttribute(@RequestParam(value = "forumId", required = false) Integer forumId,
							 @RequestParam(value = "forumName", required = false) String forumName, ModelMap model) {
		// 只有當參數真的有傳過來時才存入 Model，避免存入 null
		if(forumId != null) {
			model.addAttribute("forumId", forumId);
		}
		if(forumName != null) {
			model.addAttribute("forumName", forumName);
		}
		
	}
	
	
	
}

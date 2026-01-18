package com.petguardian.forum.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.forum.service.ForumPostPicsService;
import com.petguardian.forum.service.ForumPostService;
import com.petguardian.forum.service.ForumService;

import jakarta.validation.Valid;

import com.petguardian.forum.model.ForumPostPicsVO;
import com.petguardian.forum.model.ForumPostVO;
import com.petguardian.forum.model.ForumVO;

@Controller
@RequestMapping("/forumpost")
public class ForumPostController {
	
	@Autowired
	ForumService forumService;
	
	@Autowired
	ForumPostService forumPostService;
	
	@Autowired
	ForumPostPicsService forumPostPicsService;
	
	@GetMapping("get-forum-id-for-posts")
	public String getForumIdForPosts(@RequestParam("forumId") Integer forumId, ModelMap model) {
		List<ForumPostVO> postList = forumPostService.getAllActiveByForumId(forumId);
		model.addAttribute("postList", postList);
//		model.addAttribute("forumName", forumName);
//		model.addAttribute("forumId", forumId);
		String forumName = forumService.getOneForum(forumId).getForumName();
		model.addAttribute("forumName", forumName);
		return "frontend/forum/list-all-active-posts";
	}
	
	@GetMapping("get-post-id-for-one-post")
	public String getPostIdForOnePost(@RequestParam("postId") Integer postId, ModelMap model) {
		
		// 開始查詢資料
		ForumPostVO forumPostVO = forumPostService.getOnePost(postId);
		List<Integer> picsId = forumPostPicsService.getPicsIdByPostId(postId);
		
		// 查詢完成，交給負責的html顯示
		model.addAttribute("forumPostVO", forumPostVO);
		model.addAttribute("picsId", picsId);
		return "frontend/forum/one-post";
	}
	
	@GetMapping("add-post")
	public String addPost(ModelMap model) {
		ForumPostVO forumPostVO = new ForumPostVO();
		
		// 從 Model 中取得剛才 @ModelAttribute 塞進去的 forumId
	    Integer forumId = (Integer) model.getAttribute("forumId");
	    
	    // 必須 new 一個物件，th:field 才有地方存資料
	    ForumVO forumVO = new ForumVO();
	    forumVO.setForumId(forumId);
	    forumPostVO.setForum(forumVO);
	    
		model.addAttribute("forumPostVO", forumPostVO);
		return "frontend/forum/add-post";
	}
	
	@PostMapping("insert-post")
	public String insertPost(@Valid ForumPostVO forumPostVO, BindingResult result, ForumPostPicsVO forumPostPicsVO, ModelMap model,
							@RequestParam("upFiles") MultipartFile[] postPics, RedirectAttributes ra) throws IOException {
		
		// Java Bean Validation 錯誤處理
		if(result.hasErrors()) {
			
			// 把ObjectError手動加到result (Vaild 找 beans是FieldError，方法層級驗證是 GlobalError)
			if(result.hasGlobalErrors()) {
				result.getGlobalErrors().forEach(error -> {
					result.rejectValue("upFile", null, error.getDefaultMessage());
				});
				
			}
			return "frontend/forum/add-post";
		}
		
		// MultipartFile convert byte[]
		MultipartFile upFile = forumPostVO.getUpFile();
		if(upFile != null && !upFile.isEmpty()) {
			byte[] mainPic = upFile.getBytes();
			forumPostVO.setPostPic(mainPic);
		}
		
		if(postPics != null && postPics.length > 0) {
			
			for(int i = 0; i < postPics.length; i++) {
				if(postPics[i] == null || postPics[i].isEmpty()) {
					continue;
				} else {
					String contentType = postPics[i].getContentType();
					if(contentType == null || !contentType.startsWith("image/")) {
						model.addAttribute("errorMsgs", "請上傳圖片檔（jpg, png, gif）");
						return "frontend/forum/add-post";
					}
					
				}
				
			}
			
			if(postPics.length > 6) {
				model.addAttribute("errorMsgs", "最多上傳6張圖片");
				return "frontend/forum/add-post";
			}
			
			long maxSize = 1 * 1024 *1024;
			long totalMaxSize = 5 * 1024 *1024;
			long upFilesTotalSize = 0;
			
			for(int i = 0; i < postPics.length; i++) {
				if(postPics[i].isEmpty()) {
					continue;
				}
				if(postPics[i].getSize() > maxSize) {
					model.addAttribute("errorMsgs", "單張圖片大小不得超過 1MB");
					return "frontend/forum/add-post";
					
				} else {
					upFilesTotalSize += postPics[i].getSize();
					if(upFilesTotalSize > totalMaxSize) {
						model.addAttribute("errorMsgs", "總上傳檔案大小不得超過 5MB");
						return "frontend/forum/add-post";
					}
					
				}
				
			}
			
		}
		
		forumPostVO.setMemId(1015); // 測試用
		
		// 沒圖片時 -> 新增資料
		if(postPics == null || postPics.length == 0) {
			forumPostService.addPost(forumPostVO);
			
			// 設定閃退訊息 (Flash Attribute)，重導向後會消失，不會重複出現
		    ra.addFlashAttribute("successMsgs", "🎉 貼文發表成功！");
			
		    // 新增完成重導到成功頁面
			Integer forumId = forumPostVO.getForum().getForumId();
			
			return "redirect:/forumpost/get-forum-id-for-posts?forumId=" + forumId;
		
		} else {
			// 有圖片時 -> 新增資料
			forumPostService.addPostWithPics(forumPostVO, postPics);			
			
			// 設定閃退訊息 (Flash Attribute)，重導向後會消失，不會重複出現
		    ra.addFlashAttribute("successMsgs", "🎉 貼文發表成功！");
			
			// 新增完成重導到成功頁面
			Integer forumId = forumPostVO.getForum().getForumId();
			
			return "redirect:/forumpost/get-forum-id-for-posts?forumId=" + forumId;
		}
		
	}
	
	@GetMapping("get-keyword-for-posts")
	public String getKeywordForPosts(@RequestParam("keyword") String keyword, @RequestParam("forumId") Integer forumId, ModelMap model) {
		
		// 空字串驗證，沒輸入資料forward回原頁面
		if(keyword == null || keyword.trim().isEmpty()) {
			// 重要】搜尋完後，要記得再把 forumId 塞回去 model，否則下次搜尋時會報錯
//			model.addAttribute("forumId", forumId);
			model.addAttribute("errorMsgs", "請輸入欲查詢的內容");
			model.addAttribute("postList", new ArrayList<ForumPostVO>(forumPostService.getAllActiveByForumId(forumId)));
			return "frontend/forum/list-all-active-posts";
		}
		
		// 查詢討論區名稱
		List<ForumPostVO> postList = forumPostService.getPostBykeyword(keyword, forumId);
		
		// 查無資料，forward回原頁面
		if(postList == null || postList.isEmpty()) {
			//【重要】搜尋完後，要記得再把 forumId 塞回去 model，否則下次搜尋時會報錯
//			model.addAttribute("forumId", forumId);
			model.addAttribute("errorMsgs", "查無相關貼文");
			model.addAttribute("postList", new ArrayList<ForumPostVO>(forumPostService.getAllActiveByForumId(forumId)));
			return "frontend/forum/list-all-active-posts";
		}
		
		// 有資料，將資料放入model並forward至顯示頁面
		//【重要】搜尋完後，要記得再把 forumId 塞回去 model，否則下次搜尋時會報錯
//		model.addAttribute("forumId", forumId);
		model.addAttribute("postList", postList);
		return "frontend/forum/list-all-active-posts";
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

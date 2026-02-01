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

import com.petguardian.forum.service.ForumCommentReportService;
import com.petguardian.forum.service.ForumCommentService;
import com.petguardian.forum.service.ForumPostPicsService;
import com.petguardian.forum.service.ForumPostReportService;
import com.petguardian.forum.service.ForumPostService;
import com.petguardian.forum.service.ForumService;
import com.petguardian.member.model.Member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.petguardian.forum.model.ForumCommentReportVO;
import com.petguardian.forum.model.ForumCommentVO;
import com.petguardian.forum.model.ForumPostPicsVO;
import com.petguardian.forum.model.ForumPostReportVO;
import com.petguardian.forum.model.ForumPostVO;
import com.petguardian.forum.model.ForumVO;

import com.petguardian.common.service.AuthStrategyService;

@Controller
@RequestMapping("/forumpost")
public class ForumPostController {

	@Autowired
	AuthStrategyService authStrategyService;

	@Autowired
	ForumService forumService;

	@Autowired
	ForumPostService forumPostService;

	@Autowired
	ForumCommentService forumCommentService;

	@Autowired
	ForumPostPicsService forumPostPicsService;

	@Autowired
	ForumPostReportService forumPostReportService;

	@Autowired
	ForumCommentReportService forumCommentReportService;

	@GetMapping("get-forum-id-for-posts")
	public String getForumIdForPosts(@RequestParam("forumId") Integer forumId, ModelMap model) {
		List<ForumPostVO> postList = forumPostService.getAllActiveByForumId(forumId);
		model.addAttribute("postList", postList);
		// model.addAttribute("forumName", forumName);
		// model.addAttribute("forumId", forumId);
		String forumName = forumService.getOneForum(forumId).getForumName();
		model.addAttribute("forumName", forumName);
		return "frontend/forum/list-all-active-posts";
	}

	@GetMapping("get-post-id-for-one-post")
	public String getPostIdForOnePost(@RequestParam("postId") Integer postId, ModelMap model) {

		ForumCommentVO forumCommentVO = new ForumCommentVO();

		// 開始查詢資料
		ForumPostVO forumPostVO = forumPostService.getOnePost(postId);
		List<Integer> picsId = forumPostPicsService.getPicsIdByPostId(postId);
		List<ForumCommentVO> commentList = forumCommentService.getCommentsByPostId(postId);

		// 查詢完成，交給負責的html顯示
		model.addAttribute("forumPostVO", forumPostVO);
		model.addAttribute("picsId", picsId);
		model.addAttribute("commentList", commentList);
		model.addAttribute("forumCommentVO", forumCommentVO);

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
	public String insertPost(@Valid ForumPostVO forumPostVO, BindingResult result, ForumPostPicsVO forumPostPicsVO,
			ModelMap model, @RequestParam("upFiles") MultipartFile[] postPics, RedirectAttributes ra,
			HttpServletRequest request) throws IOException {

		// Java Bean Validation 錯誤處理
		if (result.hasErrors()) {

			// 把ObjectError手動加到result (Vaild 找 beans是FieldError，方法層級驗證是 GlobalError)
			if (result.hasGlobalErrors()) {
				result.getGlobalErrors().forEach(error -> {
					result.rejectValue("upFile", null, error.getDefaultMessage());
				});

			}
			return "frontend/forum/add-post";
		}

		// MultipartFile convert byte[]
		MultipartFile upFile = forumPostVO.getUpFile();
		if (upFile != null && !upFile.isEmpty()) {
			byte[] mainPic = upFile.getBytes();
			forumPostVO.setPostPic(mainPic);
		}

		if (postPics != null && postPics.length > 0) {

			for (int i = 0; i < postPics.length; i++) {
				if (postPics[i] == null || postPics[i].isEmpty()) {
					continue;
				} else {
					String contentType = postPics[i].getContentType();
					if (contentType == null || !contentType.startsWith("image/")) {
						model.addAttribute("errorMsgs", "請上傳圖片檔（jpg, png, gif）");
						return "frontend/forum/add-post";
					}

				}

			}

			if (postPics.length > 6) {
				model.addAttribute("errorMsgs", "最多上傳6張圖片");
				return "frontend/forum/add-post";
			}

			long maxSize = 1 * 1024 * 1024;
			long totalMaxSize = 5 * 1024 * 1024;
			long upFilesTotalSize = 0;

			for (int i = 0; i < postPics.length; i++) {
				if (postPics[i].isEmpty()) {
					continue;
				}
				if (postPics[i].getSize() > maxSize) {
					model.addAttribute("errorMsgs", "單張圖片大小不得超過 1MB");
					return "frontend/forum/add-post";

				} else {
					upFilesTotalSize += postPics[i].getSize();
					if (upFilesTotalSize > totalMaxSize) {
						model.addAttribute("errorMsgs", "總上傳檔案大小不得超過 5MB");
						return "frontend/forum/add-post";
					}

				}

			}

		}

		// 使用 AuthStrategyService 取得當前使用者
//		Integer userId = authStrategyService.getCurrentUserId(request);
//		if (userId == null) {
//			model.addAttribute("errorMsgs", "請先登入後再發表文章");
//			return "frontend/forum/add-post";
//			
//		}

		Member member = new Member();
		member.setMemId(1005);
		forumPostVO.setMember(member);

		// 沒圖片時 -> 新增資料
		if (postPics == null || postPics.length == 0 || postPics[0].isEmpty()) {
			forumPostService.addPost(forumPostVO);

			// 設定閃退訊息 (Flash Attribute)，重導向後會消失，不會重複出現
			ra.addFlashAttribute("successMsgs", "🎉 貼文發表成功！");

			// 新增完成重導到該討論區列表頁面
			Integer forumId = forumPostVO.getForum().getForumId();

			return "redirect:/forumpost/get-forum-id-for-posts?forumId=" + forumId;

		} else {
			// 有圖片時 -> 新增資料
			forumPostService.addPostWithPics(forumPostVO, postPics);

			// 設定閃退訊息 (Flash Attribute)，重導向後會消失，不會重複出現
			ra.addFlashAttribute("successMsgs", "🎉 貼文發表成功！");

			// 新增完成重導到該討論區列表頁面
			Integer forumId = forumPostVO.getForum().getForumId();

			return "redirect:/forumpost/get-forum-id-for-posts?forumId=" + forumId;
		}

	}

	@GetMapping("update-post")
	public String updatePost(@RequestParam("postId") Integer postId, ModelMap model) {

		ForumPostVO forumPostVO = forumPostService.getOnePost(postId);
		List<Integer> picsId = forumPostPicsService.getPicsIdByPostId(postId);

		// 從 Model 中取得剛才 @ModelAttribute 塞進去的 forumId
		Integer forumId = (Integer) model.getAttribute("forumId");
		String forumName = forumService.getOneForum(forumId).getForumName();

		model.addAttribute("picsId", picsId);
		model.addAttribute("forumPostVO", forumPostVO);
		model.addAttribute("forumName", forumName);

		return "frontend/forum/update-post";
	}

	@PostMapping("update-post-submit")
	public String updatePostSubmit(@Valid ForumPostVO forumPostVO, BindingResult result,
			ForumPostPicsVO forumPostPicsVO, ModelMap model, @RequestParam("upFiles") MultipartFile[] postPics,
			@RequestParam("forumId") Integer forumId, @RequestParam("forumName") String forumName,
			RedirectAttributes ra, HttpServletRequest request) throws IOException {

		Integer postId = forumPostVO.getPostId();
		List<Integer> picsId = forumPostPicsService.getPicsIdByPostId(postId);

		// Java Bean Validation 錯誤處理
		if (result.hasErrors()) {

			// 把ObjectError手動加到result (Vaild 找 beans是FieldError，方法層級驗證是 GlobalError)
			if (result.hasGlobalErrors()) {
				result.getGlobalErrors().forEach(error -> {
					result.rejectValue("upFile", null, error.getDefaultMessage());
				});

			}
			model.addAttribute("picsId", picsId);
			return "frontend/forum/update-post";
		}

		// MultipartFile convert byte[]
		MultipartFile upFile = forumPostVO.getUpFile();
		if (upFile != null && !upFile.isEmpty()) {
			byte[] mainPic = upFile.getBytes();
			forumPostVO.setPostPic(mainPic);
		} else {
			byte[] mainPic = forumPostService.getPostPic(postId);
			forumPostVO.setPostPic(mainPic);
		}

		if (postPics != null && postPics.length > 0) {

			for (int i = 0; i < postPics.length; i++) {
				if (postPics[i] == null || postPics[i].isEmpty()) {
					continue;
				} else {
					String contentType = postPics[i].getContentType();
					if (contentType == null || !contentType.startsWith("image/")) {
						model.addAttribute("errorMsgs", "請上傳圖片檔（jpg, png, gif）");
						model.addAttribute("picsId", picsId);
						return "frontend/forum/update-post";
					}

				}

			}

			if (postPics.length > 6) {
				model.addAttribute("errorMsgs", "最多上傳6張圖片");
				model.addAttribute("picsId", picsId);
				return "frontend/forum/update-post";
			}

			long maxSize = 1 * 1024 * 1024;
			long totalMaxSize = 5 * 1024 * 1024;
			long upFilesTotalSize = 0;

			for (int i = 0; i < postPics.length; i++) {
				if (postPics[i].isEmpty()) {
					continue;
				}
				if (postPics[i].getSize() > maxSize) {
					model.addAttribute("errorMsgs", "單張圖片大小不得超過 1MB");
					model.addAttribute("picsId", picsId);
					return "frontend/forum/update-post";

				} else {
					upFilesTotalSize += postPics[i].getSize();
					if (upFilesTotalSize > totalMaxSize) {
						model.addAttribute("errorMsgs", "總上傳檔案大小不得超過 5MB");
						model.addAttribute("picsId", picsId);
						return "frontend/forum/update-post";
					}

				}

			}

		}

		// 使用 AuthStrategyService 取得當前使用者
//		Integer userId = authStrategyService.getCurrentUserId(request);
//		if (userId == null) {
//			model.addAttribute("errorMsgs", "請先登入後再發表文章");
//			return "frontend/forum/add-post";
//			
//		}

		Member member = new Member();
		member.setMemId(1005);
		forumPostVO.setMember(member);

		// 沒圖片
		if (postPics == null || postPics.length == 0 || postPics[0].isEmpty()) {
			forumPostService.updatePost(forumPostVO);

			// 設定閃退訊息 (Flash Attribute)，重導向後會消失，不會重複出現
			ra.addFlashAttribute("successMsgs", "🎉 貼文修改成功！");
			ra.addAttribute("forumId", forumId);
			ra.addAttribute("forumName", forumName);

			return "redirect:/forumpost/get-post-id-for-one-post?postId=" + postId;

		} else {
			// 有圖片
			forumPostService.updatePostWithPics(forumPostVO, postPics);

			// 設定閃退訊息 (Flash Attribute)，重導向後會消失，不會重複出現
			ra.addFlashAttribute("successMsgs", "🎉 貼文修改成功！");
			ra.addAttribute("forumId", forumId);
			ra.addAttribute("forumName", forumName);

			return "redirect:/forumpost/get-post-id-for-one-post?postId=" + postId;
		}

	}

	@PostMapping("insert-comment")
	public String insertComment(@Valid ForumCommentVO forumCommentVO, BindingResult result, ModelMap model,
			RedirectAttributes ra, @RequestParam("commentContent") String commentContent,
			@RequestParam("postId") Integer postId, @RequestParam("forumId") Integer forumId,
			@RequestParam("forumName") String forumName, HttpServletRequest request) {

		// Java Bean Validation 錯誤處理
		if (result.hasErrors()) {

			ForumPostVO forumPostVO = forumPostService.getOnePost(postId);
			List<ForumCommentVO> commentList = forumCommentService.getCommentsByPostId(postId);
			List<Integer> picsId = forumPostPicsService.getPicsIdByPostId(postId);
			model.addAttribute("forumPostVO", forumPostVO);
			model.addAttribute("commentList", commentList);
			model.addAttribute("picsId", picsId);

			return "frontend/forum/one-post";
		}

		// 使用 AuthStrategyService 取得當前使用者	
//		Integer userId = authStrategyService.getCurrentUserId(request);
//		if (userId == null) {
//			// 若未登入，這裡暫時將錯誤塞回並重導 (或視需求調整)
//			ra.addFlashAttribute("errorMsgs", "請先登入後再留言");
//			ra.addAttribute("forumName", forumName);
//			ra.addAttribute("forumId", forumId);
//			return "redirect:/forumpost/get-post-id-for-one-post?postId=" + postId;
//		}

		// 開始新增資料
		forumCommentService.addCommentByPostId(commentContent, postId, 1005);

		// 重導會拿不到資料，因為有返回按鈕，所以要用RedirectAttributes把資料塞回去。
		ra.addAttribute("forumName", forumName);
		ra.addAttribute("forumId", forumId);
		ra.addFlashAttribute("successMsgs", "🎉 留言新增完成");

		// 新增完成重導到該貼文頁面
		return "redirect:/forumpost/get-post-id-for-one-post?postId=" + postId;
	}

	@PostMapping("update-comment-submit")
	public String updateCommentSubmit(@Valid ForumCommentVO forumCommentVO, BindingResult result, RedirectAttributes ra,
			ModelMap model, @RequestParam("forumId") Integer forumId, @RequestParam("commentId") Integer commentId,
			@RequestParam("commentContent") String commentContent, @RequestParam("postId") Integer postId, HttpServletRequest request) {

		String forumName = forumService.getOneForum(forumId).getForumName();
		
		if (result.hasErrors()) {
			
			ForumPostVO forumPostVO = forumPostService.getOnePost(postId);
			List<ForumCommentVO> commentList = forumCommentService.getCommentsByPostId(postId);
			List<Integer> picsId = forumPostPicsService.getPicsIdByPostId(postId);
			model.addAttribute("forumPostVO", forumPostVO);
			model.addAttribute("commentList", commentList);
			model.addAttribute("picsId", picsId);
			model.addAttribute("forumName", forumName);
			
			return "frontend/forum/one-post";
		}

		// 使用 AuthStrategyService 取得當前使用者
//		Integer userId = authStrategyService.getCurrentUserId(request);
//		if (userId == null) {
//			// 若未登入，這裡暫時將錯誤塞回並重導 (或視需求調整)
//			ra.addFlashAttribute("errorMsgs", "請先登入後再留言");
//			ra.addAttribute("forumName", forumName);
//			ra.addAttribute("forumId", forumId);
//			return "redirect:/forumpost/get-post-id-for-one-post?postId=" + postId;
//		}
		
		forumCommentService.updateCommentByPostId(commentContent, commentId, 1005);
		
		ra.addAttribute("forumName", forumName);
		ra.addAttribute("forumId", forumId);
		ra.addFlashAttribute("successMsgs", "🎉 留言修改完成");
		
		return "redirect:/forumpost/get-post-id-for-one-post?postId=" + postId;
	}

	@GetMapping("get-keyword-for-posts")
	public String getKeywordForPosts(@RequestParam("keyword") String keyword, @RequestParam("forumId") Integer forumId,
			ModelMap model) {

		// 空字串驗證，沒輸入資料forward回原頁面
		if (keyword == null || keyword.trim().isEmpty()) {
			// 重要】搜尋完後，要記得再把 forumId 塞回去 model，否則下次搜尋時會報錯
			// model.addAttribute("forumId", forumId);
			model.addAttribute("errorMsgs", "請輸入欲查詢的內容");
			model.addAttribute("postList", new ArrayList<ForumPostVO>(forumPostService.getAllActiveByForumId(forumId)));
			return "frontend/forum/list-all-active-posts";
		}

		// 查詢討論區名稱
		List<ForumPostVO> postList = forumPostService.getPostBykeyword(keyword, forumId);

		// 查無資料，forward回原頁面
		if (postList == null || postList.isEmpty()) {
			// 【重要】搜尋完後，要記得再把 forumId 塞回去 model，否則下次搜尋時會報錯
			// model.addAttribute("forumId", forumId);
			model.addAttribute("errorMsgs", "查無相關貼文");
			model.addAttribute("postList", new ArrayList<ForumPostVO>(forumPostService.getAllActiveByForumId(forumId)));
			return "frontend/forum/list-all-active-posts";
		}

		// 有資料，將資料放入model並forward至顯示頁面
		// 【重要】搜尋完後，要記得再把 forumId 塞回去 model，否則下次搜尋時會報錯
		// model.addAttribute("forumId", forumId);
		model.addAttribute("postList", postList);
		return "frontend/forum/list-all-active-posts";
	}

	@GetMapping("report-post")
	public String reportPost(@RequestParam("postId") Integer postId, ModelMap model) {

		model.addAttribute("postId", postId);
		model.addAttribute("forumPostReportVO", new ForumPostReportVO());

		return "frontend/forum/report-post";

	}

	@PostMapping("report-post-submit")
	public String reportPostSubmit(@Valid ForumPostReportVO forumPostReportVO, BindingResult result,
			@RequestParam("postId") Integer postId, RedirectAttributes ra, ModelMap model) {

		if (result.hasErrors()) {
			model.addAttribute("postId", postId);
			return "frontend/forum/report-post";
		}

		Member member = new Member();
		member.setMemId(1005);
		forumPostReportVO.setMember(member);

		forumPostReportService.addReport(forumPostReportVO, postId);
		ra.addFlashAttribute("successMsgs", "檢舉成功，感謝您的回報");
		Integer forumId = forumPostReportVO.getForumPost().getForum().getForumId();

		return "redirect:/forumpost/get-forum-id-for-posts?forumId=" + forumId;
	}

	@GetMapping("report-comment")
	public String reportComment(@RequestParam("commentId") Integer commentId, ModelMap model) {

		model.addAttribute("forumCommentReportVO", new ForumCommentReportVO());
		model.addAttribute("commentId", commentId);

		return "frontend/forum/report-comment";

	}

	@PostMapping("report-comment-submit")
	public String reportCommentSubmit(@Valid ForumCommentReportVO forumCommentReportVO, BindingResult result,
			@RequestParam("commentId") Integer commentId, RedirectAttributes ra, ModelMap model) {

		if (result.hasErrors()) {
			model.addAttribute("commentId", commentId);
			return "frontend/forum/report-comment";
		}

		Member member = new Member();
		member.setMemId(1005);
		forumCommentReportVO.setMember(member);

		forumCommentReportService.addReport(forumCommentReportVO, commentId);
		ra.addFlashAttribute("successMsgs", "檢舉成功，感謝您的回報");
		Integer forumId = forumCommentReportVO.getForumComment().getForumPost().getForum().getForumId();

		return "redirect:/forumpost/get-forum-id-for-posts?forumId=" + forumId;
	}

	@GetMapping("delete-post")
	public String deletePost(@RequestParam("postId") Integer postId, @RequestParam("forumId") Integer forumId,
			RedirectAttributes ra) {

		forumPostService.deletePost(postId);
		ra.addFlashAttribute("successMsgs", "貼文刪除成功");

		return "redirect:/forumpost/get-forum-id-for-posts?forumId=" + forumId;
	}

	@GetMapping("delete-comment")
	public String deleteComment(@RequestParam("commentId") Integer commentId, @RequestParam("postId") Integer postId,
			@RequestParam("forumId") Integer forumId, RedirectAttributes ra) {

		forumCommentService.deleteComment(commentId);
		String forumName = forumService.getOneForum(forumId).getForumName();

		ra.addAttribute("forumName", forumName);
		ra.addAttribute("forumId", forumId);
		ra.addAttribute("postId", postId);
		ra.addFlashAttribute("successMsgs", "留言刪除成功");

		return "redirect:/forumpost/get-post-id-for-one-post";
	}
	
	@GetMapping("post-collection")
	public String postCollection(ModelMap model, HttpServletRequest request) {
		
//		使用 AuthStrategyService 取得當前使用者
		Integer userId = authStrategyService.getCurrentUserId(request);
		model.addAttribute("collectionList", new ArrayList<ForumPostVO>(forumPostService.getAllPostCollectionsByMemId(userId)));
		
		return "frontend/forum/post-collection";
	}

	@ModelAttribute
	public void addAttribute(@RequestParam(value = "forumId", required = false) Integer forumId,
			@RequestParam(value = "forumName", required = false) String forumName, ModelMap model) {
		// 只有當參數真的有傳過來時才存入 Model，避免存入 null
		if (forumId != null) {
			model.addAttribute("forumId", forumId);
		}
		if (forumName != null) {
			model.addAttribute("forumName", forumName);
		}

	}

}

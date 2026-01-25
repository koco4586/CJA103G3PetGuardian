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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.forum.service.ForumCommentReportService;
import com.petguardian.forum.service.ForumCommentService;
import com.petguardian.forum.service.ForumPostPicsService;
import com.petguardian.forum.service.ForumPostReportService;
import com.petguardian.forum.service.ForumPostService;
import com.petguardian.forum.service.ForumService;
import com.petguardian.forum.model.DeletedCommentDTO;
import com.petguardian.forum.model.DeletedPostDTO;
import com.petguardian.forum.model.ForumPostReportVO;
import com.petguardian.forum.model.ForumPostVO;
import com.petguardian.forum.model.ForumVO;
import com.petguardian.forum.model.HandledCommentDTO;
import com.petguardian.forum.model.HandledPostDTO;
import com.petguardian.forum.model.PendingCommentDTO;
import com.petguardian.forum.model.PendingPostDTO;
import com.petguardian.forum.model.PostHandledResultDetailDTO;
import com.petguardian.forum.model.PostReviewDetailDTO;
import com.petguardian.forum.model.RejectedCommentDTO;
import com.petguardian.forum.model.RejectedPostDTO;

import com.petguardian.common.service.AuthStrategyService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/forum")
public class AdminForumController {

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
					result.rejectValue("upFile", null, error.getDefaultMessage());
				});
			}
			return "backend/forum/update-forum";
		}

		// MultipartFile convert byte[]
		MultipartFile upFile = forumVO.getUpFile();
		if (upFile != null && !upFile.isEmpty()) {
			byte[] forumPic = upFile.getBytes();
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
					result.rejectValue("upFile", null, error.getDefaultMessage());
				});
			}
			return "backend/forum/add-forum";
		}

		// MultipartFile convert byte[]
		MultipartFile upFile = forumVO.getUpFile();
		if (upFile != null && !upFile.isEmpty()) {
			byte[] forumPic = upFile.getBytes();
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

	@GetMapping("get-all-handled-posts")
	public String getAllHandledPosts(ModelMap model) {

		// 開始查詢資料
		List<HandledPostDTO> postList = forumPostReportService.getAllHandledPosts();

		// 查詢完成forward到顯示頁面
		model.addAttribute("postList", postList);

		return "backend/forum/forum-post";

	}
	
	@PostMapping("get-one-handled-post-to-display")
	public String getOneHandledPostToDisplay(@RequestParam("reportId") Integer reportId, @RequestParam("postId") Integer postId, ModelMap model) {
		
		// 開始查詢資料
		PostReviewDetailDTO reviewDto = forumPostReportService.getPostReviewDetailToHandle(reportId);
		PostHandledResultDetailDTO handledResultDto = forumPostReportService.getPostHandledResultDetailToDisplay(reportId);
		List<Integer> picsId = forumPostPicsService.getPicsIdByPostId(postId);
		
		// 查詢完成forward到顯示頁面
		model.addAttribute("handledResultDto", handledResultDto);
		model.addAttribute("reviewDto", reviewDto);
		model.addAttribute("picsId", picsId);
		
		return "backend/forum/display-handled-post";
	}
	
	@PostMapping("get-one-handled-post-to-recover")
	public String getOneHandledPostToRecover(@RequestParam("reportId") Integer reportId, @RequestParam("postId") Integer postId, RedirectAttributes ra) {
		
		forumPostReportService.recoverPost(postId, reportId);
		ra.addFlashAttribute("successMsgs", "貼文已成功恢復");
		
		return "redirect:/admin/forum/get-all-handled-posts";
	}
	
	@GetMapping("get-all-pending-posts")
	public String getAllPendingPosts(ModelMap model) {
		
		// 開始查詢資料
		List<PendingPostDTO> postList = forumPostReportService.getAllPendingPosts();
		
		// 查詢完成forward到顯示頁面
		model.addAttribute("postList", postList);
		
		return "backend/forum/forum-pending-post";
	
	}
	
	@PostMapping("get-one-pending-post-to-handle")
	public String getOnePendingPostToHandle(@RequestParam("reportId") Integer reportId, @RequestParam("postId") Integer postId, ModelMap model) {
		
		// 開始查詢資料
		PostReviewDetailDTO dto = forumPostReportService.getPostReviewDetailToHandle(reportId);
		List<Integer> picsId = forumPostPicsService.getPicsIdByPostId(postId);
		
		// 查詢完成forward到顯示頁面
		model.addAttribute("dto", dto);
		model.addAttribute("picsId", picsId);
		model.addAttribute("forumPostReportVO", new ForumPostReportVO());
		
		return "backend/forum/handle-pending-post";
	}
	
	@PostMapping("handle-post-report")
	public String handlePostReport(@Valid ForumPostReportVO forumPostReportVO, BindingResult result,
								   @RequestParam("reportId") Integer reportId, @RequestParam("postId") Integer postId, 
			                       @RequestParam("handleResult") String handleResult, @RequestParam("action") String action, 
			                       RedirectAttributes ra, ModelMap model) {
		
		if(result.hasErrors()) {
			result.getAllErrors().forEach(error -> System.out.println("驗證錯誤: " + error.getDefaultMessage()));
			PostReviewDetailDTO dto = forumPostReportService.getPostReviewDetailToHandle(reportId);
			List<Integer> picsId = forumPostPicsService.getPicsIdByPostId(postId);
			model.addAttribute("dto", dto);
			model.addAttribute("picsId", picsId);
			return "backend/forum/handle-pending-post";
		}
		
		if("delete".equals(action)) {
			forumPostReportService.updateHandleResult(reportId, postId, handleResult);
			ra.addFlashAttribute("successMsgs", "貼文已下架");
			return "redirect:/admin/forum/get-all-pending-posts";
		}
		
		forumPostReportService.dismissPostReport(reportId, handleResult);
		ra.addFlashAttribute("successMsgs", "檢舉已駁回");
		return "redirect:/admin/forum/get-all-pending-posts";
	}
	
	
	@GetMapping("get-all-rejected-posts")
	public String getAllRejectedPosts(ModelMap model) {
		
		// 開始查詢資料
		List<RejectedPostDTO> postList = forumPostReportService.getAllRejectedPosts();
		
		// 查詢完成forward到顯示頁面
		model.addAttribute("postList", postList);
		
		return "backend/forum/forum-rejected-post";
	
	}
	
	@PostMapping("get-one-rejected-post-to-display")
	public String getOneRejectedPostToDisplay(@RequestParam("reportId") Integer reportId, @RequestParam("postId") Integer postId, ModelMap model) {
		
		// 開始查詢資料
		PostReviewDetailDTO reviewDto = forumPostReportService.getPostReviewDetailToHandle(reportId);
		PostHandledResultDetailDTO handledResultDto = forumPostReportService.getPostHandledResultDetailToDisplay(reportId);
		List<Integer> picsId = forumPostPicsService.getPicsIdByPostId(postId);
		
		// 查詢完成forward到顯示頁面
		model.addAttribute("handledResultDto", handledResultDto);
		model.addAttribute("reviewDto", reviewDto);
		model.addAttribute("picsId", picsId);
		
		return "backend/forum/display-rejected-post";
	}
	
	@GetMapping("get-all-deleted-posts")
	public String getAllDeletedPosts(ModelMap model) {
		
		// 開始查詢資料
		List<DeletedPostDTO> postList = forumPostService.getAllDeletedPosts();
		
		// 查詢完成forward到顯示頁面
		model.addAttribute("postList", postList);
		
		return "backend/forum/forum-deleted-post";
	
	}
	
	@PostMapping("get-one-deleted-post-to-display")
	public String getOneDeletedPostToDisplay(@RequestParam("postId") Integer postId, ModelMap model) {
		
		ForumPostVO forumPostVO = forumPostService.getOnePost(postId);
		List<Integer> picsId = forumPostPicsService.getPicsIdByPostId(postId);
		
		model.addAttribute("forumPostVO", forumPostVO);
		model.addAttribute("picsId", picsId);
		
		return "backend/forum/display-deleted-post";
	}
	
	@PostMapping("get-one-deleted-post-to-recover")
	public String getOneDeletedPostToRecover(@RequestParam("postId") Integer postId, RedirectAttributes ra) {
		
		forumPostReportService.recoverPost(postId, null);
		ra.addFlashAttribute("successMsgs", "貼文已成功恢復");
		
		return "redirect:/admin/forum/get-all-deleted-posts";
	}
	
	@GetMapping("get-all-handled-comments")
	public String getAllHandledComments(ModelMap model) {

		// 開始查詢資料
		List<HandledCommentDTO> commentList = forumCommentReportService.getAllHandledComments();

		// 查詢完成forward到顯示頁面
		model.addAttribute("commentList", commentList);

		return "backend/forum/forum-comment";

	}
	
	@GetMapping("get-all-pending-comments")
	public String getAllPendingComments(ModelMap model) {
		
		// 開始查詢資料
		List<PendingCommentDTO> commentList = forumCommentReportService.getAllPendingComments();
		
		// 查詢完成forward到顯示頁面
		model.addAttribute("commentList", commentList);
		
		return "backend/forum/forum-pending-comment";
		
	}
	
	@GetMapping("get-all-rejected-comments")
	public String getAllRejectedComments(ModelMap model) {
		
		// 開始查詢資料
		List<RejectedCommentDTO> commentList = forumCommentReportService.getAllRejectedComments();
		
		// 查詢完成forward到顯示頁面
		model.addAttribute("commentList", commentList);
		
		return "backend/forum/forum-rejected-comment";
		
	}
	
	@GetMapping("get-all-deleted-comments")
	public String getAllDeletedComments(ModelMap model) {
		
		// 開始查詢資料
		List<DeletedCommentDTO> commentList = forumCommentService.getAllDeletedComments();
		
		// 查詢完成forward到顯示頁面
		model.addAttribute("commentList", commentList);
		
		return "backend/forum/forum-deleted-comment";
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

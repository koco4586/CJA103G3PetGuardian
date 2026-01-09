package com.forum.controller;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import com.forum.model.ForumService;
import com.forum.model.ForumVO;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/forum/forum.do")
@MultipartConfig(
	    fileSizeThreshold = 1024 * 1024,
	    maxFileSize = 1024 * 1024,
	    maxRequestSize = 5 * 1024 * 1024
	)
public class ForumServlet extends HttpServlet{
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doPost(req, res);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws 	ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8");
		
		String action = req.getParameter("action");
		
		if("getForumNameForDisplay".equals(action)) {
			
			List<String> errorMsgs = new ArrayList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
			//接收參數
			
			String forumName = req.getParameter("forumName");
			if(forumName == null || (forumName.trim()).isEmpty()) {
				errorMsgs.add("請輸入欲查詢討論區的名稱");
			}
			
			if(!errorMsgs.isEmpty()) {
				RequestDispatcher failureView = req
						.getRequestDispatcher("/front_end/forum/listAllActiveForum.jsp");
				failureView.forward(req, res);
				return;
			}
			
			//開始查詢資料
			
			ForumService forumSvc = new ForumService();
			List<ForumVO> forumList = forumSvc.getForumByName(forumName);
			
			if(forumList.isEmpty()) {
				errorMsgs.add("查無相關的討論區");
			}
			
			if(!errorMsgs.isEmpty()) {
				RequestDispatcher failureView = req
						.getRequestDispatcher("/front_end/forum/listAllActiveForum.jsp");
				failureView.forward(req, res);
				return;
			}
			
			//查詢完成，準備轉交
			
			req.setAttribute("forumList", forumList);
			String url = "/front_end/forum/listAllActiveForumByKeyword.jsp";
			RequestDispatcher successView = req
					.getRequestDispatcher(url);
			successView.forward(req, res);
			
		}
		
		if("getForumIdForDisable".equals(action)) {
			
			List<String> errorMsgs = new ArrayList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
			//接收參數
			
			Integer forumId = Integer.valueOf(req.getParameter("forumId"));
			Integer forumStatus = Integer.valueOf(req.getParameter("forumStatus"));
			if(forumStatus == 0) {
				errorMsgs.add("此討論區已停用");
			}
			
			if(!errorMsgs.isEmpty()) {
				RequestDispatcher failureView = req
						.getRequestDispatcher("/back_end/forum/listAllForum.jsp");
				failureView.forward(req, res);
				return;
			}
					
			//開始查詢
			
			ForumService forumSvc = new ForumService();
			
			try {
				ForumVO forumVO = forumSvc.disableForum(forumId);
				
				//查詢完成，準備轉交
				
				req.setAttribute("forumVO", forumVO);
				String url = "/back_end/forum/listAllForum.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);
				
			} catch (Exception e) {
				errorMsgs.add(e.getMessage());
				if(!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req
							.getRequestDispatcher("/back_end/forum/listAllForum.jsp");
					failureView.forward(req, res);
					return;
				}
				
			}
			
		}
		
		if("getForumIdForActive".equals(action)) {
			
			List<String> errorMsgs = new ArrayList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
			//接收參數
			
			Integer forumId = Integer.valueOf(req.getParameter("forumId").trim());
			Integer forumStatus = Integer.valueOf(req.getParameter("forumStatus"));
			if(forumStatus == 1) {
				errorMsgs.add("此討論區已啟用");
			}
			
			if(!errorMsgs.isEmpty()) {
				RequestDispatcher failureView = req
						.getRequestDispatcher("/back_end/forum/listAllForum.jsp");
				failureView.forward(req, res);
				return;
			}
			
			//開始查詢
			
			ForumService forumSvc = new ForumService();
			
			try {
				ForumVO forumVO = forumSvc.activeForum(forumId);
				
				//查詢完成，準備轉交
				
				req.setAttribute("forumVO", forumVO);
				String url = "/back_end/forum/listAllForum.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);
				
			} catch(Exception e) {
				errorMsgs.add(e.getMessage());
				if(!errorMsgs.isEmpty()) {
					RequestDispatcher failureView = req
							.getRequestDispatcher("/back_end/forum/listAllForum.jsp");
					failureView.forward(req, res);
					return;
				}
				
			}
			
		}
		
		if ("getOneForUpdate".equals(action)) {

			List<String> errorMsgs = new ArrayList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
				//接收參數
			
				Integer forumId = Integer.valueOf(req.getParameter("forumId"));
				
				//開始查詢
				
				ForumService forumSvc = new ForumService();
				ForumVO forumVO = forumSvc.getOneForum(forumId);
								
				//查詢完成，準備轉交
				
				req.setAttribute("forumVO", forumVO);        
				String url = "/back_end/forum/updateForum.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);
		}
		
		
		if("insertForum".equals(action)) {
			
			List<String> errorMsgs = new ArrayList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
			//接收參數
//			Integer forumId = Integer.valueOf(req.getParameter("forumId"));
			String forumName = req.getParameter("forumName");
			
//			Timestamp createdAt = java.sql.Timestamp.valueOf(req.getParameter("createdAt"));
//			Integer forumStatus = Integer.valueOf(req.getParameter("forumStatus"));
			
			String forumNameReg = "^[(\u4e00-\u9fa5)(a-zA-Z0-9)]{1,50}$";
			if(forumName == null || (forumName.trim()).isEmpty()) {
				errorMsgs.add("討論區名稱請勿空白");
			}else if(!forumName.trim().matches(forumNameReg)) {
				errorMsgs.add("討論區名稱: 只能是中、英文字母、或數字，且不能超過50字");
			}
			
			byte[] forumPic = null;
			Part pic = req.getPart("forumPic");
				
			if (pic == null || pic.getSize() == 0) {
			    errorMsgs.add("請新增圖片");
			} else {
			    String contentType = pic.getContentType();
			    if (contentType == null || !contentType.startsWith("image/")) {
			        errorMsgs.add("請上傳圖片檔（jpg, png, gif）");
			    } else {
			        forumPic = pic.getInputStream().readAllBytes();
			    }
			}	
			
			ForumVO forumVO = new ForumVO();
//			forumVO.setForumId(forumId);
			forumVO.setForumName(forumName);
			forumVO.setForumPic(forumPic);
//			forumVO.setCreatedAt(createdAt);
//			forumVO.setForumStatus(forumStatus);
					
			if(!errorMsgs.isEmpty()) {
				req.setAttribute("forumVO", forumVO);
				RequestDispatcher failureView = req
						.getRequestDispatcher("/back_end/forum/addForum.jsp");
				failureView.forward(req, res);
				return;
			}
			
			ForumService forumSvc = new ForumService();
			forumSvc.addForum(forumName, forumPic);
			
			//新增完成，準備轉交
			
			String url = "/back_end/forum/listAllForum.jsp";
			RequestDispatcher successView = req
					.getRequestDispatcher(url);
			successView.forward(req, res);
			
		}
		
		if("updateForum".equals(action)) {
			
			List<String> errorMsgs = new ArrayList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
			//接收參數
			Integer forumId = Integer.valueOf(req.getParameter("forumId"));
			String forumName = req.getParameter("forumName");
			
//			Timestamp createdAt = java.sql.Timestamp.valueOf(req.getParameter("createdAt"));
//			Integer forumStatus = Integer.valueOf(req.getParameter("forumStatus"));
			
			String forumNameReg = "^[(\u4e00-\u9fa5)(a-zA-Z0-9)]{1,50}$";
			if(forumName == null || (forumName.trim()).isEmpty()) {
				errorMsgs.add("討論區名稱請勿空白");
			}else if(!forumName.trim().matches(forumNameReg)) {
				errorMsgs.add("討論區名稱: 只能是中、英文字母、或數字，且不能超過50字");
			}
			
			byte[] forumPic = null;
			Part pic = req.getPart("forumPic");

			if (pic == null || pic.getSize() == 0) {
				ForumService forumSvc = new ForumService();
			    forumPic = forumSvc.getForumPic(forumId);    
			} else {
			    String contentType = pic.getContentType();
			    if (contentType == null || !contentType.startsWith("image/")) {
			        errorMsgs.add("請上傳圖片檔（jpg, png, gif）");
			    } else {
			        forumPic = pic.getInputStream().readAllBytes();
			    }
			}
			
			ForumVO forumVO = new ForumVO();
			forumVO.setForumId(forumId);
			forumVO.setForumName(forumName);
			forumVO.setForumPic(forumPic);
			// forumVO.setCreatedAt(createdAt);
			// forumVO.setForumStatus(forumStatus);

			if (!errorMsgs.isEmpty()) {
				req.setAttribute("forumVO", forumVO);
				RequestDispatcher failureView = req.getRequestDispatcher("/back_end/forum/updateForum.jsp");
				failureView.forward(req, res);
				return;
			}
			
			//開始修改資料
			
			ForumService forumSvc = new ForumService();
			forumSvc.updateForum(forumId, forumName, forumPic);
			
			//修改完成，準備轉交
			
			String url = "/back_end/forum/listAllForum.jsp";
			RequestDispatcher successView = req
					.getRequestDispatcher(url);
			successView.forward(req, res);
			
		}
			
	}
	
}

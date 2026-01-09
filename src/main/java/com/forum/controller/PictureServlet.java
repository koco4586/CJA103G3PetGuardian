package com.forum.controller;

import java.io.IOException;

import com.forum.model.ForumService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/forum/picture.do")

public class PictureServlet extends HttpServlet{
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		Integer forumId = Integer.valueOf(req.getParameter("forumId"));
		
		ForumService forumSvc = new ForumService();
		byte[] forumPic = forumSvc.getForumPic(forumId);
		
		if (forumPic != null && forumPic.length > 0) {
            res.setContentType("image/*");
            res.getOutputStream().write(forumPic);
        }	
		
	}
	
}
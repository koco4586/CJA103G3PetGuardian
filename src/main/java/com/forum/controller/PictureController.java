package com.forum.controller;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.forum.model.ForumService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/forum")
public class PictureController {
	
	@Autowired
	ForumService forumService;
	
	@GetMapping("picture")
	public void picture(@RequestParam("forumId") Integer forumId, HttpServletRequest req, HttpServletResponse res) throws IOException {
		
		res.setContentType("image/*");
//		ForumService forumSvc = new ForumService();
		byte[] forumPic = forumService.getForumPic(forumId);
		
		if (forumPic != null && forumPic.length > 0) { 
			
            res.getOutputStream().write(forumPic);
            
		} else {
			
			ClassPathResource resource = new ClassPathResource("/static/images/backend/logo.png");
			
			try(InputStream is = resource.getInputStream();){
				
				byte[] logoPic = is.readAllBytes();
				res.getOutputStream().write(logoPic);
				
			}
			
		}
		
	}
	
}
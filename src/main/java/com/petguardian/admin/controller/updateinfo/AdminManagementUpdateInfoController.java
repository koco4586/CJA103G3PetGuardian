package com.petguardian.admin.controller.updateinfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.petguardian.admin.dto.updateinfo.AdminManagementUpdateInfoDTO;
import com.petguardian.admin.model.Admin;
import com.petguardian.admin.service.updateinfo.AdminManagementUpdateInfoService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminManagementUpdateInfoController {

	@Autowired
	private AdminManagementUpdateInfoService adminManagementUpdateInfoService;

	@PutMapping("/adminupdateinfo")
	public Admin adminupdateinfo(@RequestParam(required = false) MultipartFile admImage, // 非必填，前端沒選照片時，該值會是undefined，所以欄位實際上不會被包含在請求中，沒加(required = false)會報錯
			@ModelAttribute @Valid AdminManagementUpdateInfoDTO adminManagementUpdateInfoDTO, HttpSession session) {

		Integer admId = (Integer) session.getAttribute("admId");

		return adminManagementUpdateInfoService.adminupdateinfo(admImage, adminManagementUpdateInfoDTO, admId);

	}

}

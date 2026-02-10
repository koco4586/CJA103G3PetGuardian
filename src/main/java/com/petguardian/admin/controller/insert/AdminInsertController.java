package com.petguardian.admin.controller.insert;

import com.petguardian.admin.dto.insert.AdminInsertDTO;
import com.petguardian.admin.service.insert.AdminInsertService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminInsertController {

	@Autowired
	private AdminInsertService adminInsertService;

	@PostMapping("/admininsert")
	public Map<String, String> admininsert(@RequestParam(required = false) MultipartFile admImage,
			@ModelAttribute @Valid AdminInsertDTO adminInsertDTO) {

		Map<String, String> map = new HashMap<>();

		String result = adminInsertService.admininsert(admImage, adminInsertDTO);

		map.put("result", result);

		return map;

	}

}

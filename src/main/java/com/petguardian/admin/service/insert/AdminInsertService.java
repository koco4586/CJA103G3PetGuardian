package com.petguardian.admin.service.insert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.petguardian.admin.dto.insert.AdminInsertDTO;
import com.petguardian.admin.model.Admin;
import com.petguardian.admin.repository.insert.AdminInsertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AdminInsertService {

	@Autowired
	private AdminInsertRepository adminInsertRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final String REALPATH = "src/main/resources/static/images/backend/admin/";

	public String admininsert(MultipartFile fileImagePath, AdminInsertDTO adminInsertDTO) {

		if (!adminInsertDTO.getAdmPassword().equals(adminInsertDTO.getAdmPasswordCheck())) {
			return "密碼輸入不一致，請再次確認是否輸入正確。";
		}

		try {

			// 圖片存本機
			String fakePath = null;

			if (fileImagePath != null) {
				Path uploadDir = Paths.get(REALPATH).toAbsolutePath(); // Path是一個類別， get() 方法會將字串路徑轉換成 Path
																		// 物件，toAbsolutePath()會將相對路徑轉換為絕對路徑

				Files.createDirectories(uploadDir); // 會創建目錄，包括所有不存在的父目錄

				String fileName = UUID.randomUUID().toString() + "_" + fileImagePath.getOriginalFilename();// 照片檔名（隨機碼＿照片檔名）

				Path targetPath = uploadDir.resolve(fileName); // 將fileName拼接在uploadDir後面,檔案上傳的目錄路徑+檔案名稱=組合後的完整檔案路徑

				fileImagePath.transferTo(targetPath.toFile());// transferTo():創建指定路徑檔案，並且把使用者上傳照片內容寫入該檔案,targetPath.toFile():將
																// Path 物件轉換為 File 物件

				fakePath = "/admin/" + fileName;
			}
			// 假路徑存資料庫以及密碼雜湊

			Admin admin = new Admin();

			// 確保預設值
			// 管理員狀態1，管理員登入失敗次數0
			Integer admStatus = 1;
			Integer admLoginAttempts = 0;

			admin.setAdmImage(fakePath);
			admin.setAdmName(adminInsertDTO.getAdmName());
			admin.setAdmAccount(adminInsertDTO.getAdmAccount());
			admin.setAdmEmail(adminInsertDTO.getAdmEmail());
			admin.setAdmTel(adminInsertDTO.getAdmTel());
			admin.setAdmPassword(passwordEncoder.encode(adminInsertDTO.getAdmPassword()));

			// 確保預設值
			admin.setAdmStatus(admStatus);
			admin.setAdmLoginAttempts(admLoginAttempts);

			adminInsertRepository.save(admin);

			return "新增成功";

		} catch (IOException e) {
			// 處理錯誤，例如記錄日誌或返回錯誤訊息
			e.printStackTrace();
			return "新增失敗";
		}

	}

}

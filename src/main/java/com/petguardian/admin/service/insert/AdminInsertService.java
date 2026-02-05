package com.petguardian.admin.service.insert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import com.petguardian.admin.dto.insert.AdminInsertDTO;
import com.petguardian.admin.model.Admin;
import com.petguardian.admin.repository.insert.AdminInsertRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AdminInsertService {

	@Autowired
	private AdminInsertRepository adminInsertRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${file.upload.admin.path}")
	private String uploadPath;

	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	public String admininsert(MultipartFile fileImagePath, AdminInsertDTO adminInsertDTO) {

		// 驗證密碼
		if (!adminInsertDTO.getAdmPassword().equals(adminInsertDTO.getAdmPasswordCheck())) {
			return "密碼輸入不一致，請再次確認是否輸入正確。";
		}

		try {
			// 1. 驗證檔案
			validateFile(fileImagePath);

			// 2. 清理原始檔名（防止路徑遍歷）
			String originalFilename = fileImagePath.getOriginalFilename();
			String sanitizedFilename = new File(originalFilename).getName();

			// 3. 驗證副檔名
			String extension = getFileExtension(sanitizedFilename);
			if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
				return "不允許的檔案格式，僅支援: " + ALLOWED_EXTENSIONS;
			}

			// 4. 生成安全的檔名（只用 UUID + 副檔名）
			String safeFileName = UUID.randomUUID().toString() + "." + extension;

			// 5. 建立目標目錄
			Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
			Files.createDirectories(uploadDir);

			// 6. 完整的目標檔案路徑
			Path targetPath = uploadDir.resolve(safeFileName).normalize();

			// 7. 驗證路徑安全性（防止路徑遍歷攻擊）
			if (!targetPath.startsWith(uploadDir)) {
				throw new SecurityException("檢測到路徑遍歷攻擊");
			}

			// 8. 儲存檔案
			fileImagePath.transferTo(targetPath.toFile());

			// 9. 建立管理員資料並存入資料庫
			Admin admin = new Admin();
			admin.setAdmImage("/images/backend/admin/" + safeFileName); // 網頁訪問路徑
			admin.setAdmName(adminInsertDTO.getAdmName());
			admin.setAdmAccount(adminInsertDTO.getAdmAccount());
			admin.setAdmEmail(adminInsertDTO.getAdmEmail());
			admin.setAdmTel(adminInsertDTO.getAdmTel());
			admin.setAdmPassword(passwordEncoder.encode(adminInsertDTO.getAdmPassword()));

			adminInsertRepository.save(admin);
			return "新增成功";

		} catch (IllegalArgumentException e) {
			return "檔案驗證失敗: " + e.getMessage();
		} catch (SecurityException e) {
			return "安全性檢查失敗: " + e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return "新增失敗: " + e.getMessage();
		}
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("檔案不能為空");
		}
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("檔案大小不能超過 10MB");
		}
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || originalFilename.trim().isEmpty()) {
			throw new IllegalArgumentException("檔案名稱無效");
		}
	}

	private String getFileExtension(String filename) {
		int lastDot = filename.lastIndexOf('.');
		if (lastDot == -1) {
			throw new IllegalArgumentException("檔案缺少副檔名");
		}
		return filename.substring(lastDot + 1);
	}
}

//package com.petguardian.admin.service.insert;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.UUID;
//
//import com.petguardian.admin.dto.insert.AdminInsertDTO;
//import com.petguardian.admin.model.Admin;
//import com.petguardian.admin.repository.insert.AdminInsertRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//@Service
//public class AdminInsertService {
//
//    @Autowired
//    private AdminInsertRepository adminInsertRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    private static final String REALPATH = "/Users/CJA103G3_Workspace/CJA103G3PetGuardian/src/main/resources/static/images/backend/admin/";
//
//    public String admininsert(MultipartFile fileImagePath,
//                              AdminInsertDTO adminInsertDTO) {
//
//        if(!adminInsertDTO.getAdmPassword().equals(adminInsertDTO.getAdmPasswordCheck())){
//            return "密碼輸入不一致，請再次確認是否輸入正確。";
//        };
//
//        try {
//
//            //圖片存本機
//
//            File dir = new File(REALPATH);//"記憶體"中創建一個存有路徑的File物件
//            if (!dir.exists()) {
//                dir.mkdirs();//創建資料夾
//            }
//
//            String fileName = UUID.randomUUID().toString() + "_" + fileImagePath.getOriginalFilename();//照片檔名（隨機碼＿照片檔名）
//
//            String filePath = REALPATH + fileName;//檔案上傳的目錄路徑+檔案名稱=組合後的完整檔案路徑
//
//            fileImagePath.transferTo(new File(filePath));//創建指定路徑檔案，並且把使用者上傳照片內容寫入該檔案
//
//            //假路徑存資料庫以及密碼雜湊
//
//            Admin admin = new Admin();
//
//            admin.setAdmImage("/admin/" + fileName);
//            admin.setAdmName(adminInsertDTO.getAdmName());
//            admin.setAdmAccount(adminInsertDTO.getAdmAccount());
//            admin.setAdmEmail(adminInsertDTO.getAdmEmail());
//            admin.setAdmTel(adminInsertDTO.getAdmTel());
//            admin.setAdmPassword(passwordEncoder.encode(adminInsertDTO.getAdmPassword()));
//
//            adminInsertRepository.save(admin);
//
//            return "新增成功";
//
//        } catch (IOException e) {
//            // 處理錯誤，例如記錄日誌或返回錯誤訊息
//            e.printStackTrace();
//            return "新增失敗";
//        }
//
//    }
//
//}

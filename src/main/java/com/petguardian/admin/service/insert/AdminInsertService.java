package com.petguardian.admin.service.insert;

import java.io.File;
import java.io.IOException;
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

    private static final String REALPATH = "/Users/CJA103G3_Workspace/CJA103G3PetGuardian/src/main/resources/static/images/backend/admin/";

    public String admininsert(MultipartFile fileImagePath,
                              AdminInsertDTO adminInsertDTO) {

        if(!adminInsertDTO.getAdmPassword().equals(adminInsertDTO.getAdmPasswordCheck())){
            return "密碼輸入不一致，請再次確認是否輸入正確。";
        };

        try {

            //圖片存本機

            File dir = new File(REALPATH);//"記憶體"中創建一個存有路徑的File物件
            if (!dir.exists()) {
                dir.mkdirs();//創建資料夾
            }

            String fileName = UUID.randomUUID().toString() + "_" + fileImagePath.getOriginalFilename();//照片檔名（隨機碼＿照片檔名）

            String filePath = REALPATH + fileName;//檔案上傳的目錄路徑+檔案名稱=組合後的完整檔案路徑

            fileImagePath.transferTo(new File(filePath));//創建指定路徑檔案，並且把使用者上傳照片內容寫入該檔案

            //假路徑存資料庫以及密碼雜湊

            Admin admin = new Admin();

            admin.setAdmImage("/admin/" + fileName);
            admin.setAdmName(adminInsertDTO.getAdmName());
            admin.setAdmAccount(adminInsertDTO.getAdmAccount());
            admin.setAdmEmail(adminInsertDTO.getAdmEmail());
            admin.setAdmTel(adminInsertDTO.getAdmTel());
            admin.setAdmPassword(passwordEncoder.encode(adminInsertDTO.getAdmPassword()));

            adminInsertRepository.save(admin);

            return "新增成功";

        } catch (IOException e) {
            // 處理錯誤，例如記錄日誌或返回錯誤訊息
            e.printStackTrace();
            return "新增失敗";
        }

    }

}

package com.petguardian.member.service.management;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import com.petguardian.member.dto.MemberManagementSelectDTO;
import com.petguardian.member.dto.MemberManagementUpdateDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.management.MemberManagementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MemberManagementService {

    @Autowired
    private MemberManagementRepository memberManagementRepository;

    private static final String REALPATH = "/Users/CJA103G3_Workspace/CJA103G3PetGuardian/src/main/resources/static/images/member/";

    public Member update(MultipartFile fileImagePath,
            MemberManagementUpdateDTO memberManagementUpdateDTO,
            Integer memId) {

        Member member1 = null;

        try {

            // 圖片存本機

            File dir = new File(REALPATH);// "記憶體"中創建一個存有路徑的File物件
            if (!dir.exists()) {
                dir.mkdirs();// 創建資料夾
            }

            String fileName = UUID.randomUUID().toString() + "_" + fileImagePath.getOriginalFilename();// 照片檔名（隨機碼＿照片檔名）

            String filePath = REALPATH + fileName;// 檔案上傳的目錄路徑+檔案名稱=組合後的完整檔案路徑

            fileImagePath.transferTo(new File(filePath));// 創建指定路徑檔案，並且把使用者上傳照片內容寫入該檔案
            // 假路徑存資料庫

            Member member = memberManagementRepository.findById(memId).orElse(null);

            member.setMemId(memId);
            member.setMemImage("/member/" + fileName);
            member.setMemName(memberManagementUpdateDTO.getMemName());
            member.setMemAcc(memberManagementUpdateDTO.getMemAcc());
            member.setMemUid(memberManagementUpdateDTO.getMemUid());
            member.setMemBth(memberManagementUpdateDTO.getMemBth());
            member.setMemSex(memberManagementUpdateDTO.getMemSex());
            member.setMemEmail(memberManagementUpdateDTO.getMemEmail());
            member.setMemTel(memberManagementUpdateDTO.getMemTel());
            member.setMemAdd(memberManagementUpdateDTO.getMemAdd());
            member.setMemAccountNumber(memberManagementUpdateDTO.getMemAccountNumber());

            member1 = memberManagementRepository.save(member);

            return member1;

        } catch (IOException e) {
            // 處理錯誤，例如記錄日誌或返回錯誤訊息
            e.printStackTrace();
            return member1;
        }

    }

}

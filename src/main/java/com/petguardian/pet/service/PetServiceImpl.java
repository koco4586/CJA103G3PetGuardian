package com.petguardian.pet.service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.petguardian.pet.model.PetDTO;
import com.petguardian.pet.model.PetRepository;
import com.petguardian.pet.model.PetVO;

@Service
public class PetServiceImpl implements PetService { // 🔴 加上 implements

    @Autowired
    private PetRepository repository;

    @Override
    public List<PetDTO> getPetsByMemId(Integer memId) {
        // 呼叫 Repo 剛剛補好的方法
        List<PetVO> pets = repository.findByMemId(memId);
        
        // 使用你現有的 convertToDTO 工具進行轉換
        return pets.stream()
                   .map(this::convertToDTO)
                   .collect(Collectors.toList());
    } // <---
    // --- 轉換工具 (內部使用，不一定要在 Interface 定義) ---
    private PetDTO convertToDTO(PetVO vo) {
        if (vo == null) return null;
        PetDTO dto = new PetDTO();
        dto.setPetId(vo.getPetId());
        dto.setMemId(vo.getMemId());
        dto.setPetName(vo.getPetName());
        dto.setPetAge(vo.getPetAge());
        dto.setPetDescription(vo.getPetDescription());
        dto.setTypeId(vo.getTypeId());
        dto.setTypeName(resolveTypeName(vo.getTypeId()));
        dto.setSizeName(vo.getSizeId() != null && vo.getSizeId() == 1 ? "小型" : (vo.getSizeId() == 2 ? "中型" : "大型"));
        dto.setSizeId(vo.getSizeId());
        dto.setPetGender(vo.getPetGender());
        dto.setPetGenderText(resolveGenderText(vo.getPetGender()));
        
        if (vo.getPetImage() != null) {
            String base64 = java.util.Base64.getEncoder().encodeToString(vo.getPetImage());
            dto.setBase64Image("data:image/jpeg;base64," + base64);
        }
        
        return dto;
    }
    
    private String resolveTypeName(Integer typeId) {
        if (typeId == null) return "未知";

        switch (typeId) {
            case 1:
                return "貓";
            case 2:
                return "狗";
            default:
                return "其他";
        }
    }
    
    private String resolveGenderText(Integer gender) {
        if (gender == null) return "未知";
        return gender == 1 ? "公" : "母";
    }
    
    @Override
    public List<Integer> getAllPetIds() {
        // 取得所有寵物，並只收集他們的 ID
        return repository.getAll().stream()
                         .map(PetVO::getPetId)
                         .collect(Collectors.toList());
    }

    @Override
    public byte[] getPetImage(Integer petId) {
        return repository.findByPrimaryKey(petId)
                         .map(PetVO::getPetImage)
                         .orElse(null);
    }

    @Override
    public List<PetDTO> findPetsByNameDTO(String petName) {
        List<PetVO> voList = repository.getByName(petName);
        return voList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PetDTO getOnePetDTO(Integer petId) {
        return repository.findByPrimaryKey(petId)
                         .map(this::convertToDTO)
                         .orElse(null);
    }

    @Override
    public Map<String, Object> getPetsPageData(Integer whichPage) {
        List<PetVO> voList = repository.getAll();
        List<PetDTO> dtoList = voList.stream().map(this::convertToDTO).collect(Collectors.toList());

        int rowsPerPage = 3; // 你設定一頁 3 筆
        int rowNumber = dtoList.size();
        int pageNumber = (int) Math.ceil((double) rowNumber / rowsPerPage);
        
        // 計算切片的開始與結束 index
        int fromIndex = (whichPage - 1) * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, rowNumber);

        
        // 關鍵修正：只取出那一頁要顯示的資料
        List<PetDTO> pagedlist = (fromIndex < rowNumber) 
                                 ? dtoList.subList(fromIndex, toIndex) 
                                 : List.of();

        // 注意：這裡傳出的 Key 是 "petlist"
        return Map.of(
            "petlist", pagedlist, 
            "rowNumber", rowNumber, 
            "pageNumber", pageNumber,
            "rowsPerPage", rowsPerPage,
            "whichPage", whichPage
        );
    }

    @Override
    public void deletePet(Integer petId) {
        repository.delete(petId);
    }

    @Override
    public void addPet(PetVO petVO, MultipartFile petImage) throws Exception {
        if (petImage != null && !petImage.isEmpty()) {
            petVO.setPetImage(petImage.getBytes());
        }
        repository.insert(petVO);
    }

    @Override
    public void addPetFromBase64(String base64Str, String name, String type, String sex, String age, String size, String desc) {
        String base64 = base64Str.split(",")[1];
        byte[] image = Base64.getDecoder().decode(base64);
        PetVO pet = new PetVO();
        pet.setPetName(name);
        pet.setPetImage(image);
        pet.setTypeId("狗".equals(type) ? 2 : 1);
        pet.setPetGender("母".equals(sex) ? 2 : 1);
        pet.setPetAge(age == null || age.isEmpty() ? 0 : Integer.parseInt(age));
        pet.setSizeId(Integer.parseInt(size));
        pet.setPetDescription(desc);
        pet.setMemId(1); // 暫時寫死
        repository.insert(pet);
    }

    @Override
    public void updatePetWithCanvas(PetVO petVO, String base64Data, String deleteImage) throws Exception {
        if ("true".equals(deleteImage)) {
            petVO.setPetImage(null);
        } else if (base64Data != null && base64Data.contains(",")) {
            String base64Str = base64Data.split(",")[1];
            petVO.setPetImage(Base64.getDecoder().decode(base64Str));
        } else {
            PetVO oldPet = repository.findByPrimaryKey(petVO.getPetId())
            		.orElseThrow(() ->
                    new RuntimeException("找不到 petId=" + petVO.getPetId())
                );
            	petVO.setPetImage(oldPet.getPetImage());
            
        }
        repository.update(petVO);
    }
}
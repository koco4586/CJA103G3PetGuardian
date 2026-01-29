package com.petguardian.pet.service;

	import java.util.List;
	import java.util.Map;


import org.springframework.web.multipart.MultipartFile;
	import com.petguardian.pet.model.PetVO;
	import com.petguardian.pet.model.PetDTO;

	public interface PetService {
	    // 圖片顯示
		
		
		
		
		
	    byte[] getPetImage(Integer petId);
	    
	    // 查詢功能
	    List<Integer> getAllPetIds();
	    List<PetDTO> getPetsByMemId(Integer memId);
	    List<PetDTO> findPetsByNameDTO(String petName);
	    PetDTO getOnePetDTO(Integer petId);
	    Map<String, Object> getPetsPageData(Integer whichPage);
	    
	    // 新增與刪除
	    void deletePet(Integer petId);
	    void addPet(PetVO petVO, MultipartFile petImage) throws Exception;
	    
	    // 🔴 這裡我幫你保留了兩種版本的可能性（有無 memId）
	    void addPetFromBase64(String base64Str, String name, String type, String sex, String age, String size, String desc);
	    // 未來會員功能好時，你可以改用下面這個或直接在 Impl 裡面改
	    // void addPetFromBase64(String base64Str, String name, String type, String sex, String age, String size, String desc, Integer memId);

	    // 更新功能
	    void updatePetWithCanvas(PetVO petVO, String base64Data, String deleteImage) throws Exception;
	}


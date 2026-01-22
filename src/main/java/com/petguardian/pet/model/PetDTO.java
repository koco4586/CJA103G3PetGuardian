package com.petguardian.pet.model;

import java.io.Serializable;

/**
 * PetDTO: 用於前端頁面顯示的資料傳遞物件
 */
public class PetDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer memId;//有會員時在打開
    private Integer petId;
    private String petName;
    private Integer typeId;
    private String typeName;         // 顯示用：例如 "貓"、"狗"
    private Integer sizeId;
    private String sizeName;         // 顯示用：例如 "大型"、"小型"
    private Integer petGender;
    private String petGenderText;    // 顯示用：將 0/1 轉換為 "公/母"
    private Integer petAge;
    private String petDescription;
    private String base64Image;      // 顯示用：將 byte[] 轉換成 Base64 字串

    public PetDTO() {}

//     Getters and Setters
    
    public Integer getMemId() {
        return memId;
    }
    
    public void setMemId(Integer memId) {
        this.memId = memId;
    }
    
    
    

    public Integer getPetId() { return petId; }
    public void setPetId(Integer petId) { this.petId = petId; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    
    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    
    public Integer getSizeId() { return sizeId; }
    public void setSizeId(Integer sizeId) { this.sizeId = sizeId; }
    
    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }

    public Integer getPetGender() { return petGender; }
    public void setPetGender(Integer petGender) { this.petGender = petGender; }
    
    public String getPetGenderText() { return petGenderText; }
    public void setPetGenderText(String petGenderText) { this.petGenderText = petGenderText; }

    public Integer getPetAge() { return petAge; }
    public void setPetAge(Integer petAge) { this.petAge = petAge; }

    public String getPetDescription() { return petDescription; }
    public void setPetDescription(String petDescription) { this.petDescription = petDescription; }

    public String getBase64Image() { return base64Image; }
    public void setBase64Image(String base64Image) { this.base64Image = base64Image; }
}
package com.petguardian.pet.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * PetVO: 對應資料庫 PET 表的實體類別
 */
public class PetVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer petId;           // 寵物編號 (PK)
    private Integer memId;           // 會員編號 (FK)
    private Integer typeId;          // 種類編號 (FK)
    private Integer sizeId;          // 體型編號 (FK)
    private String petName;          // 寵物姓名
    private Integer petGender;       // 寵物性別 (0:公, 1:母)
    private Integer petAge;          // 寵物年齡
    private String petDescription;   // 寵物描述
    private Timestamp createdTime;   // 建立時間
    private byte[] petImage;         // 寵物照片 (Blob)

    public PetVO() {}

    // Getters and Setters
    public Integer getPetId() { return petId; }
    public void setPetId(Integer petId) { this.petId = petId; }

    public Integer getMemId() { return memId; }
    public void setMemId(Integer memId) { this.memId = memId; }

    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }

    public Integer getSizeId() { return sizeId; }
    public void setSizeId(Integer sizeId) { this.sizeId = sizeId; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public Integer getPetGender() { return petGender; }
    public void setPetGender(Integer petGender) { this.petGender = petGender; }

    public Integer getPetAge() { return petAge; }
    public void setPetAge(Integer petAge) { this.petAge = petAge; }

    public String getPetDescription() { return petDescription; }
    public void setPetDescription(String petDescription) { this.petDescription = petDescription; }

    public Timestamp getCreatedTime() { return createdTime; }
    public void setCreatedTime(Timestamp createdTime) { this.createdTime = createdTime; }

    public byte[] getPetImage() { return petImage; }
    public void setPetImage(byte[] petImage) { this.petImage = petImage; }
}
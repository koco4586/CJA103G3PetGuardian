package com.petguardian.pet.model;

import jakarta.persistence.*;

@Entity
@Table(name = "SERVICE_ITEMS") // 對接你的資料庫表名
public class PetServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_item_id")
    private Integer serviceItemId;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    // 必須與類別名稱完全一致的建構子
    public PetServiceItem() {
    }

    // Getter & Setter
    public Integer getServiceItemId() {
        return serviceItemId;
    }

    public void setServiceItemId(Integer serviceItemId) {
        this.serviceItemId = serviceItemId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

}

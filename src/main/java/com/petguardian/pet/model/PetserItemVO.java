package com.petguardian.pet.model;

import java.io.Serializable;







public class PetserItemVO implements Serializable {

	private static final long serialVersionUID = 1L;
	
    private Integer serviceItemId;    // 服務項目編號
    private String serviceType;   // 服務名稱
    private String serviceDesc;   // 服務描述
    private String priceText;     // 格式化後的價格 (例如: NT$ 350)
    private String sitterName;    // 保姆名稱
    private Integer sitterId;     // 保姆編號 (跳轉連結用)

    public PetserItemVO() {}
    
   
    // [加入 Getter/Setter]
    
    // Getter and Setter
    public Integer getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(Integer serviceItemId) { this.serviceItemId = serviceItemId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getServiceDesc() { return serviceDesc; }
    public void setServiceDesc(String serviceDesc) { this.serviceDesc = serviceDesc; }

    public String getPriceText() { return priceText; }
    public void setPriceText(String priceText) { this.priceText = priceText; }

    public String getSitterName() { return sitterName; }
    public void setSitterName(String sitterName) { this.sitterName = sitterName; }

    public Integer getSitterId() { return sitterId; }
    public void setSitterId(Integer sitterId) { this.sitterId = sitterId; }
}

package com.petguardian.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberDTO {
    private Integer memId;    // 對應 SQL 的 mem_id
    private String name;      // 會員姓名
    private String email;     // 會員信箱
    public MemberDTO(Integer memId, String name) {
        this.memId = memId;
        this.name = name;
    }
    
}

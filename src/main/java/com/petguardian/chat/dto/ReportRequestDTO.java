package com.petguardian.chat.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportRequestDTO {
    private String messageId;
    private Integer type;
    private String reason;
}

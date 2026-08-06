package edu.cdut.aiback.vo;

import lombok.Data;

@Data
public class AiDispatchAdviceVO {
    private Long workOrderId;
    private Long personnelId;
    private String name;
    private String reason;
}

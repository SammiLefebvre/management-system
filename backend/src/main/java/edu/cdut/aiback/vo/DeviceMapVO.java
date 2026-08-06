package edu.cdut.aiback.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DeviceMapVO {
    private Long id;
    private String deviceName;
    private String deviceCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String area;
    private String projectGroup;
    private String latestWorkOrderStatus;
}

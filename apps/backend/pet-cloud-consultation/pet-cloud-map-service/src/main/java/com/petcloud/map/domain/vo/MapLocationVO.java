package com.petcloud.map.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MapLocationVO {
    private String formattedAddress;
    private String province;
    private String city;
    private String district;
    private String businessArea;
    private BigDecimal latitude;
    private BigDecimal longitude;
}

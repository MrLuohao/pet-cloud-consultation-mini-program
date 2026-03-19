package com.petcloud.map.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MapPoiVO {
    private String poiId;
    private String name;
    private String type;
    private String address;
    private String cityCode;
    private String tel;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer distanceMeter;
}

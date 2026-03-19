package com.petcloud.map.domain.dto;

import lombok.Data;

@Data
public class MapGeocodeRequest {
    private String address;
    private String cityCode;
}

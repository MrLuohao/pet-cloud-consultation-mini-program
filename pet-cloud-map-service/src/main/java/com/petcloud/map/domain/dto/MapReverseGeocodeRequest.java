package com.petcloud.map.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MapReverseGeocodeRequest {
    private BigDecimal latitude;
    private BigDecimal longitude;
}

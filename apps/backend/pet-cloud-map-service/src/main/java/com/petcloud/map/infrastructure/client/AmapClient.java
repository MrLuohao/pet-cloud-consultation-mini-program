package com.petcloud.map.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

public interface AmapClient {
    JsonNode geocode(String address, String cityCode);

    JsonNode reverseGeocode(BigDecimal latitude, BigDecimal longitude);

    JsonNode searchSuggest(String keyword, String cityCode, String keywords);

    JsonNode nearby(String keywords, BigDecimal latitude, BigDecimal longitude, Integer radius, Integer page, Integer size);

    JsonNode detail(String poiId);
}

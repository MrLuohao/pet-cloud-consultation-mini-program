package com.petcloud.map.domain.service;

import com.petcloud.map.domain.dto.MapGeocodeRequest;
import com.petcloud.map.domain.dto.MapReverseGeocodeRequest;
import com.petcloud.map.domain.vo.MapLocationVO;
import com.petcloud.map.domain.vo.MapPoiVO;

import java.math.BigDecimal;
import java.util.List;

public interface MapService {
    MapLocationVO geocode(MapGeocodeRequest request);

    MapLocationVO reverseGeocode(MapReverseGeocodeRequest request);

    List<MapPoiVO> searchSuggest(String keyword, String cityCode, String poiType, BigDecimal latitude, BigDecimal longitude);

    List<MapPoiVO> nearby(String poiType, BigDecimal latitude, BigDecimal longitude, Integer radius, Integer page, Integer size);

    MapPoiVO detail(String poiId);
}

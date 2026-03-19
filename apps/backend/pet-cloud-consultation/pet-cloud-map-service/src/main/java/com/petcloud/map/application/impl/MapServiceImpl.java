package com.petcloud.map.application.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.petcloud.map.domain.dto.MapGeocodeRequest;
import com.petcloud.map.domain.dto.MapReverseGeocodeRequest;
import com.petcloud.map.domain.service.MapService;
import com.petcloud.map.domain.vo.MapLocationVO;
import com.petcloud.map.domain.vo.MapPoiVO;
import com.petcloud.map.infrastructure.client.AmapClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapServiceImpl implements MapService {

    private final AmapClient amapClient;

    @Override
    public MapLocationVO geocode(MapGeocodeRequest request) {
        JsonNode root = amapClient.geocode(request.getAddress(), request.getCityCode());
        JsonNode geocode = root.path("geocodes").isArray() && root.path("geocodes").size() > 0
                ? root.path("geocodes").get(0)
                : root.path("geocodes");
        String[] location = geocode.path("location").asText(",").split(",");
        return MapLocationVO.builder()
                .formattedAddress(geocode.path("formatted_address").asText())
                .province(geocode.path("province").asText())
                .city(geocode.path("city").asText())
                .district(geocode.path("district").asText())
                .latitude(parseDecimal(location, 1))
                .longitude(parseDecimal(location, 0))
                .build();
    }

    @Override
    public MapLocationVO reverseGeocode(MapReverseGeocodeRequest request) {
        JsonNode regeo = amapClient.reverseGeocode(request.getLatitude(), request.getLongitude()).path("regeocode");
        JsonNode component = regeo.path("addressComponent");
        return MapLocationVO.builder()
                .formattedAddress(regeo.path("formatted_address").asText())
                .province(component.path("province").asText())
                .city(component.path("city").asText())
                .district(component.path("district").asText())
                .businessArea(component.path("businessAreas").isArray() && component.path("businessAreas").size() > 0
                        ? component.path("businessAreas").get(0).path("name").asText()
                        : "")
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

    @Override
    public List<MapPoiVO> searchSuggest(String keyword, String cityCode, String poiType, BigDecimal latitude, BigDecimal longitude) {
        return readPois(amapClient.searchSuggest(keyword, cityCode, resolveKeywords(poiType)).path("tips"));
    }

    @Override
    public List<MapPoiVO> nearby(String poiType, BigDecimal latitude, BigDecimal longitude, Integer radius, Integer page, Integer size) {
        return readPois(amapClient.nearby(
                resolveKeywords(poiType),
                latitude,
                longitude,
                radius == null ? 3000 : radius,
                page == null ? 1 : page,
                size == null ? 10 : size
        ).path("pois"));
    }

    @Override
    public MapPoiVO detail(String poiId) {
        List<MapPoiVO> pois = readPois(amapClient.detail(poiId).path("pois"));
        return pois.isEmpty() ? null : pois.get(0);
    }

    private List<MapPoiVO> readPois(JsonNode array) {
        List<MapPoiVO> result = new ArrayList<>();
        if (array == null || !array.isArray()) {
            return result;
        }
        for (JsonNode item : array) {
            String[] location = item.path("location").asText(",").split(",");
            result.add(MapPoiVO.builder()
                    .poiId(item.path("id").asText())
                    .name(item.path("name").asText())
                    .type(item.path("type").asText())
                    .address(item.path("address").asText())
                    .cityCode(item.path("citycode").asText())
                    .tel(item.path("tel").asText())
                    .latitude(parseDecimal(location, 1))
                    .longitude(parseDecimal(location, 0))
                    .distanceMeter(item.path("distance").isMissingNode() ? null : item.path("distance").asInt())
                    .build());
        }
        return result;
    }

    private BigDecimal parseDecimal(String[] values, int index) {
        if (values.length <= index || values[index] == null || values[index].isBlank()) {
            return null;
        }
        return new BigDecimal(values[index]);
    }

    private String resolveKeywords(String poiType) {
        if (poiType == null || poiType.isBlank() || "address".equalsIgnoreCase(poiType)) {
            return "";
        }
        return switch (poiType.toLowerCase()) {
            case "hospital" -> "宠物医院";
            case "store" -> "宠物店";
            case "logistics" -> "快递驿站";
            default -> poiType;
        };
    }
}

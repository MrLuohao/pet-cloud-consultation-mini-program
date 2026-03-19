package com.petcloud.map.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.map.domain.dto.MapGeocodeRequest;
import com.petcloud.map.domain.dto.MapReverseGeocodeRequest;
import com.petcloud.map.domain.service.MapService;
import com.petcloud.map.domain.vo.MapLocationVO;
import com.petcloud.map.domain.vo.MapPoiVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping("/search/suggest")
    public Response<List<MapPoiVO>> searchSuggest(@RequestParam String keyword,
                                                  @RequestParam(required = false) String cityCode,
                                                  @RequestParam(required = false) String poiType,
                                                  @RequestParam(required = false) BigDecimal latitude,
                                                  @RequestParam(required = false) BigDecimal longitude) {
        return Response.succeed(mapService.searchSuggest(keyword, cityCode, poiType, latitude, longitude));
    }

    @PostMapping("/geocode")
    public Response<MapLocationVO> geocode(@RequestBody MapGeocodeRequest request) {
        return Response.succeed(mapService.geocode(request));
    }

    @PostMapping("/reverse-geocode")
    public Response<MapLocationVO> reverseGeocode(@RequestBody MapReverseGeocodeRequest request) {
        return Response.succeed(mapService.reverseGeocode(request));
    }

    @GetMapping("/poi/nearby")
    public Response<List<MapPoiVO>> nearby(@RequestParam String poiType,
                                           @RequestParam BigDecimal latitude,
                                           @RequestParam BigDecimal longitude,
                                           @RequestParam(required = false) Integer radius,
                                           @RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size) {
        return Response.succeed(mapService.nearby(poiType, latitude, longitude, radius, page, size));
    }

    @GetMapping("/poi/detail/{poiId}")
    public Response<MapPoiVO> detail(@PathVariable String poiId) {
        return Response.succeed(mapService.detail(poiId));
    }
}

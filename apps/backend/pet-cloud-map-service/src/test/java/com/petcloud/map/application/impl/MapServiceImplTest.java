package com.petcloud.map.application.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.map.domain.dto.MapGeocodeRequest;
import com.petcloud.map.domain.dto.MapReverseGeocodeRequest;
import com.petcloud.map.domain.vo.MapLocationVO;
import com.petcloud.map.domain.vo.MapPoiVO;
import com.petcloud.map.infrastructure.client.AmapClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildLocationFromGeocodeResponse() throws Exception {
        AmapClient client = new StubAmapClient(objectMapper.readTree("""
                {
                  "status":"1",
                  "geocodes":[
                    {
                      "formatted_address":"上海市浦东新区世纪大道1188号",
                      "province":"上海市",
                      "city":"上海市",
                      "district":"浦东新区",
                      "location":"121.473701,31.230416"
                    }
                  ]
                }
                """), null, null, null, null);

        MapServiceImpl service = new MapServiceImpl(client);
        MapLocationVO result = service.geocode(new MapGeocodeRequest());

        assertEquals("上海市浦东新区世纪大道1188号", result.getFormattedAddress());
        assertEquals(new BigDecimal("121.473701"), result.getLongitude());
        assertEquals(new BigDecimal("31.230416"), result.getLatitude());
    }

    @Test
    void shouldReturnNearbyHospitalPois() throws Exception {
        AmapClient client = new StubAmapClient(null, null, null,
                objectMapper.readTree("""
                        {
                          "status":"1",
                          "pois":[
                            {
                              "id":"B001",
                              "name":"安心宠物医院",
                              "type":"宠物医院",
                              "address":"浦东新区世纪大道100号",
                              "citycode":"021",
                              "tel":"021-12345678",
                              "location":"121.473701,31.230416",
                              "distance":"256"
                            }
                          ]
                        }
                        """), null);

        MapServiceImpl service = new MapServiceImpl(client);
        List<MapPoiVO> result = service.nearby("hospital", new BigDecimal("31.230416"), new BigDecimal("121.473701"), 3000, 1, 10);

        assertEquals(1, result.size());
        assertEquals("安心宠物医院", result.get(0).getName());
        assertEquals(256, result.get(0).getDistanceMeter());
        assertEquals("021-12345678", result.get(0).getTel());
    }

    @Test
    void shouldBuildReverseGeocodeLocation() throws Exception {
        AmapClient client = new StubAmapClient(null,
                objectMapper.readTree("""
                        {
                          "status":"1",
                          "regeocode":{
                            "formatted_address":"上海市浦东新区世纪大道1188号",
                            "addressComponent":{
                              "province":"上海市",
                              "city":"上海市",
                              "district":"浦东新区",
                              "businessAreas":[{"name":"陆家嘴"}]
                            }
                          }
                        }
                        """), null, null, null);

        MapServiceImpl service = new MapServiceImpl(client);
        MapReverseGeocodeRequest request = new MapReverseGeocodeRequest();
        request.setLatitude(new BigDecimal("31.230416"));
        request.setLongitude(new BigDecimal("121.473701"));

        MapLocationVO result = service.reverseGeocode(request);

        assertNotNull(result);
        assertEquals("陆家嘴", result.getBusinessArea());
        assertEquals(new BigDecimal("31.230416"), result.getLatitude());
    }

    private record StubAmapClient(JsonNode geocode,
                                  JsonNode reverse,
                                  JsonNode suggest,
                                  JsonNode nearby,
                                  JsonNode detail) implements AmapClient {
        @Override
        public JsonNode geocode(String address, String cityCode) {
            return geocode;
        }

        @Override
        public JsonNode reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
            return reverse;
        }

        @Override
        public JsonNode searchSuggest(String keyword, String cityCode, String keywords) {
            return suggest;
        }

        @Override
        public JsonNode nearby(String keywords, BigDecimal latitude, BigDecimal longitude, Integer radius, Integer page, Integer size) {
            return nearby;
        }

        @Override
        public JsonNode detail(String poiId) {
            return detail;
        }
    }
}

package com.petcloud.map.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.map.application.config.AmapProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AmapClientImpl implements AmapClient {

    private final AmapProperties amapProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public JsonNode geocode(String address, String cityCode) {
        return get("/v3/geocode/geo", UriComponentsBuilder.newInstance()
                .queryParam("address", address)
                .queryParam("city", cityCode));
    }

    @Override
    public JsonNode reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
        return get("/v3/geocode/regeo", UriComponentsBuilder.newInstance()
                .queryParam("location", longitude + "," + latitude)
                .queryParam("extensions", "all"));
    }

    @Override
    public JsonNode searchSuggest(String keyword, String cityCode, String keywords) {
        return get("/v3/assistant/inputtips", UriComponentsBuilder.newInstance()
                .queryParam("keywords", keyword)
                .queryParam("city", cityCode)
                .queryParam("datatype", "poi")
                .queryParam("type", keywords));
    }

    @Override
    public JsonNode nearby(String keywords, BigDecimal latitude, BigDecimal longitude, Integer radius, Integer page, Integer size) {
        return get("/v3/place/around", UriComponentsBuilder.newInstance()
                .queryParam("location", longitude + "," + latitude)
                .queryParam("keywords", keywords)
                .queryParam("radius", radius)
                .queryParam("page", page)
                .queryParam("offset", size));
    }

    @Override
    public JsonNode detail(String poiId) {
        return get("/v3/place/detail", UriComponentsBuilder.newInstance()
                .queryParam("id", poiId));
    }

    private JsonNode get(String path, UriComponentsBuilder builder) {
        if (amapProperties.getApiKey() == null || amapProperties.getApiKey().isBlank()) {
            throw new BusinessException(RespType.PARAMETER_ERROR, "高德地图 API Key 未配置");
        }
        URI uri = builder
                .scheme("https")
                .host(URI.create(amapProperties.getBaseUrl()).getHost())
                .path(path)
                .queryParam("key", amapProperties.getApiKey())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode root = objectMapper.readTree(response.body());
            if (!"1".equals(root.path("status").asText("1"))) {
                throw new BusinessException(RespType.PARAMETER_ERROR, root.path("info").asText("高德地图服务调用失败"));
            }
            return root;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(RespType.PARAMETER_ERROR, "高德地图服务调用失败");
        }
    }
}

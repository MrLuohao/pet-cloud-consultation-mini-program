package com.petcloud.shop.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.shop.domain.service.SearchService;
import com.petcloud.shop.domain.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 *
 * @author luohao
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final SearchService searchService;

    /**
     * 搜索商品
     */
    @GetMapping("/products")
    public Response<List<ProductVO>> searchProducts(@RequestParam String keyword) {
        log.info("搜索商品，keyword: {}", keyword);
        List<ProductVO> products = searchService.searchProducts(keyword);
        return Response.succeed(products);
    }

    /**
     * 获取热门搜索词
     */
    @GetMapping("/hot")
    public Response<List<String>> getHotKeywords() {
        log.info("获取热门搜索词");
        List<String> keywords = searchService.getHotKeywords();
        return Response.succeed(keywords);
    }
}

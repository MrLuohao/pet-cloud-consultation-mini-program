package com.petcloud.shop.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.shop.domain.dto.CollectionAddDTO;
import com.petcloud.shop.domain.dto.CollectionRemoveDTO;
import com.petcloud.shop.domain.dto.CollectionToggleDTO;
import com.petcloud.shop.domain.service.ProductCollectionService;
import com.petcloud.shop.domain.vo.ProductCollectionVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品收藏控制器
 *
 * @author luohao
 */
@RestController
@RequestMapping("/api/collection")
@RequiredArgsConstructor
public class ProductCollectionController {

    private static final Logger log = LoggerFactory.getLogger(ProductCollectionController.class);

    private final ProductCollectionService productCollectionService;
    private final UserContextHolderWeb userContextHolder;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public Response<Long> addCollection(HttpServletRequest request,
                                         @RequestBody CollectionAddDTO addRequest) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("添加收藏，userId: {}, productId: {}", userId, addRequest.getProductId());
        Long collectionId = productCollectionService.addCollection(userId, addRequest.getProductId());
        return Response.succeed(collectionId);
    }

    /**
     * 取消收藏
     */
    @PostMapping("/remove")
    public Response<Void> removeCollection(HttpServletRequest request,
                                            @RequestBody CollectionRemoveDTO removeRequest) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("取消收藏，userId: {}, productId: {}", userId, removeRequest.getProductId());
        productCollectionService.removeCollection(userId, removeRequest.getProductId());
        return Response.succeed();
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/list")
    public Response<List<ProductCollectionVO>> getCollectionList(HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取收藏列表，userId: {}", userId);
        List<ProductCollectionVO> collectionList = productCollectionService.getCollectionList(userId);
        return Response.succeed(collectionList);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Response<Boolean> isCollected(HttpServletRequest request,
                                          @RequestParam Long productId) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("检查是否已收藏，userId: {}, productId: {}", userId, productId);
        Boolean collected = productCollectionService.isCollected(userId, productId);
        return Response.succeed(collected);
    }

    /**
     * 切换收藏状态
     */
    @PostMapping("/toggle")
    public Response<Boolean> toggleCollection(HttpServletRequest request,
                                               @RequestBody CollectionToggleDTO toggleRequest) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("切换收藏状态，userId: {}, productId: {}", userId, toggleRequest.getProductId());
        Boolean collected = productCollectionService.toggleCollection(userId, toggleRequest.getProductId());
        return Response.succeed(collected);
    }
}

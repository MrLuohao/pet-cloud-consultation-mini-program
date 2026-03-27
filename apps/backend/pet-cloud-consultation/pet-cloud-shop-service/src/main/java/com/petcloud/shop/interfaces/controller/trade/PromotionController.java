package com.petcloud.shop.interfaces.controller.trade;

import com.petcloud.common.core.response.Response;
import com.petcloud.shop.domain.service.PromotionService;
import com.petcloud.shop.domain.vo.PromotionVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 满减活动控制器
 *
 * @author luohao
 */
@RestController
@RequestMapping("/api/promotion")
@RequiredArgsConstructor
public class PromotionController {

    private static final Logger log = LoggerFactory.getLogger(PromotionController.class);

    private final PromotionService promotionService;

    /**
     * 获取当前有效的满减活动
     */
    @GetMapping("/active")
    public Response<List<PromotionVO>> getActivePromotions() {
        log.info("获取当前有效的满减活动");
        List<PromotionVO> promotions = promotionService.getActivePromotions();
        return Response.succeed(promotions);
    }
}

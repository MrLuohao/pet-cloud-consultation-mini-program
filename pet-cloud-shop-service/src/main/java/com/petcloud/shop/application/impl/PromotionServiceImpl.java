package com.petcloud.shop.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.shop.domain.entity.Promotion;
import com.petcloud.shop.domain.service.PromotionService;
import com.petcloud.shop.domain.vo.PromotionVO;
import com.petcloud.shop.infrastructure.persistence.mapper.PromotionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 满减活动服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionMapper promotionMapper;

    @Override
    public List<PromotionVO> getActivePromotions() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        LambdaQueryWrapper<Promotion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Promotion::getStatus, 1)
                .le(Promotion::getStartTime, now)
                .ge(Promotion::getEndTime, now)
                .orderByAsc(Promotion::getThreshold);

        List<Promotion> promotions = promotionMapper.selectList(queryWrapper);

        return promotions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 将实体转换为VO
     *
     * @param promotion 满减活动实体
     * @return 满减活动VO
     */
    private PromotionVO convertToVO(Promotion promotion) {
        return PromotionVO.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .type(promotion.getType())
                .typeDesc(getTypeDesc(promotion.getType()))
                .threshold(promotion.getThreshold())
                .discount(promotion.getDiscount())
                .startTime(promotion.getStartTime())
                .endTime(promotion.getEndTime())
                .status(promotion.getStatus())
                .statusDesc(getStatusDesc(promotion.getStatus()))
                .description(promotion.getDescription())
                .ruleDesc(buildRuleDesc(promotion))
                .build();
    }

    /**
     * 获取类型描述
     *
     * @param type 类型
     * @return 类型描述
     */
    private String getTypeDesc(Integer type) {
        if (type == null) {
            return "";
        }
        if (type == 1) {
            return "满减";
        }
        return "";
    }

    /**
     * 获取状态描述
     *
     * @param status 状态
     * @return 状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 0:
                return "禁用";
            case 1:
                return "启用";
            default:
                return "";
        }
    }

    /**
     * 构建活动规则描述
     *
     * @param promotion 满减活动
     * @return 规则描述
     */
    private String buildRuleDesc(Promotion promotion) {
        if (promotion.getThreshold() != null && promotion.getDiscount() != null) {
            return "满" + promotion.getThreshold().stripTrailingZeros().toPlainString()
                    + "减" + promotion.getDiscount().stripTrailingZeros().toPlainString();
        }
        return "";
    }
}

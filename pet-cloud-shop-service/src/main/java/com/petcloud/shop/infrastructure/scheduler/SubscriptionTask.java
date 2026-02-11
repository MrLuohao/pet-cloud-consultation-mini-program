package com.petcloud.shop.infrastructure.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.shop.domain.entity.Product;
import com.petcloud.shop.domain.entity.ProductSubscription;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductSubscriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 商品订阅定时任务
 * 每日 08:00 检查到期的订阅，自动创建订单
 *
 * @author luohao
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionTask {

    private final ProductSubscriptionMapper subscriptionMapper;
    private final ProductMapper productMapper;

    /**
     * 每日 08:00 执行
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void processSubscriptions() {
        LocalDate today = LocalDate.now();
        log.info("[订阅任务] 开始处理，日期：{}", today);

        // 查询今日到期的正常状态订阅
        LambdaQueryWrapper<ProductSubscription> qw = new LambdaQueryWrapper<>();
        qw.eq(ProductSubscription::getStatus, ProductSubscription.Status.ACTIVE.getCode())
          .le(ProductSubscription::getNextOrderDate, today);
        List<ProductSubscription> dueList = subscriptionMapper.selectList(qw);

        log.info("[订阅任务] 找到 {} 条到期订阅", dueList.size());

        for (ProductSubscription sub : dueList) {
            try {
                processOneSub(sub, today);
            } catch (Exception e) {
                log.error("[订阅任务] 处理订阅 {} 失败：{}", sub.getId(), e.getMessage());
            }
        }
        log.info("[订阅任务] 处理完成");
    }

    private void processOneSub(ProductSubscription sub, LocalDate today) {
        Product product = productMapper.selectById(sub.getProductId());
        if (product == null || product.getStatus() != 1 || product.getStock() < sub.getQuantity()) {
            log.warn("[订阅任务] 订阅 {} 对应商品不可用或库存不足，跳过", sub.getId());
            return;
        }

        // 更新下次配送日期
        sub.setNextOrderDate(today.plusDays(sub.getCycleDays()));
        subscriptionMapper.updateById(sub);

        // TODO: 调用 OrderService 创建实际订单（当前仅更新下次配送日期，完整集成在真实支付开通后实现）
        log.info("[订阅任务] 订阅 {} 已处理，下次配送日期：{}", sub.getId(), sub.getNextOrderDate());
    }
}

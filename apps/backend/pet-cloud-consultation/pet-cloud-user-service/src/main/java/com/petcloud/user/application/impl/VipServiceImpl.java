package com.petcloud.user.application.impl;

import cn.hutool.core.util.IdUtil;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.dto.VipSubscribeDTO;
import com.petcloud.user.domain.enums.UserRespType;
import com.petcloud.user.domain.enums.VipPlanType;
import com.petcloud.user.domain.entity.VipOrder;
import com.petcloud.user.domain.entity.WxUser;
import com.petcloud.user.domain.service.VipService;
import com.petcloud.user.domain.vo.UserInfoVO;
import com.petcloud.user.infrastructure.persistence.mapper.VipOrderMapper;
import com.petcloud.user.infrastructure.persistence.mapper.WxUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

/**
 * 会员服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VipServiceImpl implements VipService {

    private final WxUserMapper wxUserMapper;
    private final VipOrderMapper vipOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO subscribe(Long userId, VipSubscribeDTO dto) {
        VipPlanType plan = VipPlanType.fromCode(dto.getPlanId());
        if (plan == null) {
            throw new BusinessException(RespType.PARAMETER_ERROR);
        }
        if (!Boolean.TRUE.equals(dto.getPayConfirmed())) {
            throw new BusinessException(UserRespType.VIP_PAYMENT_NOT_CONFIRMED);
        }

        WxUser wxUser = wxUserMapper.selectById(userId);
        if (wxUser == null) {
            throw new BusinessException(RespType.USER_NOT_FOUND);
        }

        Date now = new Date();
        Date baseTime = getBaseExpireTime(wxUser, now);
        Date expireTime = addDays(baseTime, plan.getDurationDays());

        if (wxUser.getVipStartTime() == null || baseTime.equals(now)) {
            wxUser.setVipStartTime(now);
        }
        wxUser.setVipExpireTime(expireTime);
        wxUser.setIsVip(1);
        wxUser.setVipLevel("VIP");

        BigDecimal savingAmount = wxUser.getVipSavingAmount() == null
                ? BigDecimal.ZERO
                : wxUser.getVipSavingAmount();
        wxUser.setVipSavingAmount(savingAmount);
        wxUserMapper.updateById(wxUser);

        VipOrder order = new VipOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPlanId(plan.getCode());
        order.setPlanName(plan.getDisplayName());
        order.setAmount(plan.getPrice());
        order.setDurationDays(plan.getDurationDays());
        order.setStatus(1);
        order.setPayTime(now);
        order.setExpireTime(expireTime);
        vipOrderMapper.insert(order);

        return UserInfoVO.builder()
                .id(wxUser.getId())
                .nickname(wxUser.getNickname())
                .avatarUrl(wxUser.getAvatarUrl())
                .gender(wxUser.getGender())
                .phone(wxUser.getPhone())
                .isVip(true)
                .vipLevel(wxUser.getVipLevel())
                .vipExpireDate(expireTime)
                .savingAmount(formatSavingAmount(savingAmount))
                .lastLoginTime(wxUser.getLastLoginTime())
                .build();
    }

    private Date getBaseExpireTime(WxUser wxUser, Date now) {
        Date expireTime = wxUser.getVipExpireTime();
        if (expireTime != null && expireTime.after(now)) {
            return expireTime;
        }
        return now;
    }

    private Date addDays(Date base, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(base);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    private String generateOrderNo() {
        return "VIP" + System.currentTimeMillis() + IdUtil.getSnowflakeNextIdStr();
    }

    private String formatSavingAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

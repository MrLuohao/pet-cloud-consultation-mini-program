package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.web.constant.CacheConstants;
import com.petcloud.common.web.utils.JwtUtils;
import com.petcloud.common.web.utils.RedisUtil;
import com.petcloud.user.domain.entity.ArticleComment;
import com.petcloud.user.domain.dto.WxLoginDTO;
import com.petcloud.user.domain.enums.UserRespType;
import com.petcloud.user.domain.entity.WxUser;
import com.petcloud.user.domain.service.WxAuthService;
import com.petcloud.user.domain.vo.LoginVO;
import com.petcloud.user.domain.vo.UserInfoVO;
import com.petcloud.user.infrastructure.persistence.mapper.ArticleCommentMapper;
import com.petcloud.user.infrastructure.persistence.mapper.WxUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 微信认证服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxAuthServiceImpl implements WxAuthService {

    private final WxUserMapper wxUserMapper;
    private final ArticleCommentMapper articleCommentMapper;
    private final RedisUtil redisUtil;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${wechat.miniapp.allow-mock-openid:false}")
    private boolean allowMockOpenid;

    @Value("${wechat.miniapp.mock-openid:dev_test_user_openid}")
    private String mockOpenid;

    @Value("${wechat.miniapp.app-id:}")
    private String wechatAppId;

    @Value("${wechat.miniapp.app-secret:}")
    private String wechatAppSecret;

    @Override
    public LoginVO login(WxLoginDTO loginDTO) {
        String openid = getOpenidByCode(loginDTO.getCode());

        // 查询用户是否存在
        LambdaQueryWrapper<WxUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WxUser::getOpenid, openid);
        WxUser wxUser = wxUserMapper.selectOne(queryWrapper);

        boolean isNewUser = false;

        if (wxUser == null) {
            // 新用户，创建用户（使用用户提供的信息或默认值）
            wxUser = new WxUser();
            wxUser.setOpenid(openid);

            // 使用用户提供的昵称，若为空则使用默认值
            String nickname = loginDTO.getNickname();
            String normalizedNickname = nickname == null ? "" : nickname.trim();
            wxUser.setNickname(!normalizedNickname.isEmpty() ? normalizedNickname : "宠物主人");

            // 使用用户提供的头像URL
            String avatarUrl = loginDTO.getAvatarUrl();
            wxUser.setAvatarUrl(avatarUrl != null ? avatarUrl : "");

            // 使用用户提供的性别
            Integer gender = loginDTO.getGender();
            wxUser.setGender(gender != null ? gender : 0);

            wxUser.setStatus(WxUser.Status.NORMAL.getCode());
            wxUser.setLastLoginTime(new Date());
            wxUserMapper.insert(wxUser);
            isNewUser = true;
            log.info("新用户注册成功，userId: {}, nickname: {}", wxUser.getId(), wxUser.getNickname());
        } else {
            // 老用户登录时，如果用户提供了新的头像或昵称，则更新
            boolean needUpdate = false;
            String nickname = loginDTO.getNickname();
            String avatarUrl = loginDTO.getAvatarUrl();

            if (nickname != null) {
                String normalizedNickname = nickname.trim();
                // 登录默认占位昵称不应覆盖用户已设置昵称
                boolean isPlaceholder = "宠物主人".equals(normalizedNickname);
                boolean hasCustomNickname = wxUser.getNickname() != null
                        && !wxUser.getNickname().trim().isEmpty()
                        && !isPlaceholder;
                if (!normalizedNickname.isEmpty()
                        && !normalizedNickname.equals(wxUser.getNickname())
                        && !(isPlaceholder && hasCustomNickname)) {
                    wxUser.setNickname(normalizedNickname);
                    needUpdate = true;
                }
            }
            if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals(wxUser.getAvatarUrl())) {
                wxUser.setAvatarUrl(avatarUrl);
                needUpdate = true;
            }
            // 老用户，只更新登录时间
            wxUser.setLastLoginTime(new Date());
            wxUserMapper.updateById(wxUser);
            log.info("用户登录成功，userId: {}", wxUser.getId());
        }

        // 检查用户状态
        if (WxUser.Status.DISABLED.getCode().equals(wxUser.getStatus())) {
            throw new BusinessException(RespType.ACCOUNT_DISABLED);
        }

        // 生成JWT Token
        String token = jwtUtils.generateToken(wxUser.getId(), wxUser.getOpenid());

        // 将Token存入Redis，用于验证和登出管理
        String tokenKey = CacheConstants.USER_TOKEN_PREFIX + token;
        redisUtil.set(tokenKey, wxUser.getId(), CacheConstants.TOKEN_EXPIRE_SECONDS);

        // 缓存用户基本信息
        String userInfoKey = CacheConstants.USER_INFO_PREFIX + wxUser.getId();
        redisUtil.hset(userInfoKey, "nickname", wxUser.getNickname(), CacheConstants.TOKEN_EXPIRE_SECONDS);
        redisUtil.hset(userInfoKey, "avatarUrl", wxUser.getAvatarUrl() != null ? wxUser.getAvatarUrl() : "", CacheConstants.TOKEN_EXPIRE_SECONDS);

        log.info("Token已缓存，userId: {}", wxUser.getId());

        boolean vipActive = isVipActive(wxUser);
        if (!vipActive && Integer.valueOf(1).equals(wxUser.getIsVip())) {
            wxUser.setIsVip(0);
            wxUserMapper.updateById(wxUser);
        }

        return LoginVO.builder()
                .token(token)
                .userId(wxUser.getId())
                .nickname(wxUser.getNickname())
                .avatarUrl(wxUser.getAvatarUrl())
                .isNewUser(isNewUser)
                .isVip(vipActive)
                .vipLevel(wxUser.getVipLevel())
                .vipExpireDate(wxUser.getVipExpireTime())
                .savingAmount(formatSavingAmount(wxUser.getVipSavingAmount()))
                .build();
    }

    @Override
    public String refreshToken(String token) {
        return jwtUtils.refreshToken(token);
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        WxUser wxUser = wxUserMapper.selectById(userId);
        if (wxUser == null) {
            throw new BusinessException(RespType.USER_NOT_FOUND);
        }
        boolean vipActive = isVipActive(wxUser);
        Date vipExpireTime = wxUser.getVipExpireTime();
        String vipLevel = wxUser.getVipLevel();
        if (!vipActive) {
            vipExpireTime = null;
            vipLevel = null;
            if (Integer.valueOf(1).equals(wxUser.getIsVip())) {
                wxUser.setIsVip(0);
                wxUserMapper.updateById(wxUser);
            }
        }

        return UserInfoVO.builder()
                .id(wxUser.getId())
                .userId(wxUser.getId())
                .nickname(wxUser.getNickname())
                .avatarUrl(wxUser.getAvatarUrl())
                .gender(wxUser.getGender())
                .phone(wxUser.getPhone())
                .isVip(vipActive)
                .vipLevel(vipLevel)
                .vipExpireDate(vipExpireTime)
                .savingAmount(formatSavingAmount(wxUser.getVipSavingAmount()))
                .lastLoginTime(wxUser.getLastLoginTime())
                .build();
    }

    @Override
    public void updateUserInfo(Long userId, String nickname, String avatarUrl, Integer gender) {
        WxUser wxUser = wxUserMapper.selectById(userId);
        if (wxUser == null) {
            throw new BusinessException(RespType.USER_NOT_FOUND);
        }

        if (nickname != null) {
            wxUser.setNickname(nickname);
        }
        if (avatarUrl != null) {
            wxUser.setAvatarUrl(avatarUrl);
        }
        if (gender != null) {
            wxUser.setGender(gender);
        }

        wxUserMapper.updateById(wxUser);

        // 同步更新评论中的用户快照信息，避免文章评论展示旧头像/昵称
        if (nickname != null || avatarUrl != null) {
            LambdaUpdateWrapper<ArticleComment> commentUpdateWrapper = new LambdaUpdateWrapper<>();
            commentUpdateWrapper.eq(ArticleComment::getUserId, userId);
            if (nickname != null) {
                commentUpdateWrapper.set(ArticleComment::getUserNickname, nickname);
            }
            if (avatarUrl != null) {
                commentUpdateWrapper.set(ArticleComment::getUserAvatar, avatarUrl);
            }
            articleCommentMapper.update(null, commentUpdateWrapper);
        }

        // 同步更新回复目标昵称
        if (nickname != null) {
            LambdaUpdateWrapper<ArticleComment> replyNicknameUpdateWrapper = new LambdaUpdateWrapper<>();
            replyNicknameUpdateWrapper.eq(ArticleComment::getReplyToUserId, userId)
                    .set(ArticleComment::getReplyToNickname, nickname);
            articleCommentMapper.update(null, replyNicknameUpdateWrapper);
        }

        // 刷新缓存，确保后续读取到最新用户信息
        String userInfoKey = CacheConstants.USER_INFO_PREFIX + userId;
        redisUtil.hset(userInfoKey, "nickname", wxUser.getNickname() != null ? wxUser.getNickname() : "", CacheConstants.TOKEN_EXPIRE_SECONDS);
        redisUtil.hset(userInfoKey, "avatarUrl", wxUser.getAvatarUrl() != null ? wxUser.getAvatarUrl() : "", CacheConstants.TOKEN_EXPIRE_SECONDS);

        log.info("用户信息更新成功，userId: {}", userId);
    }

    private boolean isVipActive(WxUser wxUser) {
        if (wxUser == null || !Integer.valueOf(1).equals(wxUser.getIsVip())) {
            return false;
        }
        Date expireTime = wxUser.getVipExpireTime();
        if (expireTime == null) {
            return false;
        }
        return expireTime.after(new Date());
    }

    private String formatSavingAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        try {
            // 验证Token有效性
            if (jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserIdFromToken(token);

                // 1. 删除Token缓存
                String tokenKey = CacheConstants.USER_TOKEN_PREFIX + token;
                redisUtil.del(tokenKey);

                // 2. 将Token加入黑名单（防止JWT本身未过期被继续使用）
                String blacklistKey = CacheConstants.TOKEN_BLACKLIST_PREFIX + token;
                redisUtil.set(blacklistKey, userId, CacheConstants.TOKEN_EXPIRE_SECONDS);

                log.info("用户登出成功，userId: {}", userId);
            }
        } catch (Exception e) {
            log.warn("登出处理异常", e);
        }
    }

    /**
     * 通过code获取openid
     *
     * @param code 微信登录code
     * @return openid
     */
    private String getOpenidByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(UserRespType.WECHAT_LOGIN_CODE_REQUIRED);
        }

        if (allowMockOpenid) {
            log.warn("当前启用微信登录Mock openid，仅允许开发/测试环境使用");
            return mockOpenid;
        }

        if (wechatAppId == null || wechatAppId.isBlank() || wechatAppSecret == null || wechatAppSecret.isBlank()) {
            throw new BusinessException(UserRespType.WECHAT_LOGIN_CONFIG_MISSING);
        }

        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                URLEncoder.encode(wechatAppId, StandardCharsets.UTF_8),
                URLEncoder.encode(wechatAppSecret, StandardCharsets.UTF_8),
                URLEncoder.encode(code, StandardCharsets.UTF_8)
        );

        try {
            String responseBody = restTemplate.getForObject(url, String.class);
            if (responseBody == null || responseBody.isBlank()) {
                throw new BusinessException(UserRespType.WECHAT_LOGIN_EMPTY_RESPONSE);
            }

            JsonNode root = objectMapper.readTree(responseBody);
            int errCode = root.path("errcode").asInt(0);
            if (errCode != 0) {
                throw new BusinessException(UserRespType.WECHAT_LOGIN_FAILED, errCode);
            }

            String openid = root.path("openid").asText("");
            if (openid.isBlank()) {
                throw new BusinessException(UserRespType.WECHAT_LOGIN_OPENID_MISSING);
            }
            return openid;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信jscode2session失败", e);
            throw new BusinessException(UserRespType.WECHAT_LOGIN_SERVICE_ERROR);
        }
    }
}

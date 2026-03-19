package com.petcloud.common.web.constant;

/**
 * 缓存Key常量类
 * 统一管理所有Redis缓存Key的前缀和过期时间
 *
 * @author luohao
 */
public final class CacheConstants {

    private CacheConstants() {
        // 私有构造函数，防止实例化
    }

    // ==================== 用户相关缓存 ====================

    /**
     * 用户Token缓存前缀
     * 完整Key: user:token:{token}
     * Value: userId
     */
    public static final String USER_TOKEN_PREFIX = "user:token:";

    /**
     * Token黑名单前缀
     * 完整Key: user:token:blacklist:{token}
     * Value: userId
     */
    public static final String TOKEN_BLACKLIST_PREFIX = "user:token:blacklist:";

    /**
     * 用户信息缓存前缀
     * 完整Key: user:info:{userId}
     */
    public static final String USER_INFO_PREFIX = "user:info:";

    /**
     * Token过期时间（秒）- 7天
     */
    public static final long TOKEN_EXPIRE_SECONDS = 7 * 24 * 60 * 60;

    // ==================== 商品相关缓存 ====================

    /**
     * 商品分类列表缓存Key
     */
    public static final String PRODUCT_CATEGORY_LIST = "product:category:list";

    /**
     * 商品列表缓存前缀
     * 完整Key: product:list:{categoryId} 或 product:list:all
     */
    public static final String PRODUCT_LIST_PREFIX = "product:list:";

    /**
     * 商品详情缓存前缀
     * 完整Key: product:detail:{productId}
     */
    public static final String PRODUCT_DETAIL_PREFIX = "product:detail:";

    /**
     * 商品分类缓存过期时间（秒）- 1小时
     */
    public static final long CATEGORY_EXPIRE_SECONDS = 60 * 60;

    /**
     * 商品列表缓存过期时间（秒）- 5分钟
     */
    public static final long PRODUCT_LIST_EXPIRE_SECONDS = 5 * 60;

    /**
     * 商品详情缓存过期时间（秒）- 10分钟
     */
    public static final long PRODUCT_DETAIL_EXPIRE_SECONDS = 10 * 60;

    // ==================== 订单相关缓存 ====================

    /**
     * 订单缓存前缀
     * 完整Key: order:detail:{orderId}
     */
    public static final String ORDER_DETAIL_PREFIX = "order:detail:";

    /**
     * 用户订单列表缓存前缀
     * 完整Key: order:user:{userId}
     */
    public static final String USER_ORDER_LIST_PREFIX = "order:user:";

    /**
     * 订单缓存过期时间（秒）- 10分钟
     */
    public static final long ORDER_EXPIRE_SECONDS = 10 * 60;

    // ==================== 验证码相关缓存 ====================

    /**
     * 短信验证码缓存前缀
     * 完整Key: sms:code:{phone}
     */
    public static final String SMS_CODE_PREFIX = "sms:code:";

    /**
     * 图形验证码缓存前缀
     * 完整Key: captcha:{sessionId}
     */
    public static final String CAPTCHA_PREFIX = "captcha:";

    /**
     * 验证码过期时间（秒）- 5分钟
     */
    public static final long CODE_EXPIRE_SECONDS = 5 * 60;

    // ==================== 限流相关缓存 ====================

    /**
     * API限流缓存前缀
     * 完整Key: rate limit:{apiName}:{userId}
     */
    public static final String RATE_LIMIT_PREFIX = "rate limit:";

    /**
     * 限流时间窗口（秒）- 1分钟
     */
    public static final long RATE_LIMIT_WINDOW_SECONDS = 60;
}

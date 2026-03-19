package com.petcloud.common.web.utils;

import com.petcloud.common.web.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类 - 负责JWT token的生成和解析
 *
 * @author luohao
 */
@Slf4j
@Component
public class JwtUtils {

    private final JwtProperties jwtProperties;

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        log.info("JwtUtils初始化，密钥长度: {}", jwtProperties.getSecretKey().length());
    }

    /**
     * 生成密钥
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     *
     * @param userId 用户ID
     * @param openid 微信openid
     * @return JWT Token
     */
    public String generateToken(Long userId, String openid) {
        return generateToken(userId, openid, null);
    }

    /**
     * 生成JWT Token（包含昵称）
     *
     * @param userId   用户ID
     * @param openid   微信openid
     * @param nickname 用户昵称
     * @return JWT Token
     */
    public String generateToken(Long userId, String openid, String nickname) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpirationTime());

        var builder = Jwts.builder()
                .claim("userId", userId)
                .claim("openid", openid);

        if (nickname != null) {
            builder.claim("nickname", nickname);
        }

        return builder
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 解析JWT Token
     *
     * @param token JWT Token
     * @return Claims
     * @throws JwtException 解析异常
     */
    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取OpenID
     *
     * @param token JWT Token
     * @return OpenID
     */
    public String getOpenidFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("openid", String.class);
    }

    /**
     * 从Token中获取用户昵称
     *
     * @param token JWT Token
     * @return 用户昵称，如果Token中没有则返回null
     */
    public String getNicknameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("nickname", String.class);
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token为空");
            return false;
        }
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.warn("JWT Token验证失败: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    /**
     * 刷新Token
     *
     * @param token 旧Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        Long userId = claims.get("userId", Long.class);
        String openid = claims.get("openid", String.class);
        return generateToken(userId, openid);
    }

    /**
     * 获取Token过期时间（毫秒）
     *
     * @return 过期时间
     */
    public long getExpirationTime() {
        return jwtProperties.getExpirationTime();
    }

    /**
     * 获取Token过期时间（秒）
     *
     * @return 过期时间（秒）
     */
    public long getExpirationSeconds() {
        return jwtProperties.getExpirationTime() / 1000;
    }
}

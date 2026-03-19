package com.petcloud.common.database.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.petcloud.common.core.domain.UserContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import java.util.Date;

/**
 * MyBatis-Plus 配置类
 *
 * @author luohao
 */
@AutoConfiguration
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 防止全表更新和删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 字段自动填充配置
     * 自动填充 createTime, modifyTime, creatorId, creatorName, modifierId, modifierName
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {

            @Override
            public void insertFill(MetaObject metaObject) {
                Date now = new Date();
                Long userId = UserContextHolder.getUserId();
                String nickname = UserContextHolder.getNickname();

                // 时间字段
                this.strictInsertFill(metaObject, "createTime", Date.class, now);
                this.strictInsertFill(metaObject, "modifyTime", Date.class, now);

                // 用户字段（仅当 ThreadLocal 中有用户信息时填充）
                if (userId != null) {
                    this.strictInsertFill(metaObject, "creatorId", Long.class, userId);
                    this.strictInsertFill(metaObject, "modifierId", Long.class, userId);
                }
                if (nickname != null) {
                    this.strictInsertFill(metaObject, "creatorName", String.class, nickname);
                    this.strictInsertFill(metaObject, "modifierName", String.class, nickname);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                Long userId = UserContextHolder.getUserId();
                String nickname = UserContextHolder.getNickname();

                // 时间字段 - 使用 setFieldValByName 强制更新，无论原值是否为 null
                this.setFieldValByName("modifyTime", new Date(), metaObject);

                // 用户字段（仅当 ThreadLocal 中有用户信息时填充）
                if (userId != null) {
                    this.setFieldValByName("modifierId", userId, metaObject);
                }
                if (nickname != null) {
                    this.setFieldValByName("modifierName", nickname, metaObject);
                }
            }
        };
    }
}

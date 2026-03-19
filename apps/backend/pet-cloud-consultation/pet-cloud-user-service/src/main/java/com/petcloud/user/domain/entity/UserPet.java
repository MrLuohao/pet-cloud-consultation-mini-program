package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户宠物实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_pet")
public class UserPet extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 宠物名称
     */
    @TableField("name")
    private String name;

    /**
     * 类型(1狗/2猫/3其他)
     */
    @TableField("type")
    private Integer type;

    /**
     * 品种
     */
    @TableField("breed")
    private String breed;

    /**
     * 性别(0未知/1公/2母)
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 体重(kg)
     */
    @TableField("weight")
    private BigDecimal weight;

    /**
     * 宠物头像
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 健康状况
     */
    @TableField("health_status")
    private String healthStatus;

    /**
     * 宠物性格
     */
    @TableField("personality")
    private String personality;

    /**
     * 座右铭/介绍
     */
    @TableField("motto")
    private String motto;

    /**
     * 宠物类型枚举
     */
    public enum PetType {
        DOG(1, "狗"),
        CAT(2, "猫"),
        OTHER(3, "其他");

        private final Integer code;
        private final String desc;

        PetType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 性别枚举
     */
    public enum Gender {
        UNKNOWN(0, "未知"),
        MALE(1, "公"),
        FEMALE(2, "母");

        private final Integer code;
        private final String desc;

        Gender(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}

package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户宠物VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPetVO {

    /**
     * 宠物ID
     */
    private Long id;

    /**
     * 宠物名称
     */
    private String name;

    /**
     * 类型(1狗/2猫/3其他)
     */
    private Integer type;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 品种
     */
    private String breed;

    /**
     * 性别(0未知/1公/2母)
     */
    private Integer gender;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate birthday;

    /**
     * 体重
     */
    private BigDecimal weight;

    /**
     * 宠物头像
     */
    private String avatarUrl;

    /**
     * 健康状况
     */
    private String healthStatus;

    /**
     * 宠物性格
     */
    private String personality;

    /**
     * 座右铭/介绍
     */
    private String motto;
}

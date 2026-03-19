package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 咨询记录实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consultation")
public class Consultation extends BaseEntity {

    /**
     * 咨询单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户昵称
     */
    @TableField("user_nickname")
    private String userNickname;

    /**
     * 用户头像
     */
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 宠物ID
     */
    @TableField("pet_id")
    private Long petId;

    /**
     * 宠物名称
     */
    @TableField("pet_name")
    private String petName;

    /**
     * 宠物类型：1狗 2猫 3其他
     */
    @TableField("pet_type")
    private Integer petType;

    /**
     * 医生ID
     */
    @TableField("doctor_id")
    private Long doctorId;

    /**
     * 医生姓名
     */
    @TableField("doctor_name")
    private String doctorName;

    /**
     * 医生头像
     */
    @TableField("doctor_avatar")
    private String doctorAvatar;

    /**
     * 类型：1图文 2视频
     */
    @TableField("type")
    private Integer type;

    /**
     * 状态：0待接单 1进行中 2已完成 3已取消
     */
    @TableField("status")
    private Integer status;

    /**
     * 病情描述
     */
    @TableField("description")
    private String description;

    /**
     * 病情图片
     */
    @TableField("images")
    private String images;

    /**
     * 咨询费
     */
    @TableField("fee")
    private BigDecimal fee;

    /**
     * 接单时间
     */
    @TableField("accept_time")
    private String acceptTime;

    /**
     * 完成时间
     */
    @TableField("finish_time")
    private String finishTime;

    /**
     * 取消时间
     */
    @TableField("cancel_time")
    private String cancelTime;

    /**
     * 是否紧急：0普通 1紧急
     */
    @TableField("is_urgent")
    private Integer isUrgent;

    /**
     * 咨询类型枚举
     */
    public enum Type {
        TEXT(1, "图文"),
        VIDEO(2, "视频");

        private final Integer code;
        private final String desc;

        Type(Integer code, String desc) {
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
     * 咨询状态枚举
     */
    public enum Status {
        PENDING(0, "待接单"),
        IN_PROGRESS(1, "进行中"),
        COMPLETED(2, "已完成"),
        CANCELLED(3, "已取消");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
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

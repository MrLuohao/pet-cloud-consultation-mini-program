package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 健康档案实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("health_record")
public class HealthRecord extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

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
     * 记录类型：vaccine,checkup,medicine,surgery,other
     */
    @TableField("record_type")
    private String recordType;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 详细内容
     */
    @TableField("content")
    private String content;

    /**
     * 医院名称
     */
    @TableField("hospital_name")
    private String hospitalName;

    /**
     * 医生姓名
     */
    @TableField("doctor_name")
    private String doctorName;

    /**
     * 记录日期
     */
    @TableField("record_date")
    private LocalDate recordDate;

    /**
     * 下次日期（如疫苗下次接种）
     */
    @TableField("next_date")
    private LocalDate nextDate;

    /**
     * 相关图片
     */
    @TableField("images")
    private String images;

    /**
     * 健康记录类型枚举
     */
    public enum RecordType {
        VACCINE("vaccine", "疫苗接种"),
        CHECKUP("checkup", "健康检查"),
        MEDICINE("medicine", "用药记录"),
        SURGERY("surgery", "手术记录"),
        OTHER("other", "其他");

        private final String code;
        private final String desc;

        RecordType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}

package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 咨询VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationVO {

    /**
     * 咨询ID
     */
    private Long id;

    /**
     * 咨询单号
     */
    private String orderNo;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 医生头像
     */
    private String doctorAvatar;

    /**
     * 宠物ID
     */
    private Long petId;

    /**
     * 宠物名称
     */
    private String petName;

    /**
     * 宠物类型
     */
    private Integer petType;

    /**
     * 类型：1图文 2视频
     */
    private Integer type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 状态：0待接单 1进行中 2已完成 3已取消
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 病情描述
     */
    private String description;

    /**
     * 病情图片列表
     */
    private List<String> images;

    /**
     * 咨询费
     */
    private BigDecimal fee;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否紧急：0普通 1紧急
     */
    private Integer isUrgent;

    /**
     * 等待提示（无在线医生时返回）
     */
    private String waitingMessage;
}

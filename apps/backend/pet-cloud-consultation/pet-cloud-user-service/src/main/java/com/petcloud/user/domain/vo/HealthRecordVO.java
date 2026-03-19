package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 健康档案VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecordVO {

    /**
     * 健康档案ID
     */
    private Long id;

    /**
     * 宠物ID
     */
    private Long petId;

    /**
     * 宠物名称
     */
    private String petName;

    /**
     * 记录类型：vaccine,checkup,medicine,surgery,other
     */
    private String recordType;

    /**
     * 记录类型描述
     */
    private String recordTypeDesc;

    /**
     * 标题
     */
    private String title;

    /**
     * 详细内容
     */
    private String content;

    /**
     * 医院名称
     */
    private String hospitalName;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 记录日期
     */
    private LocalDate recordDate;

    /**
     * 下次日期
     */
    private LocalDate nextDate;

    /**
     * 相关图片列表
     */
    private List<String> images;
}

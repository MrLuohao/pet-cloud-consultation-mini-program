package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 医生VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorVO {

    private Long id;

    private String name;

    private String avatar;

    private String title;

    private String specialty;

    private String department;

    private Integer experience;

    private String description;

    private String hospitalName;

    private BigDecimal consultationFee;

    private BigDecimal rating;

    private Integer consultationCount;

    private String[] tags;

    /** 好评率 0-100 */
    private Integer goodReviewRate;

    /** 平均响应分钟数 */
    private Integer avgResponseMinutes;
}

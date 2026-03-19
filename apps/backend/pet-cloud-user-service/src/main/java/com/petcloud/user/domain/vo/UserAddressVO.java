package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户收货地址VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressVO {

    /**
     * 地址ID
     */
    private Long id;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 完整地址
     */
    private String fullAddress;

    /**
     * 是否默认地址
     */
    private Integer isDefault;

    private BigDecimal longitude;
    private BigDecimal latitude;
    private String businessArea;
    private String doorNo;
    private String rawText;
    private String parsedName;
    private String parsedPhone;
    private String mapAddress;
    private String addressTag;
}

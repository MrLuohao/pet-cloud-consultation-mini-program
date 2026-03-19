package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户收货地址实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_address")
public class UserAddress extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 联系人
     */
    @TableField("contact_name")
    private String contactName;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 省份
     */
    @TableField("province")
    private String province;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 区/县
     */
    @TableField("district")
    private String district;

    /**
     * 详细地址
     */
    @TableField("detail_address")
    private String detailAddress;

    /**
     * 是否默认地址(0否/1是)
     */
    @TableField("is_default")
    private Integer isDefault;

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("latitude")
    private BigDecimal latitude;

    @TableField("business_area")
    private String businessArea;

    @TableField("door_no")
    private String doorNo;

    @TableField("raw_text")
    private String rawText;

    @TableField("parsed_name")
    private String parsedName;

    @TableField("parsed_phone")
    private String parsedPhone;

    @TableField("map_address")
    private String mapAddress;

    @TableField("address_tag")
    private String addressTag;

    /**
     * 获取完整地址
     */
    public String getFullAddress() {
        StringBuilder builder = new StringBuilder();
        append(builder, province);
        if (city != null && !city.equals(province)) {
            append(builder, city);
        }
        append(builder, district);
        append(builder, detailAddress);
        return builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (value != null && !value.isBlank()) {
            builder.append(value);
        }
    }
}

package com.petcloud.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户收货地址实体类（shop-service本地读取）
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_address")
public class UserAddress extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("province")
    private String province;

    @TableField("city")
    private String city;

    @TableField("district")
    private String district;

    @TableField("detail_address")
    private String detailAddress;

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
